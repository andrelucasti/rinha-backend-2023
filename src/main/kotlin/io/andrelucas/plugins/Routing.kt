package io.andrelucas.plugins

import io.andrelucas.app.PersonService
import io.andrelucas.app.createPerson
import io.andrelucas.app.findPersonById
import io.andrelucas.business.EntityNotFoundException
import io.andrelucas.business.NumericException
import io.andrelucas.repository.InMemoryPersonTable
import io.andrelucas.repository.PersonInMemoryRepositoryImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respondText(text = "${cause.message}", status = HttpStatusCode.UnprocessableEntity)
        }

        exception<NumericException> { call, cause ->
            call.respondText(text = "${cause.message}", status = HttpStatusCode.BadRequest)
        }

        exception<EntityNotFoundException> { call, cause ->
            call.respondText(text = "${cause.message}", status = HttpStatusCode.BadRequest)
        }
    }

    val personService = PersonService(
        PersonInMemoryRepositoryImpl(
            InMemoryPersonTable()
        )
    )

    routing {
        createPerson(personService)
        findPersonById(personService)
    }
}
