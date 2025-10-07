package com.example.butterflydetector.ui.tutorial

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.butterflydetector.databinding.FragmentTutorialBinding
import com.google.android.material.button.MaterialButton
import java.util.*

class TutorialFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentTutorialBinding? = null
    private val binding get() = _binding!!

    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false
    private var isReading = false
    private var isPaused = false

    private lateinit var playButton: MaterialButton
    private lateinit var pauseButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val tutorialViewModel =
            ViewModelProvider(this).get(TutorialViewModel::class.java)

        _binding = FragmentTutorialBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textTutorial
        tutorialViewModel.text.observe(viewLifecycleOwner) { text ->
            textView.text = text
        }

        // Initialize TextToSpeech
        textToSpeech = TextToSpeech(requireContext(), this)

        // Setup buttons
        playButton = binding.playButton
        pauseButton = binding.pauseButton

        playButton.setOnClickListener {
            if (isTtsInitialized) {
                if (isPaused) {
                    resumeReading()
                } else {
                    startReading()
                }
            } else {
                Toast.makeText(requireContext(), "Text-to-Speech is initializing, please wait", Toast.LENGTH_SHORT).show()
            }
        }

        pauseButton.setOnClickListener {
            pauseReading()
        }

        // Initially disable pause button
        pauseButton.isEnabled = false

        return root
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.ENGLISH)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported")
                Toast.makeText(requireContext(), "Language not supported", Toast.LENGTH_SHORT).show()
                isTtsInitialized = false
            } else {
                isTtsInitialized = true
                Log.d("TTS", "TextToSpeech initialized successfully")

                // Set up utterance progress listener
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        activity?.runOnUiThread {
                            isReading = true
                            updateButtonStates()
                        }
                    }

                    override fun onDone(utteranceId: String?) {
                        activity?.runOnUiThread {
                            isReading = false
                            isPaused = false
                            updateButtonStates()
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        activity?.runOnUiThread {
                            isReading = false
                            isPaused = false
                            updateButtonStates()
                            Toast.makeText(requireContext(), "Error reading text", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
        } else {
            Log.e("TTS", "Initialization failed")
            Toast.makeText(requireContext(), "Text-to-Speech initialization failed", Toast.LENGTH_SHORT).show()
            isTtsInitialized = false
        }
    }

    private fun startReading() {
        if (!isTtsInitialized) {
            Toast.makeText(requireContext(), "Text-to-Speech not ready", Toast.LENGTH_SHORT).show()
            return
        }

        // Stop any ongoing speech
        textToSpeech?.stop()

        // Compile all tutorial text
        val tutorialText = buildString {
            append("Welcome to the Butterfly Detector! ")
            append("This tutorial will guide you through the main features of the app. ")
            append("\n\n")
            append("First, the Camera feature. ")
            append("With the camera you can take pictures of butterflies. ")
            append("While the camera is open, the button will automatically take pictures until you select the Photo selection. ")
            append("While you are anywhere else in the app, the button will take you back to the camera. ")
            append("To take pictures you will have to press the button again. ")
            append("\n\n")
            append("Second, the Photo Selection feature. ")
            append("The photo selection will show you the taken pictures. ")
            append("Here you can choose the best picture to send it to AI identification. ")
            append("\n\n")
            append("Third, the Transects feature. ")
            append("With the transects you can walk default routes and document any butterfly you can find. ")
            append("\n\n")
            append("That's all for the tutorial. Enjoy using the Butterfly Detector!")
        }

        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tutorialUtterance")

        textToSpeech?.speak(tutorialText, TextToSpeech.QUEUE_FLUSH, params, "tutorialUtterance")

        isReading = true
        isPaused = false
        updateButtonStates()
    }

    private fun pauseReading() {
        if (isReading && !isPaused) {
            textToSpeech?.stop()
            isPaused = true
            isReading = false
            updateButtonStates()
            Toast.makeText(requireContext(), "Reading paused", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resumeReading() {
        // Note: Android TTS doesn't support true pause/resume
        // So we restart from the beginning
        Toast.makeText(requireContext(), "Restarting from beginning", Toast.LENGTH_SHORT).show()
        isPaused = false
        startReading()
    }

    private fun updateButtonStates() {
        if (isReading) {
            playButton.isEnabled = false
            pauseButton.isEnabled = true
            playButton.text = "Playing..."
        } else if (isPaused) {
            playButton.isEnabled = true
            pauseButton.isEnabled = false
            playButton.text = "Resume"
        } else {
            playButton.isEnabled = true
            pauseButton.isEnabled = false
            playButton.text = "Read Tutorial Aloud"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Stop and release TextToSpeech resources
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null

        _binding = null
    }
}