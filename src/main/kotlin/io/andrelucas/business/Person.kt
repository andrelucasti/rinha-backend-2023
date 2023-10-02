package io.andrelucas.business

import java.time.LocalDate
import java.util.UUID

data class Person(val id: UUID,
                  val apelido: String,
                  val nome: String,
                  val nascimento: LocalDate,
                  val stack: List<String>) {

    companion object {
        fun create(apelido: String,
                   nome: String,
                   nascimento: LocalDate,
                   stack: List<String>): Person {

            validateNumericValues(apelido, nome, stack)
            validateRequestedField(apelido, nome)

            return Person(UUID.randomUUID(), apelido, nome, nascimento, stack)
        }

        private fun validateRequestedField(apelido: String, nome: String) {
            if (apelido.isBlank()) {
                throw IllegalArgumentException("O campo apelido � obrigat�rio")
            }

            if (nome.isBlank()) {
                throw IllegalArgumentException("O campo nome � obrigat�rio")
            }
        }

        private fun validateNumericValues(apelido: String, nome: String, stack: List<String>) {
            if (isNumeric(apelido)) {
                throw NumericException("O campo apelido n�o pode ser apenas num�rico")
            }

            if (isNumeric(nome)) {
                throw NumericException("O campo nome n�o pode ser apenas num�rico")
            }

            stack.forEach {
                if (isNumeric(it)) {
                    throw NumericException("O campo stack n�o pode conter apenas valores num�ricos")
                }
            }
        }

        private fun isNumeric(str: String): Boolean {
            return str.matches("-?\\d+(\\.\\d+)?".toRegex())
        }
    }
}