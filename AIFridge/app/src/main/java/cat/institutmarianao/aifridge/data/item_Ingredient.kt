package cat.institutmarianao.aifridge.data

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class item_Ingredient(
    @DocumentId val id: String = "",
    val name: String = "",
    val quantity: Int = 0,
    val unit: String? = null,
    val category: String? = null,
    val expiration: Date? = null,
    val addedAt: Date = Date()
)