package com.example.validate

import com.example.model.Person
import com.example.model.api.PersonRequest

import com.google.firebase.database.DatabaseReference

import org.springframework.validation.DataBinder
import org.springframework.validation.Errors
import org.springframework.validation.ValidationUtils
import org.springframework.validation.Validator

class PersonValidator : Validator {
    override fun supports(clazz: Class<*>?): Boolean {
        return Person::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any?, errors: Errors?) {
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors,
                "firstName",
                "firstName.field.required",
                "The request body must contain a non-null" +
                        "non-empty \"firstName\" String")
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors,
                "id",
                "id.field.required",
                "The request body must contain a non-null" +
                        "non-empty \"id\" String")
    }
}

/*
 * Called when listeners detect an added or changed child.
 * Validates the child and if its a valid child, it saves it to the repository.
 * Else, it is deleted from Firebase.
 */
fun validate(person: Person, fb: DatabaseReference, key: String) {
    //Checks to make sure the key and the id are equal
    //If they aren't, set id to key value
    if (!person.id.equals(key))
        fb.child(key).setValue(PersonRequest(person.firstName, person.lastName, person.favoriteLanguage).toPerson(key))

    val binder = DataBinder(person)
    binder.validator = PersonValidator()
    binder.validate()

    val results = binder.bindingResult
    if (results.hasFieldErrors())
        fb.child(key).removeValue()
}