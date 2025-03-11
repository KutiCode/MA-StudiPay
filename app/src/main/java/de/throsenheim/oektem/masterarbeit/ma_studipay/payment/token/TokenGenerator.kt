package de.throsenheim.oektem.masterarbeit.ma_studipay.payment.token


import android.content.Context
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.PaymentToken
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.RetrofitInstance
import java.text.SimpleDateFormat
import java.util.*

object TokenGenerator {

    /**
     * Generiert einen PaymentToken, indem alle nötigen Daten aus den SharedPreferences
     * und den Repository-Klassen abgerufen werden.
     *
     * Voraussetzungen:
     * - In den SharedPreferences unter "user_prefs" ist der Schlüssel "current_username" gesetzt.
     * - Der User ist in der lokalen DB vorhanden.
     * - Für die Bank, die dem User zugeordnet ist, existieren entsprechende BankSecrets.
     *
     * @param context Context, um auf SharedPreferences und die DB zuzugreifen.
     * @return Ein PaymentToken mit den abgerufenen Daten.
     */
    suspend fun generateToken(context: Context): PaymentToken {
        // 1. Aktuelle Matrikelnummer aus SharedPreferences abrufen
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val matrikelNumber = sharedPref.getString("current_username", null)
            ?: throw Exception("Keine Matrikelnummer in SharedPreferences gefunden")

        // 2. User-Daten abrufen
        val userApi = RetrofitInstance.api
        val userDao = AppDatabase.getDatabase(context).userDao()
        val userRepository = UserRepository(
            userDao,
            userApi,
            context
        ) // Falls API nicht benötigt, übergib null oder passe an.
        val user = userRepository.getUserByMatrikelnumber(matrikelNumber)
            ?: throw Exception("User mit Matrikelnummer $matrikelNumber nicht gefunden")

        // 3. Bank-Daten abrufen anhand des Bank-Codes des Users
        val bankDao = AppDatabase.getDatabase(context).bankDao()
        val bankRepository = BankRepository(bankDao)
        // Hier verwenden wir die Methode, die auch die BankSecrets liefert
        val bankWithSecrets = bankRepository.getBankWithSecrets(user.bank_code ?: "")
        // Wähle ein Secret aus; hier einfach das erste Element (du kannst auch eine Zufallsauswahl machen)
        val bankSecret = bankWithSecrets?.secrets?.firstOrNull()?.secretCode
            ?: throw Exception("Kein BankSecret für Bank-Code ${user.bank_code} gefunden")

        // 4. Aktuellen Zeitstempel erstellen (Datum und Uhrzeit)
        val currentDate = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateString = formatter.format(currentDate)

        // 5. Erstelle den PaymentToken mit den gesammelten Daten
        return PaymentToken(
            firstName = user.firstName,
            lastName = user.lastName,
            accountNumber = user.accountNumber,
            bankCode = user.bank_code ?: "",
            bankSecret = bankSecret,
            date = dateString
        )
    }
}
