package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.RetrofitInstance
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialisiere UserRepository mit allen erforderlichen Parametern
        userRepository = UserRepository(
            userDao = AppDatabase.getDatabase(requireContext()).userDao(),
            syncQueueDao = AppDatabase.getDatabase(requireContext()).syncQueueDao(),
            apiService = RetrofitInstance.api // Übergib die API-Instanz
        )

        val usernameEditText = view.findViewById<EditText>(R.id.username)
        val passwordEditText = view.findViewById<EditText>(R.id.password)
        val loginButton = view.findViewById<Button>(R.id.loginButton)
        val registerButton = view.findViewById<Button>(R.id.register_button)

        loginButton.setOnClickListener {
            val matrikelnummer = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (matrikelnummer.isNotEmpty() && password.isNotEmpty()) {
                // Login-Logik in einer Coroutine
                viewLifecycleOwner.lifecycleScope.launch {
                    val user = userRepository.getUserByMatrikelnumber(matrikelnummer)

                    if (user != null && user.password == password) {
                        // Benutzer erfolgreich authentifiziert
                        saveUserSession(matrikelnummer)
                        Toast.makeText(requireContext(), "Login erfolgreich", Toast.LENGTH_SHORT).show()

                        // Navigiere zum Dashboard
                        findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
                    } else {
                        // Fehlerhafte Login-Daten
                        Toast.makeText(requireContext(), "Ungültige Login-Daten", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show()
            }
        }

            registerButton.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
                .build()
            findNavController().navigate(R.id.registerFragment, null, navOptions)
        }
    }

    // Speichert den Benutzer in SharedPreferences
    private fun saveUserSession(username: String) {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("current_username", username)
            putBoolean("is_logged_in", true)
            apply()
        }
    }
}
