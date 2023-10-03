package io.andrelucas.app

import io.andrelucas.business.EntityNotFoundException
import io.andrelucas.business.PersonQuery
import io.andrelucas.business.PersonRepository
import java.sql.SQLException
import java.util.*

class PersonService(private val personRepository: PersonRepository,
                    private val personQuery: PersonQuery) {
    suspend fun create(personRequest: PersonRequest): UUID {
        val person = personRequest.toPerson()

        try {
            personRepository.save(person)
            return person.id
        } catch (e: SQLException){
            throw IllegalArgumentException(e.message)
        }
    }

    suspend fun findById(personId: String): PersonResponse {
        return personRepository.findById(UUID.fromString(personId))?.toPersonResponse()
            ?: throw EntityNotFoundException("Person not found")
    }

    suspend fun findByTerm(term: String): List<PersonResponse> {
        val personByTerm = personQuery.personByTerm(term)
        return personByTerm.map { it.toPersonResponse() }
    }

    suspend fun count(): Long {
        return personQuery.count()
    }
}


