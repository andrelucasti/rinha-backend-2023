package io.andrelucas.repository

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

@Serializable
data class PersonStack(val stack: List<String>) {

}

object PersonTable: Table() {
   val id = uuid("id")
    val apelido = varchar("apelido", 32).uniqueIndex()
    val nome = varchar("nome", 100)
    val nascimento = date("nascimento")
    val stack = varchar("stack", 1000)

    override val primaryKey = PrimaryKey(id)
}