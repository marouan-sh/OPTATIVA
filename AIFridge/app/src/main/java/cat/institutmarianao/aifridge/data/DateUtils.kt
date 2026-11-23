package cat.institutmarianao.aifridge.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Formato de las fechas de caducidad (dd/MM/yyyy).
 *
 * El DatePicker de Material devuelve la fecha a las 00:00 en UTC, por eso se formatea
 * también en UTC: así siempre se muestra el mismo día que eligió el usuario.
 */
object DateUtils {

    // Se crea una sola vez y se reutiliza (no un formateador nuevo por cada ingrediente de la lista)
    private val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun format(date: Date): String = formatter.format(date)
}
