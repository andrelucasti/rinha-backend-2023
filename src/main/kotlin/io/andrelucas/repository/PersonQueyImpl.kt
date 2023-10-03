package io.andrelucas.repository

import io.andrelucas.business.Person
import io.andrelucas.business.PersonQuery
import io.andrelucas.repository.DataBaseFactory.dbQuery
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class PersonQueyImpl(private val database: Database): PersonQuery {
    override suspend fun count(): Long = dbQuery {
        transaction(database) {
            PersonTable.selectAll().count()
        }
    }

    override suspend fun personByTerm(term: String): List<Person> = dbQuery {
        transaction(database) {
            PersonTable.select {
                        (PersonTable.nome like "%$term%") or
                        (PersonTable.apelido like "%$term%") or
                        (PersonTable.nome like "%$term%") or
                        (PersonTable.stack like "%$term%") }.mapNotNull {

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
}