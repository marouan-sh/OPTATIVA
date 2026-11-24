package cat.institutmarianao.aifridge.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlin.coroutines.cancellation.CancellationException

/**
 * Única clase que habla con la IA de Firebase (Firebase AI Logic · Gemini Developer API).
 */
class RecipeRepository {

    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(MODEL_NAME)

    /**
     * Pide a la IA una receta con los ingredientes. Devuelve el texto en Markdown.
     *
     * Es "suspend": espera la respuesta SIN bloquear la pantalla (no usa el hilo principal).
     */
    suspend fun generateRecipe(ingredients: List<item_Ingredient>): Result<String> {
        return try {
            val response = model.generateContent(buildPrompt(ingredients))
            val text = response.text
            if (text.isNullOrBlank()) {
                Result.failure(Exception("The AI returned an empty recipe"))
            } else {
                Result.success(text)
            }
        } catch (e: CancellationException) {
            throw e // se ha cerrado la pantalla: no es un error, se deja cancelar
        } catch (e: Exception) {
            Log.e(TAG, "Error generating recipe", e)
            Result.failure(e)
        }
    }

    /** El prompt del enunciado, con una línea por ingrediente: "- 12 units Eggs". */
    private fun buildPrompt(ingredients: List<item_Ingredient>): String {
        val ingredientList = ingredients.joinToString("\n") { ingredient ->
            listOfNotNull("-", ingredient.quantity.toString(), ingredient.unit, ingredient.name)
                .joinToString(" ")
        }
        return """
            |Suggest a simple recipe I can make with the following ingredients:
            |$ingredientList
            |Please format the response using simple Markdown:
            |- Use a main heading for the recipe **Title**.
            |- Use subheadings like **Ingredients** and **Instructions** in bold.
            |- Use bullet points for the ingredients list.
            |- You can make key preparation steps **bold** as well.
        """.trimMargin()
    }

    companion object {
        private const val TAG = "RecipeRepository"
        private const val MODEL_NAME = "gemini-3.5-flash"
    }
}
