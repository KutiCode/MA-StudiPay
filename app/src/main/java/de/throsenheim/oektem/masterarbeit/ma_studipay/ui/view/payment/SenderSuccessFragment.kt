package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.view.payment

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.payment.token.TransactionStatusHolder
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.utility.NavigationHelper

// Fragment shown when the sender's payment process has been successful.
class SenderSuccessFragment : Fragment() {

    // Inflate the layout for the SenderSuccessFragment.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sender_success, container, false)
    }

    // Called after the view is created; sets up motion animation and navigation actions.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the MotionLayout which handles the animation for this fragment.
        val motionLayout = view.findViewById<MotionLayout>(R.id.motionLayout)
        // Delay a short period (300ms) before transitioning the MotionLayout to its end state.
        motionLayout.postDelayed({
            motionLayout.transitionToEnd()
        }, 300)
        // Play a notification sound to indicate success.
        playSinglePing()
        // Find the dashboard button within the layout.
        val dashboardButton = view.findViewById<MaterialButton>(R.id.dashboardButton)
        // Set click listener on the dashboard button.
        dashboardButton.setOnClickListener {
            // Reset the TransactionStatusHolder to clear previous transaction status.
            TransactionStatusHolder.reset()
            // Build fade navigation options via the NavigationHelper.
            val navOptions = NavigationHelper.buildFadeNavOptions()
            // Navigate to the dashboard fragment using the provided navigation options.
            findNavController().navigate(R.id.dashboardFragment, null, navOptions)
        }
    }

    private fun playSinglePing() {
        val mp: MediaPlayer = MediaPlayer.create(context, R.raw.coin_win_notification)
        mp.start()
    }
}
