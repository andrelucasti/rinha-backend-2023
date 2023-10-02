package io.andrelucas.app

import io.andrelucas.business.EntityNotFoundException
import io.andrelucas.business.PersonRepository
import java.util.*

class PersonService(private val personRepository: PersonRepository) {
    suspend fun create(personRequest: PersonRequest): UUID {
        val person = personRequest.toPerson()

        personRepository.save(person)

        return person.id
    }

    suspend fun findById(personId: String): PersonResponse {
        return personRepository.findById(UUID.fromString(personId))?.toPersonResponse()
            ?: throw EntityNotFoundException("Person not found")
    }
}


