package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.bank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.Bank
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.NavigationHelper
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.bank.BankSelectViewModel
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.factory.BankSelectFactory

// Fragment for selecting a bank from a list of predefined banks.
// When a bank is selected, it is assigned to the current user and the fragment navigates back.
class BankSelectFragment : Fragment() {

    // ViewModel instance for assigning a bank to the current user.
    private lateinit var bankSelectViewModel: BankSelectViewModel

    // Predefined list of banks that the user can select from.
    private val predefinedBanks = listOf(
        Bank(name = "Top Giro Bank", bank_code = "TG12345"),
        Bank(name = "Sparkasse Rosenheim", bank_code = "SR67890"),
        Bank(name = "K-Classic Bank", bank_code = "KC54321"),
        Bank(name = "VR Bank Rosenheim-Chiemsee", bank_code = "VR98765"),
        Bank(name = "TH Rosenheimbank", bank_code = "TH11223")
    )

    // Inflate the fragment's layout from fragment_bank_select.xml.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_bank_select, container, false)
    }

    // Setup view components, initialize the ViewModel, and configure button click listeners.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate UserRepositoryImpl with access to the local AppDatabase and Retrofit API.
        val userRepositoryImpl = UserRepositoryImpl(
            userDao = AppDatabase.getDatabase(requireContext()).userDao(),
            apiService = RetrofitInstance.api,
            context = requireContext()
        )

        // Create a BankSelectFactory that provides required dependencies and obtain the ViewModel.
        val factory = BankSelectFactory(requireContext(), userRepositoryImpl)
        bankSelectViewModel = ViewModelProvider(this, factory).get(BankSelectViewModel::class.java)

        // Set click listeners for bank selection buttons.
        // Each listener assigns the selected bank and pops the navigation back stack.
        view.findViewById<Button>(R.id.topGiroBankButton).setOnClickListener {
            bankSelectViewModel.assignBankToCurrentUser(predefinedBanks[0])
            findNavController().popBackStack()
        }
        view.findViewById<Button>(R.id.sparkasseButton).setOnClickListener {
            bankSelectViewModel.assignBankToCurrentUser(predefinedBanks[1])
            findNavController().popBackStack()
        }
        view.findViewById<Button>(R.id.kClassicBankButton).setOnClickListener {
            bankSelectViewModel.assignBankToCurrentUser(predefinedBanks[2])
            findNavController().popBackStack()
        }
        view.findViewById<Button>(R.id.vrBankButton).setOnClickListener {
            bankSelectViewModel.assignBankToCurrentUser(predefinedBanks[3])
            findNavController().popBackStack()
        }
        view.findViewById<Button>(R.id.thRosenheimBankButton).setOnClickListener {
            bankSelectViewModel.assignBankToCurrentUser(predefinedBanks[4])
            findNavController().popBackStack()
        }

        // Configure the bottom navigation using the NavigationHelper.
        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navController = findNavController()
        NavigationHelper.setupBottomNavigation(bottomNavigationView, navController)
    }
}
