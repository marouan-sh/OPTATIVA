package cat.institutmarianao.aifridge

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cat.institutmarianao.aifridge.data.AuthRepository
import cat.institutmarianao.aifridge.data.FormValidator
import cat.institutmarianao.aifridge.databinding.ActivityRegisterBinding

class register_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener { register() }
    }

    private fun register() {
        val email = binding.registerEmailEditText.text.toString().trim()
        val password = binding.registerPasswordEditText.text.toString()
        val confirmPassword = binding.confirmPasswordEditText.text.toString()

        if (!validateForm(email, password, confirmPassword)) return

        setLoading(true)
        binding.registerErrorTextView.visibility = View.GONE

        authRepository.register(email, password) { result ->
            setLoading(false)
            result
                .onSuccess { goToFridge() }
                .onFailure { showError(it.message ?: "Error on register") }
        }
    }

    /** Valida TODOS los campos a la vez. No borra lo que ha escrito el usuario. */
    private fun validateForm(email: String, password: String, confirmPassword: String): Boolean {
        val emailError = FormValidator.validateEmail(email)
        val passwordError = FormValidator.validateNewPassword(password)
        val confirmError = FormValidator.validatePasswordConfirmation(password, confirmPassword)

        binding.registerEmailLayout.error = emailError
        binding.registerPasswordLayout.error = passwordError
        binding.confirmPasswordLayout.error = confirmError

        return emailError == null && passwordError == null && confirmError == null
    }

    private fun showError(message: String) {
        binding.registerErrorTextView.text = message
        binding.registerErrorTextView.visibility = View.VISIBLE
    }

    private fun setLoading(isLoading: Boolean) {
        binding.registerButton.isEnabled = !isLoading
    }

    private fun goToFridge() {
        val intent = Intent(this, fridge_Activity::class.java)
        // Borra el login y el registro del historial: al pulsar "atrás" en la nevera, la app se cierra
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
