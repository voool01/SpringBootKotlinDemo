package com.example.model.api

import com.example.model.Person

data class PersonRequest(
        val firstName: String? = null,
        val lastName: String? = null,
        val favoriteLanguage: String? = null
) {
    fun toPerson() = Person(
            firstName = firstName,
            lastName = lastName,
            favoriteLanguage = favoriteLanguage ?: "kotlin"
    )

    fun toPerson(id: String) = Person(
            id = id,
            firstName = firstName,
            lastName = lastName,
            favoriteLanguage = favoriteLanguage ?: "kotlin"
    )
}

infix fun Person.patchedWith(request: PersonRequest): Person {
    firstName = request.firstName ?: firstName
    lastName = request.lastName ?: lastName
    favoriteLanguage = request.favoriteLanguage ?: favoriteLanguage
    return this
}