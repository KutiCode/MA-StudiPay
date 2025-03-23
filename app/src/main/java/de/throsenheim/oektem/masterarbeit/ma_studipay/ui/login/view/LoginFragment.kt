package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.login.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentLoginBinding
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.login.viewmodel.LoginViewModel
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.login.viewmodel.LoginViewModelFactory

/**
 * LoginFragment handles the user login UI.
 *
 * It initializes the LoginViewModel, sets up observers for login results and error messages,
 * and configures navigation based on user interactions.
 */
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

    /**
     * Called after the view has been created.
     *
     * Initializes the ViewModel, sets up observers and click listeners.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setupObservers()
        setupListeners()
    }

    /**
     * Initializes the LoginViewModel along with its dependencies.
     * Note: In a production app, consider using a DI framework like Hilt/Dagger.
     */
    private fun initViewModel() {
        val database = AppDatabase.getDatabase(requireContext())
        val userRepositoryImpl = UserRepositoryImpl(
            userDao = database.userDao(),
            apiService = RetrofitInstance.api,
            context = requireContext()
        )
        val viewModelFactory = LoginViewModelFactory(requireActivity().application, userRepositoryImpl)
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
    }

    /**
     * Sets up LiveData observers for login results and error messages.
     */
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

    /**
     * Sets up click listeners for the login and register buttons.
     */
    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val matriculationNumber = binding.username.text.toString().trim()
            val password = binding.password.text.toString().trim()
            viewModel.login(matriculationNumber, password)
        }

        binding.registerButton.setOnClickListener {
            navigateWithSlideAnimation(R.id.registerFragment)
        }
    }

    /**
     * Helper function to build navigation options with slide animations.
     *
     * @return [NavOptions] with predefined slide animations.
     */
    private fun buildSlideNavOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()
    }

    /**
     * Navigates to the specified destination using slide animations.
     *
     * @param destinationId The ID of the destination.
     */
    private fun navigateWithSlideAnimation(destinationId: Int) {
        findNavController().navigate(destinationId, null, buildSlideNavOptions())
    }

    /**
     * Displays a custom message using an inflated custom layout and slide animation.
     *
     * @param message The message to display.
     */
    private fun showCustomMessage(message: String) {
        // Inflate the custom layout
        val inflater = LayoutInflater.from(requireContext())
        val customView = inflater.inflate(R.layout.custom_message, binding.root, false)
        val messageText = customView.findViewById<TextView>(R.id.custom_message_text)
        messageText.text = message

        // Add the custom view to the root layout
        binding.root.addView(customView)

        // Wait until the view is laid out before starting the animation
        customView.post {
            // Set initial position (above the visible area)
            customView.translationY = -customView.height.toFloat()
            customView.visibility = View.VISIBLE

            // Slide in animation to display the view
            customView.animate()
                .translationY(0f)
                .setDuration(300)
                .withEndAction {
                    // Automatically slide out after 2 seconds
                    customView.postDelayed({
                        customView.animate()
                            .translationY(-customView.height.toFloat())
                            .setDuration(300)
                            .withEndAction {
                                // Remove the view from the layout
                                binding.root.removeView(customView)
                            }
                            .start()
                    }, 2000)
                }
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
