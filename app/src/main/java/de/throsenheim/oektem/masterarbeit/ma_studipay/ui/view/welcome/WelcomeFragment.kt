package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.welcome

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

/**
 * Fragment that serves as the welcome screen.
 *
 * This fragment starts a circle animation. Once the animation finishes,
 * welcome texts and a start button are displayed, which navigates to the login screen when clicked.
 */
class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    // Reference to the animator, to allow cancelling it if necessary.
    private var circleAnimator: ValueAnimator? = null

    /**
     * Creates and returns the view hierarchy of the fragment.
     *
     * @param inflater The LayoutInflater used to inflate the layout.
     * @param container The parent ViewGroup, if available.
     * @param savedInstanceState A Bundle with previous state information, if available.
     * @return The created view of the fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called after the view hierarchy has been created.
     *
     * Starts the circle animation.
     *
     * @param view The created view of the fragment.
     * @param savedInstanceState A Bundle with previous state information, if available.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animateCircle()
    }

    /**
     * Starts a circle animation that animates the size of the circle from 100 pixels
     * to the maximum screen size. Once the animation ends, the welcome texts and the start button
     * are made visible. When the button is clicked, the app navigates to the login screen.
     */
    private fun animateCircle() {
        val circle = binding.animatedCircle
        val maxSize = resources.displayMetrics.widthPixels.coerceAtLeast(
            resources.displayMetrics.heightPixels
        )

        circleAnimator = ValueAnimator.ofFloat(100f, maxSize.toFloat()).apply {
            duration = 800
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                Log.d("WelcomeFragment", "Circle size: $value")
                circle.layoutParams.width = value.toInt()
                circle.layoutParams.height = value.toInt()
                circle.requestLayout()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    Log.d(
                        "WelcomeFragment",
                        "Animation ended, fragment active: ${isAdded && view != null}"
                    )
                    if (isAdded && view != null) {
                        // Set visibility of the elements
                        binding.welcomeText.visibility = View.VISIBLE
                        binding.welcomeSubtitle.visibility = View.VISIBLE
                        binding.startButton.visibility = View.VISIBLE

                        // Set click listener for the button
                        binding.startButton.setOnClickListener {
                            findNavController().navigate(R.id.action_welcomeFragment_to_loginFragment)
                        }
                    }
                }
            })
        }
        circleAnimator?.start()
    }

    /**
     * Called when the fragment's view is destroyed.
     *
     * Cancels any running animations and releases the binding.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("WelcomeFragment", "onDestroyView called, cancelling animation")
        circleAnimator?.cancel()
        _binding = null
    }
}
