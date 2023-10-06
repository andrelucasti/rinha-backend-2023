package io.andrelucas.app

import io.andrelucas.business.EntityNotFoundException
import io.andrelucas.business.Person
import io.andrelucas.business.PersonQuery
import io.andrelucas.business.PersonRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*

class PersonService(private val personRepository: PersonRepository,
                    private val personQuery: PersonQuery) {
    suspend fun create(personRequest: PersonRequest): UUID {
        val person = personRequest.toPerson()

        val thereIsAPerson = personQuery.personAlreadyInserted(person.apelido)
        if (thereIsAPerson) throw IllegalArgumentException("Person already inserted with this apelido ${person.apelido}")

        personRepository.save(person)
        return person.id
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


