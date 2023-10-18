package io.andrelucas.repository

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

object BufferPerson {
    val threadPool = Executors.newCachedThreadPool()
        .asCoroutineDispatcher()
}