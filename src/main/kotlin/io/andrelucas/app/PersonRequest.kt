package io.andrelucas.app

import io.andrelucas.business.Person
import kotlinx.serialization.Serializable

@Serializable
data class PersonRequest(val apelido: String?, val nome: String?, val nascimento: String, val stack: List<String>?)

fun PersonRequest.toPerson() = Person.create(apelido?: "", nome ?: "" , nascimento, stack ?: emptyList())