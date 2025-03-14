package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.login.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentLoginBinding
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.login.viewmodel.LoginViewModel
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.login.viewmodel.LoginViewModelFactory

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LoginViewModel

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

        // Stelle sicher, dass ViewModel hier initialisiert wird
        val userRepository = UserRepository(
            userDao = AppDatabase.getDatabase(requireContext()).userDao(),
            apiService = RetrofitInstance.api,
            context = requireContext()
        )

        val viewModelFactory = LoginViewModelFactory(requireActivity().application, userRepository)
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]

        // Setze Observer und Listener nach der Initialisierung des ViewModels
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Login erfolgreich", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            showCustomMessage(error)
        }
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val matrikelnummer = binding.username.text.toString().trim()
            val password = binding.password.text.toString().trim()
            viewModel.login(matrikelnummer, password)
        }

        binding.registerButton.setOnClickListener {
            navigateWithSlideAnimation(R.id.registerFragment)
        }
    }

    private fun navigateWithSlideAnimation(destinationId: Int) {
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()

        findNavController().navigate(destinationId, null, navOptions)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun showCustomMessage(message: String) {
        // Inflatiere das benutzerdefinierte Layout
        val inflater = LayoutInflater.from(requireContext())
        val customView = inflater.inflate(R.layout.custom_message, binding.root, false)
        val messageText = customView.findViewById<TextView>(R.id.custom_message_text)
        messageText.text = message

        // Füge die View dem Root-Layout hinzu
        binding.root.addView(customView)

        // Sicherstellen, dass die View gemessen wurde, bevor die Animation startet
        customView.post {
            // Setze die Startposition (über dem sichtbaren Bereich)
            customView.translationY = -customView.height.toFloat()
            customView.visibility = View.VISIBLE

            // Blende die View mit einer Slide-in-Animation ein
            customView.animate()
                .translationY(0f)
                .setDuration(300)
                .withEndAction {
                    // Nach 2 Sekunden automatisch wieder ausblenden
                    customView.postDelayed({
                        customView.animate()
                            .translationY(-customView.height.toFloat())
                            .setDuration(300)
                            .withEndAction {
                                // Entferne die View aus dem Layout
                                binding.root.removeView(customView)
                            }
                            .start()
                    }, 2000)
                }
                .start()
        }
    }

}
