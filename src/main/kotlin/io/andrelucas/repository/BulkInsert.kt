package io.andrelucas.repository

import io.andrelucas.business.Person
import javax.sql.DataSource

class BulkInsert(private val dataSource: DataSource) {

    fun batchInsert(list: List<Person>){
        val connection = dataSource.connection
        val statement = connection.createStatement()
        list.forEach { person ->
            val sql = "INSERT INTO person (id, apelido, nome, nascimento, stack) VALUES ('${person.id}', '${person.apelido}', '${person.nome}', '${person.nascimento}', '${person.stack}')"
            statement.addBatch(sql)
        }
        statement.executeBatch()
        statement.clearBatch()
        statement.close()
        connection.close()
    }
}