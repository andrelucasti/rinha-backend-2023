package io.andrelucas.app

import io.andrelucas.business.Person
import io.andrelucas.business.PersonRepository
import io.andrelucas.repository.BufferPerson
import io.andrelucas.repository.CacheService
import io.ktor.server.application.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

fun Application.handle(cacheChannel: Channel<Person>,
                       bufferInsertChannel: Channel<Person>,
                       batchInsertChannel: Channel<List<Person>>,
                       personRepository: PersonRepository,
                       cacheService: CacheService){

    val nWorkers = environment.config.property("ktor.config.workerPoll").getString().toInt()
    val batchSize = environment.config.property("ktor.config.batchSize").getString().toInt()

    repeat(nWorkers){
        launch(BufferPerson.threadPool) {
            workerSaveInCache(cacheChannel, bufferInsertChannel, cacheService)
        }
        launch(BufferPerson.threadPool) {
            workerSaveInBufferBackground(bufferInsertChannel, batchInsertChannel, batchSize)
        }
    }


    launch(BufferPerson.threadPool) {
        workerSaveInDatabase(batchInsertChannel, personRepository)
    }
}