package com.example

import au.com.console.jpaspecificationdsl.equal
import com.google.common.base.Predicates
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.query.Param
import org.springframework.validation.DataBinder
import org.springframework.validation.Errors
import org.springframework.validation.ValidationUtils
import org.springframework.validation.Validator
import org.springframework.web.bind.annotation.*
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.io.FileInputStream
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.Id


@SpringBootApplication
class DemoApplication {
    val firebaseConfigPath = "path/to/your/config/json"
    val firebaseDatabseUrl = "your-firebase-url"

    @Bean
    fun init(repository: PersonRepository, fb: DatabaseReference) = CommandLineRunner {
        /*
         * Firebase Listeners to update the Person Repository
         */
        fb.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot?, key : String?) {
                val newPerson = dataSnapshot!!.getValue(Person::class.java)
                println("Child Added:")
                println(newPerson)
                repository.validateAndSave(newPerson, fb, dataSnapshot!!.key)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot?, key: String?) {
                val changedPerson = dataSnapshot!!.getValue(Person::class.java)
                println("Child Changed:")
                println(changedPerson)
                repository.validateAndSave(changedPerson, fb, dataSnapshot!!.key)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
                val deletedPerson = dataSnapshot!!.getValue(Person::class.java)
                println("Child Deleted:")
                println(deletedPerson)
                repository.delete(deletedPerson)
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot?, key: String?) {
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
        ).forEach{fb.child(it.id).setValue(it)}

    }

    @Bean
    fun initFirebase(): DatabaseReference {
        val options = FirebaseOptions.Builder()
                .setServiceAccount(
                        FileInputStream(firebaseConfigPath))
                .setDatabaseUrl(firebaseDatabseUrl)
                .build()

        FirebaseApp.initializeApp(options)
        return FirebaseDatabase.getInstance().getReferenceFromUrl(firebaseDatabseUrl)
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(DemoApplication::class.java, *args)
}

@Entity
data class Person(
        var firstName: String? = null,
        var lastName: String? = null,
        var favoriteLanguage: String? = null,
        @Id
        val id: String = UUID.randomUUID().toString()
)

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

infix fun Person.patchedWith(request: PersonRequest) : Person {
    firstName = request.firstName ?: firstName
    lastName = request.lastName ?: lastName
    favoriteLanguage = request.favoriteLanguage ?: favoriteLanguage
    return this
}

interface PersonRepository : JpaRepository<Person, String>, JpaSpecificationExecutor<Person> {
    fun findByLastNameIgnoreCase(@Param("lastName") name: String): List<Person>
}

/*
 * Called when listeners detect an added or changed child.
 * Validates the child and if its a valid child, it saves it to the repository.
 * Else, it is deleted from Firebase.
 */
fun PersonRepository.validateAndSave(person: Person, fb: DatabaseReference, key : String) {
    if (!person.id.equals(key))
        fb.child(key).removeValue() //Checks to make sure the key and the id are equal

    val binder = DataBinder(person)
    binder.validator = PersonValidator()
    binder.validate()

    val results = binder.bindingResult
    if (results.hasFieldErrors())
        fb.child(key).removeValue()

    save(person)
}

/*
 * Called when a PATCH/PUT/POST REST Call is made
 * Validates the sent Person Object and if it is valid,
 * it is pushed to Firebase.
 * Else, an exception is thrown.
 */
fun DatabaseReference.validateAndSave(person: Person) : Person {
    val binder = DataBinder(person)
    binder.validator = PersonValidator()
    binder.validate()

    val results = binder.bindingResult
    if (results.hasFieldErrors())
        throw Exception(results.fieldErrors.toString())

    this.child(person.id).setValue(person)
    return person
}

@RestController
@RequestMapping("persons")
@Api(value = "Persons")
class PersonsController(val personRepository: PersonRepository, val databaseReference: DatabaseReference) {
    @GetMapping
    @ApiOperation(value = "Get All People")
    fun getAllPeople() = personRepository.findAll()

    @GetMapping("{id}")
    @ApiOperation(value = "Get People By Id")
    fun getAllPeople(@PathVariable id: String) = personRepository.findOne(id)

    @PostMapping
    @ApiOperation(value = "Create People")
    fun createPerson(@RequestBody request: PersonRequest) = databaseReference.validateAndSave(request.toPerson())

    @PatchMapping("{id}")
    @ApiOperation(value = "Update People")
    fun updatePerson(@PathVariable id : String, @RequestBody request: PersonRequest) =
    if(personRepository.exists(id)) databaseReference.validateAndSave(personRepository.findOne(id).patchedWith(request))
    else throw Exception("Person with id $id not found")

    @PutMapping("{id}")
    @ApiOperation(value = "Replace People")
    fun replacePerson(@PathVariable id : String, @RequestBody request: PersonRequest) =
            if(personRepository.exists(id)) databaseReference.validateAndSave(request.toPerson(id))
            else throw Exception("Person with id $id not found")

    @DeleteMapping("{id}")
    @ApiOperation(value = "Delete People")
    fun deletePerson(@PathVariable id : String) : Person =
            if(personRepository.exists(id)) {
                val person = personRepository.findOne(id)
                databaseReference.child(id).removeValue()
                person
            }
            else throw Exception("Person with id $id not found")

    @GetMapping("kotlin")
    @ApiOperation(value = "Get People Who Love Kotlin")
    fun GetAllKotlinLovers() = personRepository.findAllKotlinLovers()
}

private fun PersonRepository.findAllKotlinLovers(): List<Person> =
        findAll(Person::favoriteLanguage.equal("kotlin"))

@Configuration
@EnableSwagger2
open class SwaggerConfig {
    @Bean
    open fun swaggerConfigDocket(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(Predicates.not(RequestHandlerSelectors.basePackage("org.springframework.boot")))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo())
    }

    private fun apiInfo(): ApiInfo {
        val apiInfo = ApiInfo(
                "Person API",
                "Firebase Spring Person Example",
                "v0.0.1",
                "",
                Contact("", "", ""),
                "",
                "")
        return apiInfo
    }
}

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