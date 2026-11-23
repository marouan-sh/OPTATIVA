package cat.institutmarianao.aifridge.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

/**
 * Única clase que habla con Cloud Firestore.
 * Trabaja siempre dentro de users/{userId}/ingredients, así cada usuario solo toca su nevera.
 */
class IngredientRepository(userId: String) {

    private val ingredientsCollection = FirebaseFirestore.getInstance()
        .collection("users")
        .document(userId)
        .collection("ingredients")

    /**
     * Escucha los ingredientes en tiempo real: onChange se llama al empezar y cada vez que
     * algo cambia en Firestore. Devuelve el listener para poder pararlo (en onStop).
     */
    fun listenIngredients(onChange: (Result<List<item_Ingredient>>) -> Unit): ListenerRegistration =
        ingredientsCollection
            .orderBy("addedAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to ingredients", error)
                    onChange(Result.failure(error))
                    return@addSnapshotListener
                }
                val ingredients = snapshot?.toObjects(item_Ingredient::class.java) ?: emptyList()
                onChange(Result.success(ingredients))
            }

    fun addIngredient(ingredient: item_Ingredient, onResult: (Result<Unit>) -> Unit) {
        ingredientsCollection.add(ingredient)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error adding ingredient", exception)
                onResult(Result.failure(exception))
            }
    }

    /** Actualiza los campos editables. addedAt no se toca: sigue siendo la fecha original. */
    fun updateIngredient(ingredient: item_Ingredient, onResult: (Result<Unit>) -> Unit) {
        val changes = mapOf(
            "name" to ingredient.name,
            "quantity" to ingredient.quantity,
            "unit" to ingredient.unit,
            "category" to ingredient.category,
            "expiration" to ingredient.expiration
        )
        ingredientsCollection.document(ingredient.id).update(changes)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating ingredient ${ingredient.id}", exception)
                onResult(Result.failure(exception))
            }
    }

    fun deleteIngredient(ingredientId: String, onResult: (Result<Unit>) -> Unit) {
        ingredientsCollection.document(ingredientId).delete()
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error deleting ingredient $ingredientId", exception)
                onResult(Result.failure(exception))
            }
    }

    companion object {
        private const val TAG = "IngredientRepository"
    }
}
