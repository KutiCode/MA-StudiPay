package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.payment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.throsenheim.oektem.masterarbeit.ma_studipay.R

class SenderSuccessFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Hier wird dein Layout mit dem MotionLayout inflatiert (z. B. fragment_sender_success.xml)
        return inflater.inflate(R.layout.fragment_sender_success, container, false)
    }
}
