package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentDashboardBinding
import kotlinx.coroutines.launch
import androidx.activity.OnBackPressedCallback

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

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

        // Benutzernamen aus SharedPreferences laden
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val currentUsername = sharedPref.getString("current_username", null)

        if (currentUsername != null) {
            lifecycleScope.launch {
                val userDao = AppDatabase.getDatabase(requireContext()).userDao()
                val user = userDao.getUserByBenutzername(currentUsername)

                if (user != null) {
                    // Begrüßungstext mit Vorname aktualisieren
                    binding.welcomeText.text = "Hallo, ${user.vorname}"
                }
            }
        } else {
            // Fallback, falls kein Benutzer eingeloggt ist
            binding.welcomeText.text = "Hallo, Benutzer"
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Nichts passiert, wenn der Benutzer die Zurück-Taste drückt
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
