package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentRegisterBinding
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.register.RegisterViewModel
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.factory.RegisterFactory
import android.widget.TextView
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.NavigationHelper

// RegisterFragment handles user registration by collecting registration inputs,
// triggering registration via ViewModel and showing feedback messages or error messages.
class RegisterFragment : Fragment() {

    // ViewBinding instance to access UI elements defined in fragment_register.xml.
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    // ViewModel instance for registration logic.
    private lateinit var viewModel: RegisterViewModel

    // Inflate the layout with ViewBinding.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Called immediately after onCreateView; initialize ViewModel, observers and listeners.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create UserRepositoryImpl to handle user data access using the local database and Retrofit API.
        val userRepositoryImpl = UserRepositoryImpl(
            userDao = AppDatabase.getDatabase(requireContext()).userDao(),
            apiService = RetrofitInstance.api,
            context = requireContext()
        )
        // Instantiate the ViewModel using a custom factory that requires the repository.
        val viewModelFactory = RegisterFactory(userRepositoryImpl)
        viewModel = ViewModelProvider(this, viewModelFactory)[RegisterViewModel::class.java]

        // Setup observers to listen for registration result and error messages.
        setupObservers()
        // Setup listeners for button clicks for registration and back navigation.
        setupListeners()
    }

    // Observes the registration result and error message LiveData from the ViewModel.
    private fun setupObservers() {
        // Observe registration result; on success, show a toast and navigate to the login fragment.
        viewModel.registrationResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Registrierung erfolgreich", Toast.LENGTH_SHORT).show()
                // Build slide navigation options using NavigationHelper
                val navOptions = NavigationHelper.buildSlideNavOptions()
                // Navigate to the login fragment
                findNavController().navigate(R.id.loginFragment, null, navOptions)
            }
        }
        // Observe error message LiveData and display the custom message if an error occurs.
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            showCustomMessage(error)
        }
    }

    // Sets click listeners for registration and back-to-login actions.
    private fun setupListeners() {
        // On register button click, read input values and trigger registration through the ViewModel.
        binding.registerButton.setOnClickListener {
            val matriculationNumber = binding.matrikelnummerInput.text.toString().trim()
            val firstName = binding.vornameInput.text.toString().trim()
            val lastName = binding.nameInput.text.toString().trim()
            val password = binding.passwortInput.text.toString().trim()
            viewModel.registerUser(
                matriculationNumber,
                firstName,
                lastName,
                password
            )

        }
        // On back-to-login button click, navigate back to the login fragment using slide animation.
        binding.backToLoginButton.setOnClickListener{
            val navOptions = NavigationHelper.buildSlideNavOptions()
            findNavController().navigate(R.id.loginFragment, null, navOptions)
        }
    }

    // Clean up the ViewBinding reference when the view is destroyed to avoid memory leaks.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Displays a custom message using an animated view.
     *
     * Inflates a custom layout (custom_message.xml), sets the provided message,
     * and animates its appearance and disappearance.
     */
    private fun showCustomMessage(message: String) {
        val inflater = LayoutInflater.from(requireContext())
        // Inflate the custom message layout.
        val customView = inflater.inflate(R.layout.custom_message, binding.root, false)
        // Find the TextView within the custom layout and set the message.
        val messageText = customView.findViewById<TextView>(R.id.custom_message_text)
        messageText.text = message

        // Add the custom view to the root layout.
        binding.root.addView(customView)

        // Post animation actions to the custom view.
        customView.post {
            // Start with the view hidden (translated up out of view).
            customView.translationY = -customView.height.toFloat()
            customView.visibility = View.VISIBLE

            // Animate the view sliding in from the top.
            customView.animate()
                .translationY(0f)
                .setDuration(300)
                .withEndAction {
                    // After a delay, animate the view sliding out and remove it from the layout.
                    customView.postDelayed({
                        customView.animate()
                            .translationY(-customView.height.toFloat())
                            .setDuration(300)
                            .withEndAction {
                                binding.root.removeView(customView)
                            }
                            .start()
                    }, 2000)
                }
                .start()
        }
    }
}
