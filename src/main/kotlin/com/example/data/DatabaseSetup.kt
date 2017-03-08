package com.example.data

import com.example.model.Person
import com.example.validate.validate

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class DatabaseSetup : CommandLineRunner {
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    override fun run(vararg args: String?) {
        println("Creating tables")

        jdbcTemplate.execute("DROP TABLE people IF EXISTS")
        jdbcTemplate.execute("CREATE TABLE people(" +
        "id VARCHAR(255), " +
                "first_name VARCHAR(255), " +
                "last_name VARCHAR(255), " +
                "favorite_language VARCHAR(255), " +
                "PRIMARY KEY (id))")

        listOf(
                Person("Jon", "Snow", "kotlin"),
                Person("Daenerys", "Targaryen", "kotlin"),
                Person("Tyrion", "Lannister", "kotlin"),
                Person("Cersei", "Lannister", "javascript"),
                Person("Sansa", "Stark", "java")
        ).apply {
            forEach{
                println("Inserting Person Record for ${it.firstName} ${it.lastName}:")
            }
            forEach{
                jdbcTemplate.execute("INSERT INTO people(id, first_name, last_name, favorite_language) " +
                    "VALUES ('${it.id}', '${it.firstName}', '${it.lastName}', '${it.favoriteLanguage}')")
            }
        }

        println("Querying for person records where favorite_language = \"Kotlin\":")
        jdbcTemplate.query("SELECT * FROM people WHERE favorite_language = 'kotlin'") {
            rs, rowNum -> Person(id = rs.getString("id"), firstName = rs.getString("first_name"), lastName = rs.getString("last_name"), favoriteLanguage = rs.getString("favorite_language")) }.
                forEach(::println)
    }
}

fun JdbcTemplate.getAll() =
        query("SELECT * FROM people") {
            rs, rowNum -> Person(id = rs.getString("id"), firstName = rs.getString("first_name"), lastName = rs.getString("last_name"), favoriteLanguage = rs.getString("favorite_language"))
        }

fun JdbcTemplate.getById(id : String) =
        query("SELECT * FROM people WHERE id = '$id'") {
            rs, rowNum -> Person(id = rs.getString("id"), firstName = rs.getString("first_name"), lastName = rs.getString("last_name"), favoriteLanguage = rs.getString("favorite_language"))
        }.apply {
            if(this.isEmpty())
                throw Exception("Person with id $id not found")
        }

fun JdbcTemplate.getAllKotlinLovers() =
        query("SELECT * FROM people WHERE favorite_language = 'kotlin'") {
            rs, rowNum -> Person(id = rs.getString("id"), firstName = rs.getString("first_name"), lastName = rs.getString("last_name"), favoriteLanguage = rs.getString("favorite_language"))
        }

fun JdbcTemplate.create(person : Person) {
    validate(person)
    execute("INSERT INTO people(id, first_name, last_name, favorite_language) " +
            "VALUES ('${person.id}', '${person.firstName}', ${if (person.lastName != null) "'${person.lastName}'" else "null"}, ${if (person.favoriteLanguage != null) "'${person.favoriteLanguage}'" else "null"})")
}

fun JdbcTemplate.update(person : Person) {
    validate(person)
    execute("UPDATE people set " +
            "first_name='${person.firstName}', " +
            "last_name=${if (person.lastName != null) "'${person.lastName}'" else "null"}, " +
            "favorite_language=${if (person.favoriteLanguage != null) "'${person.favoriteLanguage}'" else "null"} " +
            "WHERE id='${person.id}'")
}

fun JdbcTemplate.delete(id : String) =
        execute("DELETE FROM people WHERE id='$id'")