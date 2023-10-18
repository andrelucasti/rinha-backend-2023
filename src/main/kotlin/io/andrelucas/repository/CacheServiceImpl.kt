package io.andrelucas.repository

import io.andrelucas.business.Person
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.util.*

object CacheServiceImpl: CacheService {
    override suspend fun put(person: Person) {
        DataBaseFactory.jdbcConnection {
            it.autoCommit = false
            it.beginRequest()
            it.prepareStatement("INSERT INTO cache (id, key, value ) VALUES (?, ?, ?::jsonb)")
                .apply {
                    setString(1, person.id.toString())
                    setString(2, person.apelido)
                    setString(3, Json.encodeToJsonElement(person.toPersonCache()).toString())

                    execute()
                }
            it.commit()
        }
    }

    override suspend fun get(id: UUID): Person? {
        return DataBaseFactory.jdbcConnection {
            val ps = it.prepareStatement("SELECT id, key, value from cache where id = ?")
            ps.setString(1, id.toString())
            val rs = ps.executeQuery()

            if (rs.next()) {
                return@jdbcConnection Json.decodeFromString(PersonCache.serializer(), rs.getString("value")).toPerson()
            } else
                return@jdbcConnection null
        }
    }

    override suspend fun exists(apelido: String): Boolean {
        return DataBaseFactory.jdbcConnection {
            val ps = it.prepareStatement("SELECT count(*) from cache where key = ?")
            ps.setString(1, apelido)
            val rs = ps.executeQuery()
            rs.next()
            rs.getLong(1) > 0
        }
    }

    override suspend fun count(): Long {
        return DataBaseFactory.jdbcConnection {
            val ps = it.prepareStatement("SELECT count(*) from cache")
            val rs = ps.executeQuery()
            rs.next()
            rs.getLong(1)
        }
    }

    override suspend fun findByTerm(term: String): List<Person> {
        return DataBaseFactory.jdbcConnection {
            val ps = it.prepareStatement("SELECT id, key, value from cache where key = ?")
            ps.setString(1, "%$term%")
            val rs = ps.executeQuery()

            val persons = mutableListOf<Person>()
            while (rs.next()) {
                persons.add(Json.decodeFromString(PersonCache.serializer(), rs.getString("value")).toPerson())
            }
            persons
        }
    }
}

