package com.example.butterflydetector.ui.tutorial

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.butterflydetector.databinding.FragmentTutorialBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
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

    /**
     * Extracts all text content from the tutorial layout dynamically
     */
    private fun extractTutorialText(): String {
        val textBuilder = StringBuilder()

        // Get the root ScrollView's child (LinearLayout)
        val rootView = binding.root
        val scrollViewChild = rootView.getChildAt(0)

        if (scrollViewChild is LinearLayout) {
            // Iterate through all children of the main LinearLayout
            for (i in 0 until scrollViewChild.childCount) {
                val child = scrollViewChild.getChildAt(i)

                when (child) {
                    is TextView -> {
                        // Extract text from standalone TextViews
                        val text = child.text.toString().trim()
                        if (text.isNotEmpty() && child.id != binding.playButton.id && child.id != binding.pauseButton.id) {
                            textBuilder.append(text)
                            textBuilder.append(". ")
                        }
                    }
                    is MaterialCardView -> {
                        // Extract text from cards (feature descriptions)
                        val cardText = extractTextFromViewGroup(child)
                        if (cardText.isNotEmpty()) {
                            textBuilder.append(cardText)
                            textBuilder.append(". ")
                        }
                    }
                    is LinearLayout -> {
                        // Check if this is the button container (skip it)
                        val hasPlayButton = findViewInGroup(child, binding.playButton.id)
                        if (!hasPlayButton) {
                            // Extract text from other LinearLayouts
                            val layoutText = extractTextFromViewGroup(child)
                            if (layoutText.isNotEmpty()) {
                                textBuilder.append(layoutText)
                                textBuilder.append(". ")
                            }
                        }
                    }
                }
            }
        }

        return textBuilder.toString().trim()
    }

    /**
     * Recursively extracts text from a ViewGroup
     */
    private fun extractTextFromViewGroup(viewGroup: ViewGroup): String {
        val textBuilder = StringBuilder()

        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)

            when (child) {
                is TextView -> {
                    val text = child.text.toString().trim()
                    if (text.isNotEmpty()) {
                        textBuilder.append(text)
                        textBuilder.append(" ")
                    }
                }
                is ViewGroup -> {
                    // Recursively extract from nested ViewGroups
                    val nestedText = extractTextFromViewGroup(child)
                    if (nestedText.isNotEmpty()) {
                        textBuilder.append(nestedText)
                        textBuilder.append(" ")
                    }
                }
            }
        }

        return textBuilder.toString().trim()
    }

    /**
     * Helper function to check if a view with specific ID exists in a ViewGroup
     */
    private fun findViewInGroup(viewGroup: ViewGroup, targetId: Int): Boolean {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child.id == targetId) {
                return true
            }
            if (child is ViewGroup) {
                if (findViewInGroup(child, targetId)) {
                    return true
                }
            }
        }
        return false
    }

    private fun startReading() {
        if (!isTtsInitialized) {
            Toast.makeText(requireContext(), "Text-to-Speech not ready", Toast.LENGTH_SHORT).show()
            return
        }

        // Stop any ongoing speech
        textToSpeech?.stop()

        // Extract text dynamically from the layout
        val tutorialText = extractTutorialText()

        if (tutorialText.isEmpty()) {
            Toast.makeText(requireContext(), "No text to read", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("TTS", "Reading text: $tutorialText")

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