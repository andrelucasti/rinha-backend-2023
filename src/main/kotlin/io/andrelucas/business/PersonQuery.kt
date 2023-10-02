package io.andrelucas.business

interface PersonQuery {

   suspend fun count(): Long
   suspend fun personByName(name: String): Person?
}