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
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository
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

        userRepository = UserRepository(AppDatabase.getInstance(requireContext()).userDao())

        val usernameEditText = view.findViewById<EditText>(R.id.username)
        val passwordEditText = view.findViewById<EditText>(R.id.password)
        val loginButton = view.findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                // Login-Logik in einer Coroutine
                viewLifecycleOwner.lifecycleScope.launch {
                    val user = userRepository.getUserByBenutzername(username)

                    if (user != null && user.passwort == password) {
                        // Benutzer erfolgreich authentifiziert
                        saveUserSession(username)
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
