package io.andrelucas.business

import java.time.LocalDate
import java.util.UUID

data class Person(val id: UUID,
                  val apelido: String,
                  val nome: String,
                  val nascimento: LocalDate,
                  val stack: List<String>? = emptyList()
) {

    companion object {
        fun create(apelido: String,
                   nome: String,
                   nascimento: String,
                   stack: List<String>): Person {

            validateNumericValues(apelido, nome, stack)
            validateRequestedField(apelido, nome)
            validateBirthDay(nascimento)

            if (apelido.length > 32) {
                throw IllegalArgumentException("O campo apelido não pode ter mais que 32 caracteres")
            }

            if (nome.length > 100) {
                throw IllegalArgumentException("O campo nome não pode ter mais que 100 caracteres")
            }


            return Person(UUID.randomUUID(), apelido, nome, LocalDate.parse(nascimento), stack)
        }

        private fun validateBirthDay(nascimento: String) {
            val nascimentoRegex = Regex("^(\\d{4})-(\\d{2})-(\\d{2})\$")
            if (!nascimentoRegex.matches(nascimento)) {
                throw IllegalArgumentException("O campo nascimento deve estar no formato yyyy-MM-dd")
            }

            try {
                LocalDate.parse(nascimento)
            } catch (e: Exception) {
                throw IllegalArgumentException("O campo nascimento estar com valores inválidos yyyy-MM-dd", e)
            }
        }

        private fun validateRequestedField(apelido: String, nome: String) {
            if (apelido.isBlank()) {
                throw IllegalArgumentException("O campo apelido é obrigatório")
            }

            if (nome.isBlank()) {
                throw IllegalArgumentException("O campo nome é obrigatório")
            }
        }

        private fun validateNumericValues(apelido: String, nome: String, stack: List<String>) {
            if (isNumeric(apelido)) {
                throw NumericException("O campo apelido não pode ser apenas numérico")
            }

            if (isNumeric(nome)) {
                throw NumericException("O campo nome não pode ser apenas numérico")
            }

            stack.forEach {
                if (isNumeric(it)) {
                    throw NumericException("O campo stack não pode conter apenas valores numéricos")
                }
            }
        }

        private fun isNumeric(str: String): Boolean {
            return str.matches("-?\\d+(\\.\\d+)?".toRegex())
        }
    }
}