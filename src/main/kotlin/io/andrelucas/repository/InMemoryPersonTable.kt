package io.andrelucas.repository

import io.andrelucas.business.Person
import java.util.*

class InMemoryPersonTable {
    private val items = mutableListOf<Person>()

    fun save(any: Person) {
        items.add(any)
    }

    fun findAll(): List<Person> {
        return items
    }

    fun findById(id: UUID): Person? {
        return items.find { it.id == id }
    }
}