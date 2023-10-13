package io.andrelucas.app

import io.andrelucas.business.Person
import kotlinx.serialization.Serializable
import java.time.format.DateTimeFormatter

@Serializable
data class PersonResponse(val id: String, val apelido: String, val nome: String, val nascimento: String, val stack: List<String>)

fun Person.toPersonResponse(): PersonResponse {
    return this.let {
        PersonResponse(
            id = it.id.toString(),
            apelido = it.apelido,
            nome = it.nome,
            nascimento = it.nascimento.format(DateTimeFormatter.ISO_LOCAL_DATE),
            stack = it.stack?: emptyList()
        )
    }
}