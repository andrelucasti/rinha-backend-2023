package io.andrelucas.app

import io.andrelucas.business.EntityNotFoundException
import io.andrelucas.business.PersonQuery
import io.andrelucas.business.PersonRepository
import io.andrelucas.repository.CacheService
import java.util.*

class PersonService private constructor(
                    private val personRepository: PersonRepository,
                    private val personQuery: PersonQuery,
                    private val cacheService: CacheService) {

    companion object {
        private var instance : PersonService? = null
        fun getInstance(personRepository: PersonRepository,
                        personQuery: PersonQuery,
                        cacheService: CacheService) : PersonService {
            if (instance == null) {
                instance = PersonService(personRepository, personQuery, cacheService)
            }
            return instance!!
        }
    }
    suspend fun create(personRequest: PersonRequest): UUID {
        LOGGER.info("Thread: ${Thread.currentThread().name}- creating person")

        val person = personRequest.toPerson()

        val thereIsAPerson = personQuery.exists(person.apelido)
        if (thereIsAPerson) throw IllegalArgumentException("Person already inserted with this apelido ${person.apelido}")

        LOGGER.info("Thread: ${Thread.currentThread().name}-  saving person in database - launch")
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



