package io.andrelucas.repository

import io.andrelucas.business.Person
import io.andrelucas.business.PersonRepository
import io.andrelucas.repository.DataBaseFactory.database
import io.andrelucas.repository.DataBaseFactory.dbQuery
import io.andrelucas.repository.DataBaseFactory.jdbcConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.PreparedStatement
import java.util.*

object PersonRepositoryImpl: PersonRepository {
    override suspend fun save(person: Person): Unit =
       jdbcConnection {
           it.autoCommit = false
           it.beginRequest()
           it.prepareStatement(
               "INSERT INTO person (id, apelido, nome, nascimento, stack, search) VALUES (?, ?, ?, ?, ?, ?)"
           ).apply {

                setObject(1, person.id)
                setString(2, person.apelido)
                setString(3, person.nome)
                setDate(4, java.sql.Date.valueOf(person.nascimento))
                setArray(5, it.createArrayOf("TEXT", person.stack?.toTypedArray()))
                setString(6, person.nome + " " + person.apelido + " " + person.stack?.joinToString(" "))
                addBatch()
                executeBatch()
           }

           it.commit()
       }


    override suspend fun saveBatch(personList: List<Person>): Unit =
        jdbcConnection {
            it.prepareStatement(
                "INSERT INTO person (id, apelido, nome, nascimento, stack, search) VALUES (?, ?, ?, ?, ?, ?)"
            ).apply {
                personList.forEach { person ->
                    setObject(1, person.id)
                    setString(2, person.apelido)
                    setString(3, person.nome)
                    setDate(4, java.sql.Date.valueOf(person.nascimento))
                    setArray(5, it.createArrayOf("TEXT", person.stack?.toTypedArray()))
                    setString(6, person.nome + " " + person.apelido + " " + person.stack?.joinToString(" "))
                    addBatch()
                }
                executeBatch()
            }
        }


    override suspend fun findById(id: UUID): Person? {
        return jdbcConnection {
            val ps = it.prepareStatement("SELECT id, apelido, nome, nascimento, stack from person where id = ?")
            ps.setObject(1, id)
            val rs = ps.executeQuery()

            if (rs.next()) {
                return@jdbcConnection Person(
                    id = rs.getObject("id", UUID::class.java),
                    apelido = rs.getString("apelido"),
                    nome = rs.getString("nome"),
                    nascimento = rs.getDate("nascimento").toLocalDate(),
                    stack = rs.getArray("stack")?.let { stack ->
                        (stack.array as Array<String>).toList()
                    }
                )
            }
            return@jdbcConnection null
        }
    }
    override suspend fun delete(id: UUID) {
        TODO("Not yet implemented")
    }

    override suspend fun update(person: Person): Person {
        TODO("Not yet implemented")
    }
}