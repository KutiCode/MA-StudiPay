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
            val vorname = binding.vornameInput.text.toString()
            val benutzername = binding.benutzernameInput.text.toString()
            val passwort = binding.passwortInput.text.toString()

            if (name.isNotEmpty() && vorname.isNotEmpty() && benutzername.isNotEmpty() && passwort.isNotEmpty()) {
                lifecycleScope.launch {
                    val userDao = AppDatabase.getDatabase(requireContext()).userDao()
                    userDao.insertUser(
                        User(
                            name = name,
                            vorname = vorname,
                            benutzername = benutzername,
                            passwort = passwort
                        )
                    )
                    Toast.makeText(requireContext(), "Registrierung erfolgreich!", Toast.LENGTH_SHORT).show()

                    // Zurück zur Login-Seite navigieren
                    findNavController().navigate(
                        R.id.action_registerFragment_to_loginFragment,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.registerFragment, true) // Entfernt RegisterFragment aus dem Back-Stack
                            .build()
                    )
                }
            } else {
                Toast.makeText(requireContext(), "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show()
            }
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
