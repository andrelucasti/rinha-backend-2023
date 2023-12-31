package io.andrelucas.business

interface PersonQuery {

   suspend fun count(): Long
   suspend fun personByTerm(term: String): List<Person>
   suspend fun exists(apelido: String): Boolean
}