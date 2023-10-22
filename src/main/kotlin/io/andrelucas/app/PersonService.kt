package io.andrelucas.app

import io.andrelucas.business.EntityNotFoundException
import io.andrelucas.business.Person
import io.andrelucas.business.PersonQuery
import io.andrelucas.business.PersonRepository
import io.andrelucas.repository.BufferPerson
import io.andrelucas.repository.CacheService
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.selects.select
import java.time.Duration
import java.util.*

class PersonService private constructor(
                    private val personChannel: Channel<Person>,
                    private val personRepository: PersonRepository,
                    private val personQuery: PersonQuery,
                    private val cacheService: CacheService) {

    companion object {
        private var instance : PersonService? = null
        fun getInstance(
            personChannel: Channel<Person>,
            personRepository: PersonRepository,
            personQuery: PersonQuery,
            cacheService: CacheService) : PersonService {
            if (instance == null) {
                instance = PersonService(personChannel, personRepository, personQuery, cacheService)
            }
            return instance!!
        }
    }
    suspend fun create(personRequest: PersonRequest): UUID = withContext(BufferPerson.threadPool) {
        val person = personRequest.toPerson()
        val thereIsAPerson = cacheService.exists(person.apelido).or(personQuery.exists(person.apelido))
        if (thereIsAPerson) throw IllegalArgumentException("Person already inserted with this apelido ${person.apelido}")

        LOGGER.info("Sending person to worker thread - launch - ${person.id}")
        //senderPerson(personChannel, person)

        cacheService.put(person)
        personRepository.save(person)

        person.id
    }

    suspend fun findById(personId: String): PersonResponse {
      return withContext(BufferPerson.threadPool) {
          LOGGER.info("finding person by ID $personId - no launch")
          val person = cacheService.get(UUID.fromString(personId))
          person?.toPersonResponse()
              ?: (personRepository.findById(UUID.fromString(personId))?.toPersonResponse()
                  ?: throw EntityNotFoundException("Person not found with id $personId"))
      }
    }

    suspend fun findByTerm(term: String): List<PersonResponse> {
       return withContext(BufferPerson.threadPool) {
           LOGGER.info("finding person by term in database - no launch")

           val persons = cacheService.findByTerm(term).map { it.toPersonResponse() }

           if (persons.isEmpty()){
                return@withContext personQuery.personByTerm(term).map { it.toPersonResponse() }
           }

           return@withContext persons
       }
    }

    suspend fun count(): Long {
        return personQuery.count()
    }
}

fun CoroutineScope.senderPerson(personChannel: SendChannel<Person>, person: Person) = launch(BufferPerson.threadPool) {
    LOGGER.info("Sending person to worker thread - launch ${person.id}")
    personChannel.send(person)
}



fun CoroutineScope.workerSaveInCache(receiveChannel: ReceiveChannel<Person>, sendChannel: SendChannel<Person>, cacheService: CacheService) = launch(BufferPerson.threadPool) {
    while(true){
        LOGGER.info("listening person from worker thread - launch")
        select {
            receiveChannel.onReceive {
                LOGGER.info("Receiving person ${it.apelido} from worker to save in cache thread - launch")
                cacheService.put(it)
                sendChannel.send(it)
            }
        }
    }
}
fun CoroutineScope.workerSaveBackground(
    receiveChannel: ReceiveChannel<Person>,
    personRepository: PersonRepository,
    cacheService: CacheService,
    batchSize: Int
) = launch(BufferPerson.threadPool){
    LOGGER.info("loading person from cache to save in database - launch")
    val personBatch = mutableListOf<Person>()
    while (true) {
        LOGGER.info("listening person to save in database - launch")
        LOGGER.info("personBatch size ${personBatch.size}")

        select<Unit> {
            receiveChannel.onReceiveCatching {
                val person = it.getOrNull()
                if (person == null) {
                    LOGGER.info("Receiving null person from worker to save in database thread - launch")
                    personRepository.saveBatch(personBatch)
                    //cacheService.deleteBatch(personBatch)
                    personBatch.clear()
                } else {
                    LOGGER.info("Receiving person ${person.apelido} to batch save in database - launch")
                    if (personBatch.size < batchSize) {
                        personBatch.add(person)
                    } else {
                        personRepository.saveBatch(personBatch)
                        //cacheService.deleteBatch(personBatch)
                        personBatch.clear()
                    }

                }
            }
        }
    }
}
