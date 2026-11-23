package cat.institutmarianao.aifridge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cat.institutmarianao.aifridge.data.DateUtils
import cat.institutmarianao.aifridge.data.item_Ingredient
import cat.institutmarianao.aifridge.databinding.ItemIngredientBinding

/**
 * Pinta cada ingrediente de la lista usando item_ingredient.xml.
 *
 * Es un ListAdapter: cuando llega una lista nueva (submitList), compara con la anterior
 * y solo vuelve a pintar las tarjetas que han cambiado.
 *
 * @param onItemClick   qué hacer al pulsar una tarjeta (cargarla en el formulario)
 * @param onDeleteClick qué hacer al pulsar la papelera (pedir confirmación)
 */
class IngredientAdapter(
    private val onItemClick: (item_Ingredient) -> Unit,
    private val onDeleteClick: (item_Ingredient) -> Unit
) : ListAdapter<item_Ingredient, IngredientAdapter.IngredientViewHolder>(IngredientDiffCallback) {

    inner class IngredientViewHolder(
        private val binding: ItemIngredientBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ingredient: item_Ingredient) {
            binding.itemNameTextView.text = ingredient.name

            // "12 units", o solo "12" si no tiene unidad
            binding.itemQuantityTextView.text =
                listOfNotNull(ingredient.quantity.toString(), ingredient.unit).joinToString(" ")

            val expiration = ingredient.expiration
            if (expiration != null) {
                binding.itemExpirationTextView.text = DateUtils.format(expiration)
                binding.itemExpirationTextView.visibility = View.VISIBLE
            } else {
                binding.itemExpirationTextView.visibility = View.GONE
            }

            binding.ingredientCard.setOnClickListener { onItemClick(ingredient) }
            binding.deleteButton.setOnClickListener { onDeleteClick(ingredient) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val binding = ItemIngredientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /** Le dice al ListAdapter cómo comparar dos ingredientes. */
    private object IngredientDiffCallback : DiffUtil.ItemCallback<item_Ingredient>() {
        override fun areItemsTheSame(oldItem: item_Ingredient, newItem: item_Ingredient) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: item_Ingredient, newItem: item_Ingredient) =
            oldItem == newItem
    }
}
