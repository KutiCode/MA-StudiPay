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

/**
 * Fragment, das als Willkommen-Bildschirm dient.
 *
 * Hier wird eine Kreis-Animation gestartet. Nach Abschluss der Animation werden
 * Begrüßungstexte und ein Start-Button eingeblendet, mit dem zur Login-Seite navigiert wird.
 */
class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    // Referenz auf den Animator, um ihn bei Bedarf abbrechen zu können.
    private var circleAnimator: ValueAnimator? = null

    /**
     * Erzeugt und gibt die View-Hierarchie des Fragments zurück.
     *
     * @param inflater Der LayoutInflater, der zum Aufblasen des Layouts verwendet wird.
     * @param container Die übergeordnete ViewGroup, falls vorhanden.
     * @param savedInstanceState Ein Bundle mit vorherigen Zustandsinformationen, falls vorhanden.
     * @return Die erstellte View des Fragments.
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
     * Wird aufgerufen, nachdem die View-Hierarchie erstellt wurde.
     *
     * Hier wird die Kreis-Animation gestartet.
     *
     * @param view Die erstellte View des Fragments.
     * @param savedInstanceState Ein Bundle mit vorherigen Zustandsinformationen, falls vorhanden.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animateCircle()
    }

    /**
     * Startet eine Kreis-Animation, die die Größe des Kreises von 100 Pixel bis zur maximalen
     * Bildschirmgröße animiert. Am Ende der Animation werden die Begrüßungstexte und der Start-Button
     * eingeblendet. Beim Klick auf den Button wird zur Login-Seite navigiert.
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
                Log.d("WelcomeFragment", "Kreisgröße: $value")
                circle.layoutParams.width = value.toInt()
                circle.layoutParams.height = value.toInt()
                circle.requestLayout()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    Log.d(
                        "WelcomeFragment",
                        "Animation beendet, Fragment aktiv: ${isAdded && view != null}"
                    )
                    if (isAdded && view != null) {
                        // Sichtbarkeit der Elemente einstellen
                        binding.welcomeText.visibility = View.VISIBLE
                        binding.welcomeSubtitle.visibility = View.VISIBLE
                        binding.startButton.visibility = View.VISIBLE

                        // Klick-Listener für den Button setzen
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
     * Wird aufgerufen, wenn die View des Fragments zerstört wird.
     *
     * Hier werden laufende Animationen abgebrochen und das Binding freigegeben.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("WelcomeFragment", "onDestroyView aufgerufen, Animation abbrechen")
        circleAnimator?.cancel()
        _binding = null
    }
}
