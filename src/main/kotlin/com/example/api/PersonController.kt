package com.example.api

import com.example.data.getChildById
import com.example.data.getChildren
import com.example.data.validateAndSave
import com.example.model.Person
import com.example.model.api.PersonRequest
import com.example.model.api.patchedWith

import com.google.firebase.database.DatabaseReference

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("persons")
@Api(value = "Persons")
class PersonsController(val fb: DatabaseReference) {
    @GetMapping
    @ApiOperation(value = "Get All People")
    fun getAllPeople(): MutableList<Person> =
            fb.getChildren()

    @GetMapping("{id}")
    @ApiOperation(value = "Get Person By Id")
    fun getPerson(@PathVariable id: String): Person =
            if (fb.getChildById(id) != null) fb.getChildById(id)!!
            else throw Exception("Person with id $id not found")

    @PostMapping
    @ApiOperation(value = "Create People")
    fun createPerson(@RequestBody request: PersonRequest) =
            fb.validateAndSave(request.toPerson())

    @PatchMapping("{id}")
    @ApiOperation(value = "Update People")
    fun updatePerson(@PathVariable id: String, @RequestBody request: PersonRequest) =
            if (fb.getChildById(id) != null) fb.validateAndSave(fb.getChildById(id)!!.patchedWith(request))
            else throw Exception("Person with id $id not found")

    @PutMapping("{id}")
    @ApiOperation(value = "Replace People")
    fun replacePerson(@PathVariable id: String, @RequestBody request: PersonRequest) =
            if (fb.getChildById(id) != null) fb.validateAndSave(request.toPerson(id))
            else throw Exception("Person with id $id not found")

    @DeleteMapping("{id}")
    @ApiOperation(value = "Delete People")
    fun deletePerson(@PathVariable id: String): Person =
            if (fb.getChildById(id) != null) {
                val person = fb.getChildById(id)
                fb.child(id).removeValue()
                person!!
            } else throw Exception("Person with id $id not found")

    @GetMapping("kotlin")
    @ApiOperation(value = "Get People Who Love Kotlin")
    fun getAllKotlinLovers() = fb.getChildren().filter { it.favoriteLanguage.equals("kotlin", false) }
}