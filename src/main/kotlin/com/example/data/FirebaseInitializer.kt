package com.example.data

import com.example.DemoApplication
import com.example.model.Person
import com.example.validate.PersonValidator

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseCredentials
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import nl.komponents.kovenant.Deferred
import nl.komponents.kovenant.deferred

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import org.springframework.validation.DataBinder

@Configuration
@Service
open class FirebaseInitializer {
    @Bean
    open fun databaseReference(): DatabaseReference {
        val serviceAccountKeyStream = DemoApplication::class.java.classLoader.getResourceAsStream("google-services.json")
        val options = FirebaseOptions.Builder()
                .setCredential(FirebaseCredentials.fromCertificate(serviceAccountKeyStream))
                .setDatabaseUrl("https://your.firebase.url.io/")
                .build()
        FirebaseApp.initializeApp(options)
        return FirebaseDatabase.getInstance().reference.child("people")
    }
}

/*
 * Called when a PATCH/PUT/POST REST Call is made
 * Validates the sent Person Object and if it is valid,
 * it is pushed to Firebase.
 * Else, an exception is thrown.
 */
fun DatabaseReference.validateAndSave(person: Person): Person {
    val binder = DataBinder(person)
    binder.validator = PersonValidator()
    binder.validate()

    val results = binder.bindingResult
    if (results.hasFieldErrors())
        throw Exception(results.fieldErrors.toString())

    this.child(person.id).setValue(person)
    return person
}

fun DatabaseReference.getChildById(id: String): Person? {
    val deferred: Deferred<Person?, Exception> = deferred()
    child(id).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            deferred.reject(p0.toException())
        }

        override fun onDataChange(p0: DataSnapshot) {
            deferred.resolve(p0.getValue(Person::class.java))
        }
    })
    if (deferred.promise.isFailure())
        throw deferred.promise.getError()
    return deferred.promise.get()
}

fun DatabaseReference.getChildren(): MutableList<Person> {
    val deferred: Deferred<MutableList<Person>, Exception> = deferred()
    addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            deferred.reject(p0.toException())
        }

        override fun onDataChange(p0: DataSnapshot) {
            val people: MutableList<Person> = mutableListOf()
            p0.children.forEach { people.add(it.getValue(Person::class.java)) }
            deferred.resolve(people)
        }
    })
    if (deferred.promise.isFailure())
        throw deferred.promise.getError()
    return deferred.promise.get()
}