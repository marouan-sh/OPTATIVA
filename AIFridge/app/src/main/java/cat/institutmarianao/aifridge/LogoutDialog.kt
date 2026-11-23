package cat.institutmarianao.aifridge

import android.app.Activity
import android.content.Intent
import cat.institutmarianao.aifridge.data.AuthRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Diálogo de "cerrar sesión" compartido por fridge_Activity y Recipe_Activity.
 * Pide confirmación y, si el usuario acepta, vuelve al login borrando el historial de pantallas.
 */
object LogoutDialog {

    fun show(activity: Activity, authRepository: AuthRepository) {
        MaterialAlertDialogBuilder(activity)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK") { _, _ ->
                authRepository.logout()
                val intent = Intent(activity, login_Activity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                activity.startActivity(intent)
            }
            .show()
    }
}
