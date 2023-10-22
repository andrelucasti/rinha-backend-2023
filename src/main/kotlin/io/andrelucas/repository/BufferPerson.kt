package io.andrelucas.repository

import io.andrelucas.business.Person
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object BufferPerson {
    val buffer = mutableListOf<Person>()
    val threadPool = Executors.newCachedThreadPool()
        .asCoroutineDispatcher()
}