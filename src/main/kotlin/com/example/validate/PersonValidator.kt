package com.example.validate

import com.example.model.Person

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

fun validate(person: Person) {
    val binder = DataBinder(person)
    binder.validator = PersonValidator()
    binder.validate()

    val results = binder.bindingResult
    if (results.hasFieldErrors())
        throw Exception(results.fieldErrors.toString())
}