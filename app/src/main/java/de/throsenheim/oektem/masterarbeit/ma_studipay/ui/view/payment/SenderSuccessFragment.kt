package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.payment.token.TransactionStatusHolder

class SenderSuccessFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Hier wird dein Layout mit dem MotionLayout inflatiert (z. B. fragment_sender_success.xml)
        return inflater.inflate(R.layout.fragment_sender_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val motionLayout = view.findViewById<MotionLayout>(R.id.motionLayout)
        // Starte den Übergang vom Start- zum Endzustand
        motionLayout.postDelayed({
            motionLayout.transitionToEnd()
        }, 300)

// Finde den Button über findViewById
        val dashboardButton = view.findViewById<MaterialButton>(R.id.dashboardButton)
        dashboardButton.setOnClickListener {
            TransactionStatusHolder.reset()
            findNavController().navigate(R.id.navigation_dashboard)
        }
    }

}
