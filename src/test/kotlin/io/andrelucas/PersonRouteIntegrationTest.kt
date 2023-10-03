package io.andrelucas

import io.andrelucas.app.PersonRequest
import io.andrelucas.app.PersonResponse
import io.andrelucas.repository.DataBaseFactory
import io.andrelucas.repository.PersonTable
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PersonRouteIntegrationTest {

    private val path = "/pessoas"
    @BeforeTest
    fun setUp() {
        transaction(DataBaseFactory.database) {
            PersonTable.deleteAll()
        }
    }

    @Test
    fun shouldReturnStatusCreatedAndTheLocationWhenAPersonIsCreated() = testApplication {
        application {
            module()
        }
        client.post(path) {
            contentType(ContentType.Application.Json)
            setBody("""
                    {
                        "apelido" : "josé",
                        "nome" : "José Roberto",
                        "nascimento" : "2000-10-01",
                        "stack" : ["C#", "Node", "Oracle"]
                    }
                    """.trimIndent()
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val location = headers["Location"]
            assertFalse { location.isNullOrBlank() }
        }
    }

    @Test
    fun shouldReturnStatusCreatedWhenAValidPersonIsCreated() = testApplication {
        application {
            module()
        }
        client.post(path) {
            contentType(ContentType.Application.Json)
            setBody("""
                    {
                        "apelido" : "Norberto",
                        "nome" : "José Norberto",
                        "nascimento" : "2000-10-01",
                        "stack" : ["C#", "Node", "Oracle"]
                    }
                    """.trimIndent()
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val location = headers["Location"]
            assertFalse { location.isNullOrBlank() }
        }

        client.post(path) {
            contentType(ContentType.Application.Json)
            setBody("""
                    {
                        "apelido" : "Andre",
                        "nome" : "José Roberto",
                        "nascimento" : "2000-10-01",
                        "stack" : null
                    }
                    """.trimIndent()
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
            val location = headers["Location"]
            assertFalse { location.isNullOrBlank() }
        }
    }

    @Test
    fun shouldReturnStatusUnprocessableEntityWhenNameIsNull() = testApplication{
        application {
            module()
        }

        client.post(path) {
            contentType(ContentType.Application.Json)
            setBody("""
                    {
                        "apelido" : "josé",
                        "nome" : null,
                        "nascimento" : "2000-10-01",
                        "stack" : ["C#", "Node", "Oracle"]
                    }
                    """.trimIndent()
            )
        }.apply {
            assertEquals(HttpStatusCode.UnprocessableEntity, status)
            assertEquals("O campo nome é obrigatório", bodyAsText())
        }
    }

    @Test
    fun shouldReturnStatusUnprocessableEntityWhenApelidoIsNull() = testApplication{
        application {
            module()
        }

        client.post(path) {
            contentType(ContentType.Application.Json)
            setBody("""
                    {
                        "apelido" : null,
                        "nome" : "José Roberto",
                        "nascimento" : "2000-10-01",
                        "stack" : ["C#", "Node", "Oracle"]
                    }
                    """.trimIndent()
            )
        }.apply {
            assertEquals(HttpStatusCode.UnprocessableEntity, status)
            assertEquals("O campo apelido é obrigatório", bodyAsText())
        }
    }

    @Test
    fun shouldReturnStatusUnprocessableEntityWhenThePersonAlreadyWasCreated() = testApplication{
        application {
            module()
        }

        client.post(path) {
            contentType(ContentType.Application.Json)
            setBody("""
                    {
                        "apelido" : jose,
                        "nome" : "José Roberto",
                        "nascimento" : "2000-10-01",
                        "stack" : ["C#", "Node", "Oracle"]
                    }
                    """.trimIndent()
            )
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
        }

        client.post(path) {
            contentType(ContentType.Application.Json)
            setBody("""
                    {
                        "apelido" : jose,
                        "nome" : "José Roberto",
                        "nascimento" : "2000-10-01",
                        "stack" : ["C#", "Node", "Oracle"]
                    }
                    """.trimIndent()
            )
        }.apply {
            assertEquals(HttpStatusCode.UnprocessableEntity, status)
        }
    }


    @Test
    fun shouldReturnStatusBadRequestWhenAPersonIsCreatedWithInvalidValueAtTheStackField() = testApplication {
        application {
            module()
        }
        client.post(path) {
            contentType(ContentType.Application.Json)
            setBody("""
                    {
                        "apelido" : "josé",
                        "nome" : "José Roberto",
                        "nascimento" : "2000-10-01",
                        "stack" : [1, "Node", "Oracle"]
                    }
                    """.trimIndent()
            )
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun shouldReturnStatusBadRequestWhenAPersonIsCreatedWithInvalidValueAtTheNomeField() = testApplication {
        application {
            module()
        }
        client.post(path) {
            contentType(ContentType.Application.Json)
            setBody("""
                    {
                        "apelido" : "josé"
                        "nome" : 1,
                        "nascimento" : "2000-10-01",
                        "stack" : [C#, "Node", "Oracle"]
                    }
                    """.trimIndent()
            )
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun shouldReturnStatusBadRequestWhenAPersonIsCreatedWithInvalidValueAtTheApelidoField() = testApplication {
        application {
            module()
        }
        client.post(path) {
            contentType(ContentType.Application.Json)
            setBody("""
                    {
                        "apelido" : 1
                        "nome" : "José Roberto",
                        "nascimento" : "2000-10-01",
                        "stack" : [C#, "Node", "Oracle"]
                    }
                    """.trimIndent()
            )
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun shouldFindPersonById() = testApplication {
        application {
            module()
        }

        client.post(path) {
            val body = createPersonBody(PersonRequest("josé", "José Roberto", "2000-10-01", listOf("C#", "Node", "Oracle")))

            contentType(ContentType.Application.Json)
            setBody(body)

        }.apply {
            assertEquals(HttpStatusCode.Created, status)

            val location = headers["Location"]

            client.get(location!!).apply {
                assertEquals(HttpStatusCode.OK, status)
                val response = Json.decodeFromString<PersonResponse>(bodyAsText())

                assertEquals("josé", response.apelido)
                assertEquals("José Roberto", response.nome)
                assertEquals("2000-10-01", response.nascimento)
                assertEquals(listOf("C#", "Node", "Oracle"), response.stack)

            }

        }

    }

    @Test
    fun shouldReturnStatusNotFoundWhenNotAPersonIsNotFound() = testApplication {
        application {
            module()
        }

        val location = "${path}/f7379ae8-8f9b-4cd5-8221-51efe19e721b"

        client.get(location).apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

    @Test
    fun shouldReturnPersonsByStack() = testApplication {
        application {
            module()
        }

        client.post(path){
            val body = createPersonBody(PersonRequest("josé", "José Roberto", "2000-10-01", listOf("Java", "Node", "Oracle")))

            contentType(ContentType.Application.Json)
            setBody(body)
        }.apply { assertEquals(HttpStatusCode.Created, status) }

        client.post(path){
            val body = createPersonBody(PersonRequest("Andre", "André Lucas", "1994-02-02", listOf("Java", "Kotlin", "Rust")))

            contentType(ContentType.Application.Json)
            setBody(body)
        }.apply { assertEquals(HttpStatusCode.Created, status) }

        client.post(path){
            val body = createPersonBody(PersonRequest("Vitor", "Vitor Pereira", "1994-02-01", listOf("k8s", "Go", "Python")))

            contentType(ContentType.Application.Json)
            setBody(body)
        }.apply { assertEquals(HttpStatusCode.Created, status) }

        client.get("${path}?t=Java").apply{
            assertEquals(HttpStatusCode.OK, status)

            val response = Json.decodeFromString<List<PersonResponse>>(bodyAsText())

            assertEquals(2, response.size)
            assertEquals("josé", response[0].apelido)
            assertEquals("Andre", response[1].apelido)
        }
    }

    @Test
    fun shouldReturnPersonsByNames() = testApplication {
        application {
            module()
        }

        client.post(path){
            val body = createPersonBody(PersonRequest("josé", "José Roberto Silva", "2000-10-01", listOf("Java", "Node", "Oracle")))

            contentType(ContentType.Application.Json)
            setBody(body)
        }.apply { assertEquals(HttpStatusCode.Created, status) }

        client.post(path){
            val body = createPersonBody(PersonRequest("Andre", "André Lucas Santos Silva", "1994-02-02", listOf("Java", "Kotlin", "Rust")))

            contentType(ContentType.Application.Json)
            setBody(body)
        }.apply { assertEquals(HttpStatusCode.Created, status) }

        client.post(path){
            val body = createPersonBody(PersonRequest("Vitor", "Vitor Pereira Santos", "1994-02-01", listOf("k8s", "Go", "Python")))

            contentType(ContentType.Application.Json)
            setBody(body)
        }.apply { assertEquals(HttpStatusCode.Created, status) }

        client.get("${path}?t=Silva").apply{
            assertEquals(HttpStatusCode.OK, status)

            val response = Json.decodeFromString<List<PersonResponse>>(bodyAsText())

            assertEquals(2, response.size)
            assertEquals("josé", response[0].apelido)
            assertEquals("Andre", response[1].apelido)
        }
    }

    private val createPersonBody: (personRequest: PersonRequest) ->  String =
         {
             """
            {
                "apelido" : "${it.apelido}",
                "nome" : "${it.nome}",
                "nascimento" : "${it.nascimento}",
                "stack" : ${it.stack}
            }
            """.trimIndent()
         }
}