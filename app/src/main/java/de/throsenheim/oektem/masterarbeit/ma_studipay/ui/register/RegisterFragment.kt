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
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val userDao by lazy {
        AppDatabase.getDatabase(requireContext()).userDao()
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
            val username = binding.benutzernameInput.text.toString()
            val password = binding.passwortInput.text.toString()
            val matrikelnumber = binding.matrikelnummerInput.text.toString()

            if (name.isNotEmpty() && firstName.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    val kontonummer = generateUniqueKontonummer()

                    userDao.insertUser(
                        User(
                            lastName = name,
                            firstName = firstName,
                            username = username,
                            password = password,
                            accountNumber = kontonummer,
                            matrikelnumber = matrikelnumber,
                            balance = 0.0
                        )
                    )
                    Toast.makeText(requireContext(), "Registrierung erfolgreich!", Toast.LENGTH_SHORT).show()

                    // Zurück zur Login-Seite navigieren
                    findNavController().navigate(
                        R.id.action_registerFragment_to_loginFragment,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(R.id.registerFragment, true) // Entfernt RegisterFragment aus dem Back-Stack
                            .build()
                    )
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
        } while (userDao.countByKontonummer(kontonummer) > 0)
        return kontonummer
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
