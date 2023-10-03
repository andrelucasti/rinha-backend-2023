package io.andrelucas.plugins

import io.andrelucas.app.*
import io.andrelucas.business.EntityNotFoundException
import io.andrelucas.business.NumericException
import io.andrelucas.repository.DataBaseFactory
import io.andrelucas.repository.PersonQueyImpl
import io.andrelucas.repository.PersonRepositoryImpl
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
            call.respondText(text = "${cause.message}", status = HttpStatusCode.NotFound)
        }
    }

    val personService = PersonService(
        PersonRepositoryImpl(DataBaseFactory.database),
        PersonQueyImpl(DataBaseFactory.database)
    )

    routing {
        createPerson(personService)
        findPersonById(personService)
        findPersonByTerm(personService)
        countPerson(personService)
    }
}
