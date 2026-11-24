package cat.institutmarianao.aifridge

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cat.institutmarianao.aifridge.data.AuthRepository
import cat.institutmarianao.aifridge.data.DateUtils
import cat.institutmarianao.aifridge.data.FormValidator
import cat.institutmarianao.aifridge.data.IngredientRepository
import cat.institutmarianao.aifridge.data.RecipeRepository
import cat.institutmarianao.aifridge.data.item_Ingredient
import cat.institutmarianao.aifridge.databinding.ActivityFridgeBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import java.util.Date

class fridge_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityFridgeBinding
    private val authRepository = AuthRepository()
    private val recipeRepository = RecipeRepository()
    private lateinit var ingredientRepository: IngredientRepository
    private lateinit var adapter: IngredientAdapter

    /** Listener de Firestore activo (se para en onStop para no gastar recursos). */
    private var ingredientsListener: ListenerRegistration? = null

    /** null = estamos añadiendo; con valor = id del ingrediente que estamos editando. */
    private var editingIngredientId: String? = null

    /** Fecha elegida en el DatePicker (null = sin fecha de caducidad). */
    private var selectedExpiration: Date? = null

    /** Diálogo "Generating Recipe" mientras la IA responde (null = no se está generando). */
    private var progressDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFridgeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sin usuario no hay nevera: volvemos al login
        val user = authRepository.currentUser
        if (user == null) {
            goToLogin()
            return
        }
        ingredientRepository = IngredientRepository(user.uid)

        setSupportActionBar(binding.fridgeToolbar)
        setupSpinners()
        setupRecyclerView()
        setupForm()

        if (savedInstanceState != null) {
            restoreFormState(savedInstanceState)
        }
    }

    // ---------- Ciclo de vida: listener de Firestore ----------

    override fun onStart() {
        super.onStart()
        if (!::ingredientRepository.isInitialized) return

        // La lista se actualiza sola cada vez que algo cambia en Firestore
        ingredientsListener = ingredientRepository.listenIngredients { result ->
            result
                .onSuccess { ingredients -> adapter.submitList(ingredients) }
                .onFailure { showMessage("Error loading ingredients: ${it.message}") }
        }
    }

    override fun onStop() {
        super.onStop()
        ingredientsListener?.remove()
        ingredientsListener = null
    }

    // ---------- Rotación: guardar lo que Android no guarda solo ----------

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_EDITING_ID, editingIngredientId)
        selectedExpiration?.let { outState.putLong(KEY_EXPIRATION, it.time) }
    }

    private fun restoreFormState(state: Bundle) {
        editingIngredientId = state.getString(KEY_EDITING_ID)
        if (state.containsKey(KEY_EXPIRATION)) {
            selectedExpiration = Date(state.getLong(KEY_EXPIRATION))
        }
        updateFormMode()
    }

    // ---------- Menú de la toolbar ----------

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_fridge, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_generate_recipe -> {
            generateRecipe()
            true
        }
        R.id.action_logout -> {
            LogoutDialog.show(this, authRepository)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    /**
     * Botón ✨ del menú: pide a la IA una receta con los ingredientes de la nevera.
     *
     * Los ingredientes ya están en adapter.currentList (el listener de Firestore los mantiene
     * al día), así que no hace falta volver a pedirlos a Firestore.
     */
    private fun generateRecipe() {
        val ingredients = adapter.currentList
        if (ingredients.isEmpty()) {
            showMessage("Add some ingredients first")
            return
        }

        progressDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Generating Recipe")
            .setMessage("Please wait while Gemini generates a recipe suggestion for you...")
            .setCancelable(false)
            .show()

        // Corrutina: espera a la IA sin bloquear la pantalla.
        // Si se cierra la Activity, se cancela sola.
        lifecycleScope.launch {
            val result = recipeRepository.generateRecipe(ingredients)
            dismissProgressDialog()

            result
                .onSuccess { recipeText -> openRecipe(recipeText) }
                .onFailure { showMessage("Error generating recipe: ${it.message}") }
        }
    }

    private fun openRecipe(recipeText: String) {
        val intent = Intent(this, Recipe_Activity::class.java)
        intent.putExtra(Recipe_Activity.EXTRA_RECIPE, recipeText)
        startActivity(intent)
    }

    private fun dismissProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    override fun onDestroy() {
        dismissProgressDialog() // evita dejar el diálogo "colgado" si se gira la pantalla
        super.onDestroy()
    }

    // ---------- Spinners ----------

    private fun setupSpinners() {
        binding.unitSpinner.adapter = createSpinnerAdapter(UNITS)
        binding.categorySpinner.adapter = createSpinnerAdapter(CATEGORIES)
    }

    private fun createSpinnerAdapter(items: List<String>) =
        ArrayAdapter(this, android.R.layout.simple_spinner_item, items).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

    /** La posición 0 es "[Unit]" / "[Category]", que significa "sin valor" (null). */
    private fun selectedValue(spinner: Spinner, items: List<String>): String? {
        val position = spinner.selectedItemPosition
        return if (position > 0) items[position] else null
    }

    private fun selectValue(spinner: Spinner, items: List<String>, value: String?) {
        spinner.setSelection(items.indexOf(value).coerceAtLeast(0))
    }

    // ---------- Lista ----------

    private fun setupRecyclerView() {
        adapter = IngredientAdapter(
            onItemClick = { ingredient -> loadIngredientIntoForm(ingredient) },
            onDeleteClick = { ingredient -> confirmDelete(ingredient) }
        )
        binding.ingredientsRecyclerView.adapter = adapter
    }

    // ---------- Formulario ----------

    private fun setupForm() {
        binding.addButton.setOnClickListener { saveIngredient() }
        binding.clearButton.setOnClickListener { clearForm() }

        // Fecha: se elige con el calendario (el campo no se puede escribir a mano)
        binding.expirationEditText.setOnClickListener { showDatePicker() }
        binding.expirationLayout.setEndIconOnClickListener { showDatePicker() }

        // Si se giró la pantalla con el calendario abierto, se vuelve a conectar su botón OK
        findDatePicker()?.addOnPositiveButtonClickListener { millis -> setExpiration(Date(millis)) }
    }

    private fun saveIngredient() {
        val name = binding.nameEditText.text.toString().trim()
        val quantityText = binding.quantityEditText.text.toString().trim()

        // Errores de todos los campos a la vez
        val nameError = FormValidator.validateIngredientName(name)
        val quantityError = FormValidator.validateQuantity(quantityText)
        binding.nameLayout.error = nameError
        binding.quantityLayout.error = quantityError
        if (nameError != null || quantityError != null) return

        val ingredient = item_Ingredient(
            id = editingIngredientId ?: "",
            name = name,
            quantity = quantityText.toInt(),
            unit = selectedValue(binding.unitSpinner, UNITS),
            category = selectedValue(binding.categorySpinner, CATEGORIES),
            expiration = selectedExpiration
        )

        val onResult: (Result<Unit>) -> Unit = { result ->
            result.onFailure { showMessage("Error saving ingredient: ${it.message}") }
        }

        if (editingIngredientId == null) {
            ingredientRepository.addIngredient(ingredient, onResult)
        } else {
            ingredientRepository.updateIngredient(ingredient, onResult)
        }

        // No hace falta esperar a Firestore: el listener actualizará la lista solo
        clearForm()
    }

    /** Al pulsar una tarjeta: sus datos pasan al formulario y el botón cambia a "Update". */
    private fun loadIngredientIntoForm(ingredient: item_Ingredient) {
        editingIngredientId = ingredient.id
        binding.nameEditText.setText(ingredient.name)
        binding.quantityEditText.setText(ingredient.quantity.toString())
        selectValue(binding.unitSpinner, UNITS, ingredient.unit)
        selectValue(binding.categorySpinner, CATEGORIES, ingredient.category)
        setExpiration(ingredient.expiration)
        clearErrors()
        updateFormMode()
    }

    private fun clearForm() {
        editingIngredientId = null
        binding.nameEditText.setText("")
        binding.quantityEditText.setText("")
        binding.unitSpinner.setSelection(0)
        binding.categorySpinner.setSelection(0)
        setExpiration(null)
        clearErrors()
        updateFormMode()
        binding.nameEditText.clearFocus()
        binding.quantityEditText.clearFocus()
    }

    private fun clearErrors() {
        binding.nameLayout.error = null
        binding.quantityLayout.error = null
    }

    /** Texto del botón según si estamos añadiendo o editando. */
    private fun updateFormMode() {
        binding.addButton.text = if (editingIngredientId == null) "Add Ingredient" else "Update Ingredient"
    }

    // ---------- Fecha de caducidad ----------

    private fun showDatePicker() {
        if (findDatePicker() != null) return // ya está abierto

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Expiration date")
            .setSelection(selectedExpiration?.time ?: MaterialDatePicker.todayInUtcMilliseconds())
            .build()
        picker.addOnPositiveButtonClickListener { millis -> setExpiration(Date(millis)) }
        picker.show(supportFragmentManager, DATE_PICKER_TAG)
    }

    @Suppress("UNCHECKED_CAST")
    private fun findDatePicker(): MaterialDatePicker<Long>? =
        supportFragmentManager.findFragmentByTag(DATE_PICKER_TAG) as? MaterialDatePicker<Long>

    private fun setExpiration(date: Date?) {
        selectedExpiration = date
        binding.expirationEditText.setText(date?.let { DateUtils.format(it) } ?: "")
    }

    // ---------- Borrar ----------

    private fun confirmDelete(ingredient: item_Ingredient) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Ingredient")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage("Are you sure you want to delete '${ingredient.name}'?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK") { _, _ ->
                ingredientRepository.deleteIngredient(ingredient.id) { result ->
                    result.onFailure { showMessage("Error deleting ingredient: ${it.message}") }
                }
                // Si estábamos editando ese mismo ingrediente, se limpia el formulario
                if (editingIngredientId == ingredient.id) clearForm()
            }
            .show()
    }

    // ---------- Utilidades ----------

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun goToLogin() {
        val intent = Intent(this, login_Activity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        private val UNITS = listOf("[Unit]", "units", "grams", "kg", "liters", "ml", "packs")
        private val CATEGORIES = listOf(
            "[Category]", "Fruit & Vegs", "Meat & Fish", "Dairy & Eggs", "Bakery & Bread",
            "Grains & Pasta", "Sauces & Condiments", "Drinks", "Leftovers", "Other"
        )

        private const val DATE_PICKER_TAG = "expirationDatePicker"
        private const val KEY_EDITING_ID = "editingIngredientId"
        private const val KEY_EXPIRATION = "selectedExpiration"
    }
}
