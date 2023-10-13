package io.andrelucas.app

import io.andrelucas.repository.CacheServiceImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.createPerson(personService: PersonService) {

    post("/pessoas") {
        val personRequest = call.receive<PersonRequest>()
        val id = personService.create(personRequest)
        call.response.headers.append(HttpHeaders.Location, "/pessoas/$id")
        call.respond(HttpStatusCode.Created, "Pessoa criada com id $id")
    }
}

fun Route.findPersonById(personService: PersonService) {
    get("/pessoas/{id}") {
        val id = call.parameters["id"] ?: throw IllegalArgumentException("Id inválido")

        val personCache = CacheServiceImpl.get(UUID.fromString(id))

        if (personCache != null) {
            call.respond(HttpStatusCode.OK, personCache.toPersonResponse())
            return@get
        }

        val person = personService.findById(id)
        call.respond(HttpStatusCode.OK, person)
    }
}

fun Route.countPerson(personService: PersonService) {
        get("/contagem-pessoas") {
        val count = personService.count()

        call.respond(HttpStatusCode.OK, count)
    }
}

fun Route.findPersonByTerm(personService: PersonService) {
    get("/pessoas") {
        val term = call.request.queryParameters["t"] ?: throw BadRequestException("t é obrigatório")
        CacheServiceImpl.findByTerm(term).let {
            if (it.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, it.map { personCache -> personCache.toPersonResponse() })
                return@get
            } else {
                val persons = personService.findByTerm(term)
                call.respond(HttpStatusCode.OK, persons)
            }
        }
    }
}
