package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Animierter Kreis
        binding.animatedCircle.animate()
            .scaleX(30f) // Vergrößert den Kreis
            .scaleY(30f)
            .setDuration(1000)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                // Zeige Text, Untertitel und Button nach der Animation
                binding.welcomeText.visibility = View.VISIBLE
                binding.welcomeSubtitle.visibility = View.VISIBLE
                binding.startButton.visibility = View.VISIBLE

                // Animation für Begrüßungstext
                binding.welcomeText.alpha = 0f
                binding.welcomeText.animate().alpha(1f).setDuration(500).start()

                // Animation für Untertitel
                binding.welcomeSubtitle.alpha = 0f
                binding.welcomeSubtitle.animate().alpha(1f).setDuration(500).setStartDelay(200).start()

                // Animation für Button
                binding.startButton.alpha = 0f
                binding.startButton.animate().alpha(1f).setDuration(500).setStartDelay(400).start()
            }
            .start()

        // Navigation zur Login-Seite
        binding.startButton.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeFragment_to_loginFragment)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
