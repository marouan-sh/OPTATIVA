package cat.institutmarianao.aifridge.data

import com.google.firebase.firestore.DocumentId
import java.util.Date

/**
 * Un ingrediente de la nevera, tal como se guarda en Firestore.
 *
 * - Los valores por defecto permiten a Firestore crear el objeto vacío al leer.
 * - Son "var" porque Firestore rellena los campos DESPUÉS de crear el objeto
 *   (con "val" no podría y llegarían vacíos).
 */
data class item_Ingredient(
    @DocumentId var id: String = "",
    var name: String = "",
    var quantity: Int = 0,
    var unit: String? = null,
    var category: String? = null,
    var expiration: Date? = null,
    var addedAt: Date = Date()
)
