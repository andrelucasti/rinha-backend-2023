package io.andrelucas.repository

import io.andrelucas.business.Person
import io.andrelucas.business.PersonQuery
import io.andrelucas.repository.DataBaseFactory.jdbcConnection
import java.util.*

object PersonQueryImpl : PersonQuery {
    override suspend fun count(): Long = jdbcConnection {
            it.prepareStatement("select count(*) from person").use { statement ->
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    resultSet.getLong(1)
                }
            }
        }

    override suspend fun personByTerm(term: String): List<Person> = jdbcConnection {
        //val ps = connection.prepareStatement("select id, apelido, nome, nascimento, stack from person where '${term}' <%  search ")
        val ps = it.prepareStatement("select id, apelido, nome, nascimento, stack from person where search ilike '%$term%'")
        ps.executeQuery().use { rs ->
            val list = mutableListOf<Person>()
            while (rs.next()) {
                list.add(Person(
                    id = UUID.fromString(rs.getString("id")),
                    apelido = rs.getString("apelido"),
                    nome = rs.getString("nome"),
                    nascimento = rs.getDate("nascimento").toLocalDate(),
                    stack = rs.getArray("stack")?.let { stack ->
                        (stack.array as Array<String>).toList()
                    }
                ))
            }
            list
        }
    }

    override suspend fun exists(apelido: String): Boolean {
        return jdbcConnection {
            val ps = it.prepareStatement("select count(*) from person where apelido = ?")
            ps.setString(1, apelido)
            ps.executeQuery().use { rs ->
                rs.next()
                rs.getLong(1) > 0
            }
        }
    }
}