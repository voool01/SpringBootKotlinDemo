package com.example.config

import com.example.model.Person
import com.example.validate.validate

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class FirebaseConfig : CommandLineRunner {
    @Autowired
    lateinit var fb: DatabaseReference

    override fun run(vararg args: String?) {
        /*
         * Firebase Listeners to update the Person Repository
         */
        fb.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot?, key: String?) {
                val newPerson = dataSnapshot!!.getValue(Person::class.java)
                validate(newPerson, fb, dataSnapshot.key)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot?, key: String?) {
                val changedPerson = dataSnapshot!!.getValue(Person::class.java)
                validate(changedPerson, fb, dataSnapshot.key)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot?, key: String?) {
                val movedPerson = dataSnapshot!!.getValue(Person::class.java)
                validate(movedPerson, fb, dataSnapshot.key)
            }

            override fun onCancelled(databaseError: DatabaseError?) {
                if (databaseError != null) {
                    throw databaseError.toException()
                }
            }
        })
        //These People will be added to the Repository by the Listeners
        listOf(
                Person("Jon", "Snow", "kotlin"),
                Person("Daenerys", "Targaryen", "kotlin"),
                Person("Tyrion", "Lannister", "kotlin"),
                Person("Cersei", "Lannister", "javascript"),
                Person("Sansa", "Stark", "java")
        ).forEach { fb.child(it.id).setValue(it) }
    }
}