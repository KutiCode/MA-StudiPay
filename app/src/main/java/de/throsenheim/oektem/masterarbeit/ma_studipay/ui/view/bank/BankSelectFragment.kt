package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.bank


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepositoryImpl
import de.throsenheim.oektem.masterarbeit.ma_studipay.model.Bank
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.viewmodel.BankSelectViewModel
import de.throsenheim.oektem.masterarbeit.ma_studipay.ui.factory.BankSelectViewModelFactory

class BankSelectFragment : Fragment() {

    private lateinit var bankSelectViewModel: BankSelectViewModel
    private val predefinedBanks = listOf(
        Bank(name = "Top Giro Bank", bank_code = "TG12345"),
        Bank(name = "Sparkasse Rosenheim", bank_code = "SR67890"),
        Bank(name = "K-Classic Bank", bank_code = "KC54321"),
        Bank(name = "VR Bank Rosenheim-Chiemsee", bank_code = "VR98765"),
        Bank(name = "TH Rosenheimbank", bank_code = "TH11223")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_bank_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userRepositoryImpl = UserRepositoryImpl(
            userDao = AppDatabase.getDatabase(requireContext()).userDao(),
            apiService = RetrofitInstance.api,
            context = requireContext()
        )
        val factory = BankSelectViewModelFactory(requireContext(), userRepositoryImpl)
        bankSelectViewModel = ViewModelProvider(this, factory).get(BankSelectViewModel::class.java)

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

        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navController = findNavController()

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    val navOptions = NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_right)
                        .setExitAnim(R.anim.slide_out_left)
                        .setPopEnterAnim(R.anim.slide_in_left)
                        .setPopExitAnim(R.anim.slide_out_right)
                        .build()
                    navController.navigate(R.id.navigation_dashboard, null, navOptions)
                    true
                }

                R.id.navigation_home -> {
                    if (navController.currentDestination?.id != R.id.navigation_dashboard) {
                        val navOptions = NavOptions.Builder()
                            .setEnterAnim(R.anim.slide_in_right)
                            .setExitAnim(R.anim.slide_out_left)
                            .setPopEnterAnim(R.anim.slide_in_left)
                            .setPopExitAnim(R.anim.slide_out_right)
                            .build()
                        navController.navigate(R.id.navigation_dashboard, null, navOptions)
                    }
                    true
                }

                else -> false
            }
        }
    }
}