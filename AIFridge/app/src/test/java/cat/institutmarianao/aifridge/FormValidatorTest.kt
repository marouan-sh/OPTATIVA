package cat.institutmarianao.aifridge

import cat.institutmarianao.aifridge.data.DateUtils
import cat.institutmarianao.aifridge.data.FormValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

/**
 * Tests unitarios de las validaciones.
 * Se ejecutan en el ordenador, sin emulador: clic derecho en el archivo → Run 'FormValidatorTest'.
 */
class FormValidatorTest {

    // ---------- Email ----------

    @Test
    fun emptyEmail_isRequired() {
        assertEquals("Email is required", FormValidator.validateEmail(""))
        assertEquals("Email is required", FormValidator.validateEmail("   "))
    }

    @Test
    fun malformedEmail_isRejected() {
        assertEquals("The email address is badly formatted.", FormValidator.validateEmail("hola"))
        assertEquals("The email address is badly formatted.", FormValidator.validateEmail("hola@gmail"))
    }

    @Test
    fun validEmail_hasNoError() {
        assertNull(FormValidator.validateEmail("marouan@gmail.com"))
    }

    // ---------- Contraseñas ----------

    @Test
    fun loginPassword_isRequired() {
        assertEquals("Password is required", FormValidator.validateLoginPassword(""))
        assertNull(FormValidator.validateLoginPassword("x"))
    }

    @Test
    fun newPassword_needsSixCharacters() {
        assertEquals("Password should be at least 6 characters.", FormValidator.validateNewPassword("12345"))
        assertNull(FormValidator.validateNewPassword("123456"))
    }

    @Test
    fun passwordConfirmation_mustMatch() {
        assertEquals("Passwords do not match", FormValidator.validatePasswordConfirmation("abcdef", "abcdeg"))
        assertNull(FormValidator.validatePasswordConfirmation("abcdef", "abcdef"))
    }

    // ---------- Ingredientes ----------

    @Test
    fun ingredientName_isRequired() {
        assertEquals("Name is required", FormValidator.validateIngredientName(" "))
        assertNull(FormValidator.validateIngredientName("Eggs"))
    }

    @Test
    fun quantity_mustBePositiveNumber() {
        assertEquals("Quantity is required", FormValidator.validateQuantity(""))
        assertEquals("Quantity must be a positive number", FormValidator.validateQuantity("0"))
        assertEquals("Quantity must be a positive number", FormValidator.validateQuantity("-3"))
        assertEquals("Quantity must be a positive number", FormValidator.validateQuantity("abc"))
        assertNull(FormValidator.validateQuantity("12"))
    }

    // ---------- Fechas ----------

    @Test
    fun date_isFormattedAsDayMonthYear() {
        // 31/12/2025 a las 00:00 UTC (como la devuelve el DatePicker)
        val date = Date(1767139200000L)
        assertEquals("31/12/2025", DateUtils.format(date))
    }
}
