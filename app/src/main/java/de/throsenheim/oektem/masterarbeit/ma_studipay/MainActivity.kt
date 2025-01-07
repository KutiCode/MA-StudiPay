package de.throsenheim.oektem.masterarbeit.ma_studipay

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}