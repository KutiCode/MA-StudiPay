package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.welcome

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        // Kreis-Animation starten
        animateCircle()
    }

    private fun animateCircle() {
        val circle = binding.animatedCircle
        val maxSize = resources.displayMetrics.widthPixels.coerceAtLeast(resources.displayMetrics.heightPixels)

        val animator = ValueAnimator.ofFloat(100f, maxSize.toFloat())
        animator.duration = 800
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            Log.d("WelcomeFragment", "Kreisgröße: $value")
            circle.layoutParams.width = value.toInt()
            circle.layoutParams.height = value.toInt()
            circle.requestLayout()
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                Log.d("WelcomeFragment", "Animation beendet, Fragment aktiv: ${isAdded && view != null}")
                if (isAdded && view != null) {
                    // Sichtbarkeit der Elemente einstellen
                    binding.welcomeText.visibility = View.VISIBLE
                    binding.welcomeSubtitle.visibility = View.VISIBLE
                    binding.startButton.visibility = View.VISIBLE

                    // Klick-Listener für den Button
                    binding.startButton.setOnClickListener {
                        findNavController().navigate(R.id.action_welcomeFragment_to_loginFragment)
                    }
                }
            }
        })
        animator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("WelcomeFragment", "onDestroyView aufgerufen, Animation abbrechen")
        binding.animatedCircle.animate().cancel() // Animation abbrechen
        _binding = null // Binding aufräumen
    }
}
