package io.andrelucas.app

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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
        val id = call.parameters["id"] ?: throw IllegalArgumentException("Id inv�lido")

        //val personCache = CacheServiceImpl.get(UUID.fromString(id))


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
        val term = call.request.queryParameters["t"] ?: throw BadRequestException("t � obrigat�rio")
        val persons = personService.findByTerm(term)
        call.respond(HttpStatusCode.OK, persons)
    }
}
