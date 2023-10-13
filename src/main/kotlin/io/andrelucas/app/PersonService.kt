package io.andrelucas.app

import io.andrelucas.business.EntityNotFoundException
import io.andrelucas.business.Person
import io.andrelucas.business.PersonQuery
import io.andrelucas.business.PersonRepository
import io.andrelucas.repository.BufferPerson
import io.andrelucas.repository.BufferPerson.threadPool
import io.andrelucas.repository.CacheService
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import java.util.*
import java.util.concurrent.Executors

class PersonService private constructor(
                    private val personRepository: PersonRepository,
                    private val personQuery: PersonQuery,
                    private val cacheService: CacheService) {


    companion object {
        private var instance : PersonService? = null
        fun getInstance(personRepository: PersonRepository, personQuery: PersonQuery, cacheService: CacheService) : PersonService {
            if (instance == null) {
                instance = PersonService(personRepository, personQuery, cacheService)
            }
            return instance!!
        }
    }
    suspend fun create(personRequest: PersonRequest): UUID {
        LOGGER.info("Thread: ${Thread.currentThread().name}- creating person")
        return coroutineScope {
            val person = personRequest.toPerson()

            val thereIsAPerson = async { cacheService.exists(person.apelido).or(personQuery.exists(person.apelido)) }.await()
            if (thereIsAPerson) throw IllegalArgumentException("Person already inserted with this apelido ${person.apelido}")

            launch(threadPool) {
                LOGGER.info("Thread: ${Thread.currentThread().name}-  saving person in cache - launch")
                cacheService.put(person)

            }

            launch {
                LOGGER.info("Thread: ${Thread.currentThread().name}-  saving person in database - launch")
                personRepository.save(person)
            }

            person.id
        }
    }

    suspend fun findById(personId: String): PersonResponse {
       return coroutineScope {
           personRepository.findById(UUID.fromString(personId))?.toPersonResponse()
               ?: throw EntityNotFoundException("Person not found")
       }
    }

    suspend fun findByTerm(term: String): List<PersonResponse> {
       return coroutineScope {
           val personByTerm = personQuery.personByTerm(term)
           personByTerm.map { it.toPersonResponse() }
       }
    }

    suspend fun count(): Long {
        return personQuery.count()
    }
}
suspend fun <T> Channel<T>.size(): Int {
    var size = 0
    for (item in this) {
        size++
    }
    return size
}




