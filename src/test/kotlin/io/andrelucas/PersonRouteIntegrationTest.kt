package io.andrelucas

import io.andrelucas.app.PersonRequest
import io.andrelucas.app.PersonResponse
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PersonRouteIntegrationTest {
    @Test
    fun shouldReturnStatusCreatedAndTheLocationWhenAPersonIsCreated() = testApplication {
        application {
            module()
        }
        client.post("/pessoas") {
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
        client.post("/pessoas") {
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

        client.post("/pessoas") {
            contentType(ContentType.Application.Json)
            setBody("""
                    {
                        "apelido" : "josé",
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

        client.post("/pessoas") {
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

        client.post("/pessoas") {
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
    fun shouldReturnStatusBadRequestWhenAPersonIsCreatedWithInvalidValueAtTheStackField() = testApplication {
        application {
            module()
        }
        client.post("/pessoas") {
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
        client.post("/pessoas") {
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
        client.post("/pessoas") {
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

        client.post("/pessoas") {
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
    fun shouldReturnStatusBadRequestWhenNotAPersonIsNotFound() = testApplication {
        application {
            module()
        }

        val location = "/pessoas/f7379ae8-8f9b-4cd5-8221-51efe19e721b"

        client.get(location).apply {
            assertEquals(HttpStatusCode.BadRequest, status)
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