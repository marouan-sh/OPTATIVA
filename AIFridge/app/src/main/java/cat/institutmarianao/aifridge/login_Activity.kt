package cat.institutmarianao.aifridge

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cat.institutmarianao.aifridge.data.AuthRepository
import cat.institutmarianao.aifridge.data.FormValidator
import cat.institutmarianao.aifridge.databinding.ActivityLoginBinding

class login_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Si ya hay sesión iniciada (y verificada), se salta el login
        if (authRepository.isLoggedIn()) {
            goToFridge()
            return
        }

        binding.loginButton.setOnClickListener { login() }

        binding.signUpButton.setOnClickListener {
            startActivity(Intent(this, register_Activity::class.java))
        }
    }

    private fun login() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()

        if (!validateForm(email, password)) return

        setLoading(true)
        binding.loginErrorTextView.visibility = View.GONE

        authRepository.login(email, password) { result ->
            setLoading(false)
            result
                .onSuccess { goToFridge() }
                .onFailure { showError(it.message ?: "Error on login") }
        }
    }

    /** Valida TODOS los campos a la vez. No borra lo que ha escrito el usuario. */
    private fun validateForm(email: String, password: String): Boolean {
        val emailError = FormValidator.validateEmail(email)
        val passwordError = FormValidator.validateLoginPassword(password)

        binding.emailLayout.error = emailError
        binding.passwordLayout.error = passwordError

        return emailError == null && passwordError == null
    }

    private fun showError(message: String) {
        binding.loginErrorTextView.text = message
        binding.loginErrorTextView.visibility = View.VISIBLE
    }

    /** Desactiva los botones mientras Firebase responde (evita dobles clics). */
    private fun setLoading(isLoading: Boolean) {
        binding.loginButton.isEnabled = !isLoading
        binding.signUpButton.isEnabled = !isLoading
    }

    private fun goToFridge() {
        startActivity(Intent(this, fridge_Activity::class.java))
        finish()
    }
}
