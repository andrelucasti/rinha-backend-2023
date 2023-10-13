package io.andrelucas.repository

import io.andrelucas.business.Person
import java.util.UUID

interface CacheService {

    suspend fun put(person: Person)
    suspend fun get(id: UUID): Person?
    suspend fun exists(apelido: String): Boolean
    suspend fun count(): Long
    suspend fun findByTerm(term: String): List<Person>
}