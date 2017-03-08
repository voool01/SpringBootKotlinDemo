package com.example.api

import com.example.data.*
import com.example.model.Person
import com.example.model.api.PersonRequest
import com.example.model.api.patchedWith

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
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
class PersonsController @Autowired constructor(val jdbcTemplate: JdbcTemplate) {
    @GetMapping
    @ApiOperation(value = "Get All People")
    fun getAllPeople() = jdbcTemplate.getAll()

    @GetMapping("{id}")
    @ApiOperation(value = "Get Person By Id")
    fun getPerson(@PathVariable id: String) =
            jdbcTemplate.getById(id)[0]

    @PostMapping
    @ApiOperation(value = "Create People")
    fun createPerson(@RequestBody request: PersonRequest) =
            jdbcTemplate.create(request.toPerson())

    @PatchMapping("{id}")
    @ApiOperation(value = "Update People")
    fun updatePerson(@PathVariable id: String, @RequestBody request: PersonRequest) =
            jdbcTemplate.update(jdbcTemplate.getById(id)[0].patchedWith(request))

    @PutMapping("{id}")
    @ApiOperation(value = "Replace People")
    fun replacePerson(@PathVariable id: String, @RequestBody request: PersonRequest) =
            jdbcTemplate.update(request.toPerson(
                    jdbcTemplate.getById(id)[0].id
            ))

    @DeleteMapping("{id}")
    @ApiOperation(value = "Delete People")
    fun deletePerson(@PathVariable id: String) =
            jdbcTemplate.delete(jdbcTemplate.getById(id)[0].id)

    @GetMapping("kotlin")
    @ApiOperation(value = "Get People Who Love Kotlin")
    fun getAllKotlinLovers() =
            jdbcTemplate.getAllKotlinLovers()
}