package io.andrelucas.repository

import io.andrelucas.business.Person
import io.andrelucas.business.PersonRepository
import io.andrelucas.repository.DataBaseFactory.dbQuery
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class PersonRepositoryImpl(private val database: Database) : PersonRepository {

    override suspend fun save(person: Person): Unit = dbQuery {
       transaction(database){
           PersonTable.insert {
               it[id] = person.id
               it[apelido] = person.apelido
               it[nome] = person.nome
               it[nascimento] = person.nascimento
               it[stack] = Json.encodeToString(PersonStack.serializer(), PersonStack(person.stack))
           }
       }
    }

    override suspend fun findById(id: UUID): Person? {
        return dbQuery {
            transaction(database) {
                PersonTable.select { PersonTable.id eq id }.mapNotNull {
                    Person(
                        id = it[PersonTable.id],
                        apelido = it[PersonTable.apelido],
                        nome = it[PersonTable.nome],
                        nascimento = it[PersonTable.nascimento],
                        stack = Json.decodeFromString(PersonStack.serializer(), it[PersonTable.stack]).stack
                    )
                }.singleOrNull()
            }
        }
    }

    override suspend fun findAll(): List<Person> = dbQuery {
        transaction(database) {
            PersonTable.selectAll().map {
                Person(
                    id = it[PersonTable.id],
                    apelido = it[PersonTable.apelido],
                    nome = it[PersonTable.nome],
                    nascimento = it[PersonTable.nascimento],
                    stack = Json.decodeFromString(PersonStack.serializer(), it[PersonTable.stack]).stack
                )
            }
        }
    }

    override suspend fun delete(id: UUID) {
        TODO("Not yet implemented")
    }

    override suspend fun update(person: Person): Person {
        TODO("Not yet implemented")
    }
}