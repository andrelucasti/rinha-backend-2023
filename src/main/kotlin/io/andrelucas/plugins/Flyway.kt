package io.andrelucas.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.ClassicConfiguration

fun Application.configureMigration() {
    ClassicConfiguration().run {
        setDataSource(
            createHikariDataSource()
        )
        setLocationsAsStrings("classpath:migration")

        Flyway.configure().configuration(this).load().migrate()
    }
}

fun Application.createHikariDataSource() = HikariDataSource(
    HikariConfig().apply {
        this.jdbcUrl = "jdbc:postgresql://db:5432/person"
        this.username = "person"
        this.password = "person"
        this.driverClassName = "org.postgresql.Driver"
        this.minimumIdle = 10
        this.maximumPoolSize = 20
        this.maxLifetime = 30000
        this.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }
)