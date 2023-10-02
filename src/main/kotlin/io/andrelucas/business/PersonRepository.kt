package io.andrelucas.business

import java.util.UUID

interface PersonRepository {
    suspend fun save(person: Person)
    suspend fun findById(id: UUID): Person?
    suspend fun findAll(): List<Person>
    suspend fun delete(id: UUID)
    suspend fun update(person: Person): Person
}