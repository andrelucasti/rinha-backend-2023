package io.andrelucas.business

import java.util.UUID

interface PersonRepository {
    suspend fun save(person: Person)
    fun saveBatch(personList: List<Person>)
    suspend fun findById(id: UUID): Person?
    suspend fun delete(id: UUID)
    suspend fun update(person: Person): Person
}