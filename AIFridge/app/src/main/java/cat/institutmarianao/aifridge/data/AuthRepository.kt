package cat.institutmarianao.aifridge.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun login(email: String, password: String, onResult: (Result<FirebaseUser?>) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onResult(Result.success(it.user)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun register(email: String, password: String, onResult: (Result<FirebaseUser?>) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { onResult(Result.success(it.user)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun logout() {
        auth.signOut()
    }
}