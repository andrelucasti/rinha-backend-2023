package io.andrelucas.repository

import io.andrelucas.business.Person
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Serializable
data class PersonCache(val id: String, val apelido: String, val nome: String, val nascimento: String, val stack: List<String>)


fun Person.toPersonCache(): PersonCache {
  return PersonCache(
    id = id.toString(),
    apelido = apelido,
    nome = nome,
    nascimento = nascimento.format(DateTimeFormatter.ISO_DATE),
    stack = stack?: emptyList()
  )
}


fun PersonCache.toPerson(): Person {
  return Person(
    id = UUID.fromString(id),
    apelido = apelido,
    nome = nome,
    nascimento = LocalDate.parse(nascimento, DateTimeFormatter.ISO_DATE),
    stack = stack
  )
}

