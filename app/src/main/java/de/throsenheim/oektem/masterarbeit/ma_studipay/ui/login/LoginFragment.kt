package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.login

import android.content.Context
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
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentLoginBinding
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.RetrofitInstance
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var userRepository: UserRepository
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UserRepository
        userRepository = UserRepository(
            userDao = AppDatabase.getDatabase(requireContext()).userDao(),
            syncQueueDao = AppDatabase.getDatabase(requireContext()).syncQueueDao(),
            apiService = RetrofitInstance.api
        )

        setupListeners()
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val matrikelnummer = binding.username.text.toString().trim()
            val password = hashPassword(binding.password.text.toString().trim())

            if (matrikelnummer.isNotEmpty() && password.isNotEmpty()) {
                loginUser(matrikelnummer, password)
            } else {
                showToast("Bitte alle Felder ausfüllen")
            }
        }

        binding.registerButton.setOnClickListener {
            navigateToRegisterFragment()
        }
    }

    private fun loginUser(matrikelnummer: String, password: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val user = userRepository.getUserByMatrikelnumber(matrikelnummer)

            if (user != null && user.password == password) {
                saveUserSession(matrikelnummer)
                showToast("Login erfolgreich")
                navigateToDashboard()
            } else {
                showToast("Ungültige Login-Daten")
            }
        }
    }

    private fun navigateToRegisterFragment() {
        val navOptions = createNavOptions()
        findNavController().navigate(R.id.registerFragment, null, navOptions)
    }

    private fun navigateToDashboard() {
        findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
    }

    private fun saveUserSession(username: String) {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("current_username", username)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    private fun hashPassword(password: String): String {
        return password.hashCode().toString()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun createNavOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
