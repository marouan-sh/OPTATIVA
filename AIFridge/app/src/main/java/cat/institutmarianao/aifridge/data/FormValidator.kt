package cat.institutmarianao.aifridge.data

/**
 * Reglas de validación de todos los formularios de la app.
 * Cada función devuelve el mensaje de error, o null si el valor es correcto.
 *
 * Está separada de las Activities para poder reutilizarla y probarla con tests (FormValidatorTest).
 */
object FormValidator {

    const val MIN_PASSWORD_LENGTH = 6

    private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !EMAIL_REGEX.matches(email.trim()) -> "The email address is badly formatted."
        else -> null
    }

    /** Login: solo comprobamos que no esté vacía. */
    fun validateLoginPassword(password: String): String? =
        if (password.isEmpty()) "Password is required" else null

    /** Registro: Firebase exige un mínimo de 6 caracteres. */
    fun validateNewPassword(password: String): String? =
        if (password.length < MIN_PASSWORD_LENGTH) "Password should be at least 6 characters." else null

    fun validatePasswordConfirmation(password: String, confirmation: String): String? =
        if (password != confirmation) "Passwords do not match" else null

    fun validateIngredientName(name: String): String? =
        if (name.isBlank()) "Name is required" else null

    fun validateQuantity(quantity: String): String? {
        if (quantity.isBlank()) return "Quantity is required"
        val number = quantity.trim().toIntOrNull()
        return if (number == null || number <= 0) "Quantity must be a positive number" else null
    }
}
