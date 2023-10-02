package io.andrelucas.repository

import io.andrelucas.business.Person
import io.andrelucas.business.PersonRepository
import java.util.*

class PersonInMemoryRepositoryImpl(private val memoryRepositoryImpl: InMemoryPersonTable) : PersonRepository {

    override suspend fun save(person: Person) {
        memoryRepositoryImpl.save(person)
    }

    override suspend fun findById(id: UUID): Person? {
        return memoryRepositoryImpl.findById(id)
    }

    override suspend fun findAll(): List<Person> {
        return memoryRepositoryImpl.findAll()
    }

    override suspend fun delete(id: UUID) {
        TODO("Not yet implemented")
    }

    override suspend fun update(person: Person): Person {
        TODO("Not yet implemented")
    }
}