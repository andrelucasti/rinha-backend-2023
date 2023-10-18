package io.andrelucas.app

import io.andrelucas.business.EntityNotFoundException
import io.andrelucas.business.Person
import io.andrelucas.business.PersonQuery
import io.andrelucas.business.PersonRepository
import io.andrelucas.repository.BufferPerson
import io.andrelucas.repository.CacheService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        return withContext(BufferPerson.threadPool) {
            val person = personRequest.toPerson()
            val thereIsAPerson = personQuery.exists(person.apelido)
            if (thereIsAPerson) throw IllegalArgumentException("Person already inserted with this apelido ${person.apelido}")

            val personChannel = Channel<Person>()
            launch(BufferPerson.threadPool) {
                LOGGER.info("Sending person to worker thread - launch")
                personChannel.send(person)

                throw IllegalArgumentException("test excep ${person.apelido}")
            }

            launch(BufferPerson.threadPool) {
                LOGGER.info("Receiving person from worker thread - launch")
                workerReceiver(personChannel, personRepository)
            }
            personChannel.close()
            person.id
        }
    }

    suspend fun findById(personId: String): PersonResponse {
        LOGGER.info("finding person in database - no launch")
        return personRepository.findById(UUID.fromString(personId))?.toPersonResponse()
               ?: throw EntityNotFoundException("Person not found")
    }

    suspend fun findByTerm(term: String): List<PersonResponse> {
        LOGGER.info("finding person by term in database - no launch")
        val personByTerm = personQuery.personByTerm(term)
        return personByTerm.map { it.toPersonResponse() }
    }

    suspend fun count(): Long {
        return personQuery.count()
    }
}

fun CoroutineScope.workerReceiver(receiveChannel: ReceiveChannel<Person>, personRepository: PersonRepository) = launch(BufferPerson.threadPool) {
    for (person in receiveChannel) {
        LOGGER.info("Thread: ${Thread.currentThread().name}-  saving person in database - launch")
        personRepository.save(person)
    }
}



