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

    /** Hay una sesión iniciada. */
    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun login(email: String, password: String, onResult: (Result<FirebaseUser>) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    onResult(Result.success(user))
                } else {
                    onResult(Result.failure(Exception("Error on login")))
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Login failed", exception)
                onResult(Result.failure(exception))
            }
    }

    /** Crea la cuenta. Firebase inicia la sesión automáticamente al crearla. */
    fun register(email: String, password: String, onResult: (Result<FirebaseUser>) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    onResult(Result.success(user))
                } else {
                    onResult(Result.failure(Exception("Error on register")))
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
    }
}
