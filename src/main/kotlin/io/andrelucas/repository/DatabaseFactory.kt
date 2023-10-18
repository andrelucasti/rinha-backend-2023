package io.andrelucas.repository

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import io.andrelucas.app.LOGGER
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.util.concurrent.Executors

object DataBaseFactory {

    //val database: Database = Database.connect(DATABASE_URL, DRIVE_CLASS_NAME, "person", "person")

    private val dataSource = createHikariDataSource()

    private val databaseThreadPool = Executors.newCachedThreadPool()
        .asCoroutineDispatcher()

    private val maxThreadSimult = Semaphore(dataSource.maximumPoolSize * 4)

    suspend fun <T> jdbcConnection(block: (Connection) -> (T)) = maxThreadSimult.withPermit {
        withContext(databaseThreadPool) {
            dataSource.connection.use { connection ->
                LOGGER.info("Thread: ${Thread.currentThread().name}- Opening Connection")

                return@use block(connection)
            }
        }

    }

    private fun createHikariDataSource() = HikariDataSource(
        HikariConfig().apply {
            this.jdbcUrl = "jdbc:postgresql://localhost:5432/person"
            this.username = "person"
            this.password = "person"
            this.driverClassName = "org.postgresql.Driver"
            this.minimumIdle = 15
            this.isAutoCommit = true
            this.maximumPoolSize = 150
            this.keepaliveTime = 150
            this.transactionIsolation = IsolationLevel.TRANSACTION_REPEATABLE_READ.name
            this.addDataSourceProperty("reWriteBatchedInserts", "true")
            this.poolName = "PersonPool"
        }
    )


}

suspend fun createTable() {
    DataBaseFactory.jdbcConnection { connection ->
        connection.createStatement().use { statement ->
            statement.execute("CREATE TABLE IF NOT EXISTS person (id UUID PRIMARY KEY, apelido VARCHAR(32) NOT NULL, nome VARCHAR(100) NOT NULL, nascimento DATE NOT NULL, stack TEXT[] NULL, search TEXT NOT NULL)")
            statement.execute("CREATE INDEX IF NOT EXISTS idx_apelido ON person (apelido)")
            statement.execute("CREATE EXTENSION IF NOT EXISTS  pg_trgm")
            statement.execute("CREATE INDEX IF NOT EXISTS idx_search_gist ON person USING GIST (search GIST_TRGM_OPS)")
        }
    }
}