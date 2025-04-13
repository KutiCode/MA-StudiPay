package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentLoginBinding
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.NavigationHelper
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.login.LoginViewModel
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.factory.LoginFactory

/**
 * LoginFragment handles the user login UI.
 *
 * It initializes the LoginViewModel, sets up observers for login results and error messages,
 * and configures navigation based on user interactions.
 */
class LoginFragment : Fragment() {

    // ViewBinding instance to safely access views defined in fragment_login.xml.
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // Instance of LoginViewModel to manage login logic and data.
    private lateinit var viewModel: LoginViewModel

    // Inflate the layout using view binding.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called after the view is created.
     *
     * Initializes the ViewModel, sets up observers and click listeners.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()      // Initialize the ViewModel with its required dependencies.
        setupObservers()     // Setup observers for LiveData from the ViewModel.
        setupListeners()     // Setup click listeners for login and register actions.
    }

    /**
     * Initializes the LoginViewModel along with its dependencies.
     * Note: In a production app, consider using a DI framework like Hilt/Dagger.
     */
    private fun initViewModel() {
        // Get an instance of the AppDatabase.
        val database = AppDatabase.getDatabase(requireContext())
        // Create a UserRepositoryImpl using the database's userDao and Retrofit service.
        val userRepositoryImpl = UserRepositoryImpl(
            userDao = database.userDao(),
            apiService = RetrofitInstance.api,
            context = requireContext()
        )
        // Create a LoginFactory to instantiate the LoginViewModel with required dependencies.
        val viewModelFactory = LoginFactory(requireActivity().application, userRepositoryImpl)
        // Retrieve the LoginViewModel instance.
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
    }

    /**
     * Sets up LiveData observers for login results and error messages.
     */
    private fun setupObservers() {
        // Observe loginResult LiveData to react when login is successful.
        viewModel.loginResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                // If login succeeds, display a success Toast message.
                Toast.makeText(requireContext(), "Login erfolgreich", Toast.LENGTH_SHORT).show()
                // Create slide navigation options for smooth transition.
                val navOptions = NavigationHelper.buildSlideNavOptions()
                // Navigate to the dashboard fragment.
                findNavController().navigate(
                    R.id.action_loginFragment_to_dashboardFragment,
                    null,
                    navOptions
                )
            }
        }

        // Observe errorMessage LiveData to display errors.
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            showCustomMessage(error)
        }
    }

    /**
     * Sets up click listeners for the login and register buttons.
     */
    private fun setupListeners() {
        // Login button: attempt to log in using provided credentials.
        binding.loginButton.setOnClickListener {
            // Retrieve and trim the input values.
            val matriculationNumber = binding.username.text.toString().trim()
            val password = binding.password.text.toString().trim()
            // Call the login function in the ViewModel.
            viewModel.login(requireContext(), matriculationNumber, password)
        }

        // Register button: navigate to the RegisterFragment.
        binding.registerButton.setOnClickListener {
            // Build slide navigation options.
            val navOptions = NavigationHelper.buildSlideNavOptions()
            // Navigate to the RegisterFragment.
            findNavController().navigate(
                R.id.action_loginFragment_to_registerFragment,
                null,
                navOptions
            )
        }
    }

    /**
     * Displays a custom message using an inflated custom layout and slide animation.
     *
     * @param message The message to display.
     */
    private fun showCustomMessage(message: String) {
        // Inflate the custom message layout.
        val inflater = LayoutInflater.from(requireContext())
        val customView = inflater.inflate(R.layout.custom_message, binding.root, false)
        // Find the TextView within the custom layout and set the message.
        val messageText = customView.findViewById<TextView>(R.id.custom_message_text)
        messageText.text = message

        // Add the custom view to the fragment's root layout.
        binding.root.addView(customView)

        // Wait for layout pass before starting the animation.
        customView.post {
            // Initialize the custom view off-screen (translated upward).
            customView.translationY = -customView.height.toFloat()
            customView.visibility = View.VISIBLE

            // Animate the view sliding in.
            customView.animate()
                .translationY(0f)
                .setDuration(300)
                .withEndAction {
                    // After 2 seconds, animate the view sliding out and remove it.
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

    // Clean up the binding when the view is destroyed to avoid memory leaks.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
