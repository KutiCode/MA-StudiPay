package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import androidx.lifecycle.lifecycleScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

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
        // Animation für den Pfeil
        binding.arrow.translationY = -20f
        binding.arrow.animate()
            .translationY(0f)
            .setDuration(600)
            .setInterpolator(BounceInterpolator())
            .start()

        // Animation für die Eingabekarte
        binding.cardInputs.alpha = 0f
        binding.cardInputs.translationY = 50f
        binding.cardInputs.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setInterpolator(DecelerateInterpolator())
            .start()

        // Animation für den Login-Button
        binding.loginButton.alpha = 0f
        binding.loginButton.translationY = 50f
        binding.loginButton.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setStartDelay(300) // Startet nach der Eingabekarte
            .setInterpolator(DecelerateInterpolator())
            .start()

        binding.registerButton.alpha = 0f
        binding.registerButton.translationY = 50f
        binding.registerButton.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setStartDelay(800) // Startet nach der Eingabekarte
            .setInterpolator(DecelerateInterpolator())
            .start()
        binding.registerButton.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
                .build()

            findNavController().navigate(R.id.registerFragment, null, navOptions)
        }


        // Klick-Animation für den Login-Button
        binding.loginButton.setOnClickListener {
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .withEndAction {
                            performLogin()
                        }
                }
        }
    }

    private fun performLogin() {
        val username = binding.username.text.toString()
        val password = binding.password.text.toString()
        if (username.isNotEmpty() && password.isNotEmpty()) {
            lifecycleScope.launch {
                val userDao = AppDatabase.getDatabase(requireContext()).userDao()
                val user = userDao.getUserByBenutzername(username)

                if (user != null && user.passwort == password) {
                    // Benutzername speichern
                    val sharedPref = requireActivity().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("current_username", username)
                        apply()
                    }

                    Toast.makeText(requireContext(), "Login erfolgreich!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
                } else {
                    Toast.makeText(requireContext(), "Ungültige Login-Daten", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show()

            // Zum Debuggen die Eingaben ausgeben
            Log.d("LoginFragment", "Username: $username, Password: $password")
        }
    }
        private suspend fun checkLoginCredentials(username: String, password: String): Boolean {
            // Beispiel für Login-Überprüfung aus der Datenbank
            val userDao = AppDatabase.getDatabase(requireContext()).userDao()
            val user = userDao.getUserByBenutzername(username)
            return user != null && user.passwort == password
        }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
