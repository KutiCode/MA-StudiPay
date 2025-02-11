package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.dashboard.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.BankRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentDashboardBinding
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.dashboard.viewmodel.DashboardViewModel
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.dashboard.viewmodel.DashboardViewModelFactory

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userRepository = UserRepository(
            userDao = AppDatabase.getDatabase(requireContext()).userDao(),
            apiService = RetrofitInstance.api,
            context = requireContext()
        )
        val bankRepository = BankRepository(AppDatabase.getDatabase(requireContext()).bankDao())
        val viewModelFactory = DashboardViewModelFactory(bankRepository, userRepository)
        viewModel = ViewModelProvider(this, viewModelFactory)[DashboardViewModel::class.java]

        setupObservers()
        setupListeners()
        loadUserData()
    }

    private fun setupObservers() {
        viewModel.userData.observe(viewLifecycleOwner) { uiState ->
            binding.welcomeText.text = "Hallo, ${uiState.firstName}"
            binding.cardDashboardBalance.text = "Dein Guthaben:"
            binding.balanceText.text = uiState.balance
            binding.matrikelnummerText.text = "Matrikelnummer: ${uiState.matrikelNumber}"
        }
    }

    private fun setupListeners() {
        // Navigation für Send-Button
        binding.sendButton.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_right)
                .setExitAnim(R.anim.slide_out_left)
                .setPopEnterAnim(R.anim.slide_in_left)
                .setPopExitAnim(R.anim.slide_out_right)
                .build()

            findNavController().navigate(
                R.id.action_UserInfoFragment_to_userPinEntryFragment,
                Bundle().apply {
                    putBoolean("isChangePin", false)
                    Log.d(
                        "DashboarFragment",
                        "Navigating to UserPinEntryFragment with NOT Change Pin"
                    )
                },
                navOptions
            )
        }

        // Navigation für Receive-Button
        binding.receiveButton.setOnClickListener {
            navigateWithSlideAnimation(R.id.userTransactionFragment)
        }

        // Navigation für Karten-Details
        binding.balanceCard.setOnClickListener {
            navigateWithFadeAnimation(R.id.orangeDetailsFragment)
        }

        // Bottom Navigation View
        val bottomNavigationView = binding.bottomNavigation
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_settings -> {
                    navigateWithSlideAnimation(R.id.navigation_settings)
                    true
                }
                R.id.navigation_home -> {
                    // Bleib auf dem Dashboard, keine Navigation erforderlich
                    true
                }
                else -> false
            }
        }
    }

    private fun loadUserData() {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val matrikelnummer = sharedPreferences.getString("current_username", null)
        matrikelnummer?.let {
            viewModel.loadUserData(it)
        } ?: run {
            binding.welcomeText.text = "Hallo, Gast"
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
    private fun navigateWithFadeAnimation(destinationId: Int) {
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.fade_out)
            .build()

        findNavController().navigate(destinationId, null, navOptions)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
