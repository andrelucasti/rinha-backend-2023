package io.andrelucas.repository

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DataBaseFactory {
    private const val DRIVE_CLASS_NAME = "org.postgresql.Driver"
    private const val DATABASE_URL = "jdbc:db://localhost:5432/person"
    val database: Database = Database.connect(DATABASE_URL, DRIVE_CLASS_NAME, "person", "person")
    fun init() {
       // For now ... But, after these tests I'm gonna move this configuration to an application.properties file
        transaction(database) {
            if (PersonTable.exists().not()) {
                println("Creating table ${PersonTable.tableName}")
                SchemaUtils.create(PersonTable)
            } else {
                println("Table ${PersonTable.tableName} already exists")
            }
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}