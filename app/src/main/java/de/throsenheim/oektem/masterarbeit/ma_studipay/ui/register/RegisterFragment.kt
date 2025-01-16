package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.User
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentRegisterBinding
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.RetrofitInstance
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val apiService = RetrofitInstance.api
    private val binding get() = _binding!!
    private val userRepository by lazy {
        UserRepository(
            AppDatabase.getDatabase(requireContext()).userDao(),
            AppDatabase.getDatabase(requireContext()).syncQueueDao(),
            apiService
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Registrieren-Button Logik
        binding.registerButton.setOnClickListener {
            val name = binding.nameInput.text.toString()
            val firstName = binding.vornameInput.text.toString()
            val password = binding.passwortInput.text.toString()
            val matrikelnumber = binding.matrikelnummerInput.text.toString()

            if (name.isNotEmpty() && firstName.isNotEmpty() && matrikelnumber.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    val kontonummer = generateUniqueKontonummer()

                    // Nutzer lokal registrieren
                    val user = User(
                        matrikelnumber = matrikelnumber,
                        lastName = name,
                        firstName = firstName,
                        password = hashPassword(password),
                        accountNumber = kontonummer,
                        balance = 0.0
                    )

                    try {
                        userRepository.registerUserLocally(user)
                        Toast.makeText(
                            requireContext(),
                            "Nutzer lokal registriert. Synchronisation ausstehend.",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Zurück zur Login-Seite navigieren
                        findNavController().navigate(
                            R.id.action_registerFragment_to_loginFragment,
                            null,
                            NavOptions.Builder()
                                .setPopUpTo(R.id.registerFragment, true)
                                .build()
                        )
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "Fehler bei der Registrierung: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show()
            }
        }

        // Zurück-Button Logik
        binding.backToLoginButton.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
                .build()
            findNavController().navigate(R.id.loginFragment, null, navOptions)
        }
    }

    private suspend fun generateUniqueKontonummer(): String {
        var kontonummer: String
        do {
            kontonummer = (100000..999999).random().toString()
        } while (userRepository.userDao.getAllUsers().any { it.accountNumber == kontonummer })
        return kontonummer
    }

    private fun hashPassword(password: String): String {
        // Hash-Funktion für das Passwort (z. B. SHA-256 oder bcrypt)
        return password.hashCode()
            .toString() // Dummy-Hash (ersetze mit einer richtigen Hash-Funktion)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
