package de.throsenheim.oektem.masterarbeit.ma_studipay.ui.payment.view

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.throsenheim.oektem.masterarbeit.ma_studipay.R
import de.throsenheim.oektem.masterarbeit.ma_studipay.databinding.FragmentBeginningRecieveBinding
import java.util.Locale

class BeginningRecieveFragment : Fragment() {

    private var _binding: FragmentBeginningRecieveBinding? = null
    private val binding get() = _binding!!

    // NFC-Adapter-Instanz
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        _binding = FragmentBeginningRecieveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // NFC-Adapter abrufen
        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
        if (nfcAdapter == null) {
            // Gerät unterstützt kein NFC
            Toast.makeText(context, "Dieses Gerät unterstützt kein NFC.", Toast.LENGTH_LONG).show()
        }

        binding.cancelButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_dashboard)
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.let { adapter ->
            if (!adapter.isEnabled) {
                // NFC ist deaktiviert – Nutzer darauf hinweisen
                Toast.makeText(context, "Bitte aktivieren Sie NFC.", Toast.LENGTH_LONG).show()
                // Optional: Öffne die NFC-Einstellungen
                startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            } else {
                adapter.enableReaderMode(
                    requireActivity(),
                    { tag -> processTag(tag) },
                    NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    null
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Reader-Modus deaktivieren, wenn das Fragment nicht im Vordergrund ist
        nfcAdapter?.disableReaderMode(requireActivity())
    }

    /**
     * Diese Methode wird aufgerufen, sobald ein NFC-Tag erkannt wird.
     * Hier senden wir den SELECT-APDU-Befehl an den Sender (HCE-Karte) und
     * verarbeiten die Antwort, die INIT lauten soll.
     */
    private fun processTag(tag: Tag) {
        val isoDep = IsoDep.get(tag)
        isoDep?.let {
            try {
                it.connect()
                // SELECT-APDU-Befehl: Dieser Befehl wählt die Anwendung auf dem HCE-Sender aus.
                // In diesem Beispiel: "00A4040008A0000002471001"
                val selectApdu = hexStringToByteArray("00A4040008A0000002471001")
                // Sende den Befehl und empfange die Antwort
                val response = it.transceive(selectApdu)
                val responseHex = response.toHexString()
                Log.d("NFC", "Antwort vom Sender: $responseHex")
                // Wir erwarten, dass der Sender "INIT" (ASCII: 49 4E 49 54) zurücksendet,
                // gefolgt vom Statuswort 0x9000. Beispiel: "494E49549000"
                if (responseHex.startsWith("494E4954")) {
                    // INIT-Antwort empfangen
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "INIT-Antwort empfangen!",
                            Toast.LENGTH_LONG
                        ).show()
                        // Hier kannst du z.B. zum nächsten Screen navigieren oder weitere Logik ausführen
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "Unerwartete Antwort: $responseHex",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("NFC", "Fehler bei der NFC-Kommunikation: ${e.message}")
            } finally {
                try {
                    it.close()
                } catch (e: Exception) {
                    // Fehler beim Schließen ignorieren
                }
            }
        }
    }

    /**
     * Wandelt einen Hex-String (z. B. "00A4040008A0000002471001") in ein ByteArray um.
     */
    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4)
                    + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    /**
     * Erweiterungsfunktion, die ein ByteArray in einen Hex-String umwandelt.
     */
    private fun ByteArray.toHexString(): String {
        return joinToString(separator = "") { String.format(Locale.US, "%02X", it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
