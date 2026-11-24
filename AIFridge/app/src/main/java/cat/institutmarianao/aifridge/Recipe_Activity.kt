package cat.institutmarianao.aifridge

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import cat.institutmarianao.aifridge.data.AuthRepository
import cat.institutmarianao.aifridge.databinding.ActivityRecipeBinding
import io.noties.markwon.Markwon

/**
 * Muestra la receta generada por la IA de Firebase.
 * Recibe el texto en Markdown con el extra EXTRA_RECIPE y lo pinta con Markwon.
 */
class Recipe_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.recipeToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // flecha atrás

        val recipeText = intent.getStringExtra(EXTRA_RECIPE)
        if (recipeText.isNullOrBlank()) {
            binding.recipeTextView.text = "No recipe available."
        } else {
            Markwon.create(this).setMarkdown(binding.recipeTextView, recipeText)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_recipe, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish() // vuelve a la nevera tal como estaba
            true
        }
        R.id.action_logout -> {
            LogoutDialog.show(this, authRepository)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    companion object {
        /** Clave del extra con el texto de la receta (Markdown). */
        const val EXTRA_RECIPE = "extra_recipe"
    }
}
