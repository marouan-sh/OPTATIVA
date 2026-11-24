package cat.institutmarianao.aifridge

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize

/**
 * Se ejecuta una sola vez al arrancar la app, antes que cualquier Activity.
 *
 * Configura Firebase App Check con el proveedor debug: cada llamada a la IA lleva un token
 * que demuestra que viene de esta app. El token debug de cada emulador se ve en Logcat
 * (DebugAppCheckProvider) y hay que registrarlo en la consola de Firebase (ver README).
 */
class AIFridgeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
    }
}
