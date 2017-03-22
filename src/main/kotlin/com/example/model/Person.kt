package com.example.model

import com.fasterxml.jackson.annotation.JsonInclude

import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Person(
        var firstName: String? = null,
        var lastName: String? = null,
        var favoriteLanguage: String? = null,
        val id: String = UUID.randomUUID().toString()
)