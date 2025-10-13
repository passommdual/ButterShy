package com.example.butterflydetector.ui.tutorial

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.butterflydetector.R
import com.example.butterflydetector.databinding.FragmentTutorialBinding
import com.example.butterflydetector.ui.base.BaseFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.util.*

class TutorialFragment : BaseFragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentTutorialBinding? = null
    private val binding get() = _binding!!
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false
    private var isReading = false
    private var isPaused = false
    private var tutorialSentences: List<String> = emptyList()
    private var currentSentenceIndex: Int = 0
    private lateinit var playResumeButton: MaterialButton
    private lateinit var pauseButton: MaterialButton
    private lateinit var readFromTopButton: MaterialButton

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

        playResumeButton = binding.playResumeButton
        pauseButton = binding.pauseButton
        readFromTopButton = binding.readFromTopButton

        playResumeButton.setOnClickListener {
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

        readFromTopButton.setOnClickListener {
            readFromTop()
        }

        binding.nextButton.setOnClickListener {
            try {
                // Stop any ongoing text-to-speech
                textToSpeech?.stop()

                // Navigate to the home/camera fragment
                findNavController().navigate(R.id.nav_camera)
            } catch (e: Exception) {
                Log.e("TutorialFragment", "Navigation error", e)
                Toast.makeText(requireContext(), "Navigation failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        updateButtonVisibility()

        return root
    }

    override fun applyColorMode(view: View) {
        super.applyColorMode(view)

        // Apply background color to ScrollView
        val scrollView = view.findViewById<ScrollView>(R.id.tutorial_scroll_view)
        scrollView?.setBackgroundColor(getBookPages())

        // Apply background color to main LinearLayout
        val mainLayout = view.findViewById<LinearLayout>(R.id.tutorial_main_layout)
        mainLayout?.setBackgroundColor(getBookPages())

        // Apply colors to MaterialCardViews
        val card1 = view.findViewById<MaterialCardView>(R.id.tutorial_card_1)
        val card2 = view.findViewById<MaterialCardView>(R.id.tutorial_card_2)
        val card3 = view.findViewById<MaterialCardView>(R.id.tutorial_card_3)
        val card4 = view.findViewById<MaterialCardView>(R.id.tutorial_card_4)
        val card5 = view.findViewById<MaterialCardView>(R.id.tutorial_card_5)


        card1?.setCardBackgroundColor(getLogoGreen())
        card2?.setCardBackgroundColor(getLogoGreen())
        card3?.setCardBackgroundColor(getLogoGreen())
        card4?.setCardBackgroundColor(getLogoGreen())
        card5?.setCardBackgroundColor(getLogoGreen())

        val nextButton = view.findViewById<MaterialButton>(R.id.nextButton)
        nextButton.setBackgroundColor(getLogoGreen())


        // Apply button colors
        playResumeButton.setBackgroundColor(getLogoDarkGreen())
        pauseButton.setBackgroundColor(getLogoDarkGreen())
        readFromTopButton.setBackgroundColor(getLogoDarkGreen())
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

                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        activity?.runOnUiThread {
                            isReading = true
                            updateButtonVisibility()
                        }
                    }

                    override fun onDone(utteranceId: String?) {
                        activity?.runOnUiThread {
                            // Move to next sentence
                            currentSentenceIndex++

                            if (currentSentenceIndex < tutorialSentences.size && !isPaused) {
                                // Continue reading next sentence
                                speakCurrentSentence()
                            } else {
                                // Finished reading all sentences
                                isReading = false
                                isPaused = false
                                currentSentenceIndex = 0
                                updateButtonVisibility()
                            }
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        activity?.runOnUiThread {
                            isReading = false
                            isPaused = false
                            currentSentenceIndex = 0
                            updateButtonVisibility()
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
                        if (text.isNotEmpty() && child.id != binding.playResumeButton.id &&
                            child.id != binding.pauseButton.id && child.id != binding.readFromTopButton.id) {
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
                        val hasPlayButton = findViewInGroup(child, binding.playResumeButton.id)
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

    private fun splitIntoSentences(text: String): List<String> {
        // Split by sentence-ending punctuation, keeping the punctuation
        val sentences = text.split(Regex("(?<=[.!?])\\s+"))
            .filter { it.isNotBlank() }
            .map { it.trim() }

        return sentences
    }

    private fun speakCurrentSentence() {
        if (currentSentenceIndex < tutorialSentences.size) {
            val sentence = tutorialSentences[currentSentenceIndex]
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "sentence_$currentSentenceIndex")

            textToSpeech?.speak(sentence, TextToSpeech.QUEUE_FLUSH, params, "sentence_$currentSentenceIndex")

            Log.d("TTS", "Speaking sentence $currentSentenceIndex: $sentence")
        }
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

        tutorialSentences = splitIntoSentences(tutorialText)
        currentSentenceIndex = 0
        isPaused = false

        Log.d("TTS", "Starting reading. Total sentences: ${tutorialSentences.size}")

        speakCurrentSentence()

        isReading = true
        updateButtonVisibility()
    }

    private fun pauseReading() {
        if (isReading) {
            textToSpeech?.stop()
            isPaused = true
            isReading = false
            updateButtonVisibility()
            Toast.makeText(requireContext(), "Reading paused at sentence ${currentSentenceIndex + 1} of ${tutorialSentences.size}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resumeReading() {
        if (isPaused && currentSentenceIndex < tutorialSentences.size) {
            isPaused = false
            isReading = true

            Log.d("TTS", "Resuming from sentence $currentSentenceIndex")

            speakCurrentSentence()
            updateButtonVisibility()
            Toast.makeText(requireContext(), "Resuming from sentence ${currentSentenceIndex + 1}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readFromTop() {
        currentSentenceIndex = 0
        isPaused = false
        startReading()
        Toast.makeText(requireContext(), "Reading from the beginning", Toast.LENGTH_SHORT).show()
    }

    private fun updateButtonVisibility() {
        when {
            isReading -> {
                // Playing state: Show only Pause button
                playResumeButton.visibility = View.GONE
                pauseButton.visibility = View.VISIBLE
                readFromTopButton.visibility = View.GONE
            }
            isPaused -> {
                // Paused state: Show Resume and Read from Top buttons
                playResumeButton.visibility = View.VISIBLE
                playResumeButton.text = "Resume"
                playResumeButton.setIconResource(android.R.drawable.ic_media_play)
                pauseButton.visibility = View.GONE
                readFromTopButton.visibility = View.VISIBLE
            }
            else -> {
                // Initial/Stopped state: Show only Play button
                playResumeButton.visibility = View.VISIBLE
                playResumeButton.text = "Read Aloud"
                playResumeButton.setIconResource(android.R.drawable.ic_media_play)
                pauseButton.visibility = View.GONE
                readFromTopButton.visibility = View.GONE
            }
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