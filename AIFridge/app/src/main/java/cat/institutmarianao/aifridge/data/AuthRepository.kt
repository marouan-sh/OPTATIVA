package cat.institutmarianao.aifridge.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * Única clase que habla con Firebase Authentication.
 * La usan login_Activity, register_Activity, fridge_Activity y Recipe_Activity.
 */
class AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /** Hay sesión iniciada y el email está verificado. */
    fun isLoggedIn(): Boolean = auth.currentUser?.isEmailVerified == true

    /**
     * Inicia sesión. Si el email todavía no está verificado, se cierra la sesión
     * y se devuelve un error para que el usuario lo verifique primero.
     */
    fun login(email: String, password: String, onResult: (Result<FirebaseUser>) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                when {
                    user == null -> onResult(Result.failure(Exception("Error on login")))
                    !user.isEmailVerified -> {
                        auth.signOut()
                        onResult(Result.failure(Exception(EMAIL_NOT_VERIFIED_MESSAGE)))
                    }
                    else -> onResult(Result.success(user))
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Login failed", exception)
                onResult(Result.failure(exception))
            }
    }

    /**
     * Crea la cuenta y envía el email de verificación.
     * Después cierra la sesión: el usuario tendrá que verificar el email y hacer login.
     */
    fun register(email: String, password: String, onResult: (Result<Unit>) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user == null) {
                    onResult(Result.failure(Exception("Error on register")))
                    return@addOnSuccessListener
                }
                user.sendEmailVerification()
                    .addOnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.w(TAG, "Verification email could not be sent", task.exception)
                        }
                        auth.signOut()
                        onResult(Result.success(Unit))
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Register failed", exception)
                onResult(Result.failure(exception))
            }
    }

    fun logout() {
        auth.signOut()
    }

    companion object {
        private const val TAG = "AuthRepository"
        const val EMAIL_NOT_VERIFIED_MESSAGE =
            "Please verify your email before logging in. Check your inbox."
    }
}
