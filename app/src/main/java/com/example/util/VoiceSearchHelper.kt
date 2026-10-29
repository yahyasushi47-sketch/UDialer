package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.data.model.Contact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class VoiceSearchState {
    object Idle : VoiceSearchState()
    object Listening : VoiceSearchState()
    data class Processing(val partialText: String) : VoiceSearchState()
    data class Success(
        val spokenQuery: String,
        val cleanedQuery: String,
        val matches: List<Contact>,
        val bestMatch: Contact?
    ) : VoiceSearchState()
    data class Error(val message: String) : VoiceSearchState()
}

class VoiceSearchHelper(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val _state = MutableStateFlow<VoiceSearchState>(VoiceSearchState.Idle)
    val state: StateFlow<VoiceSearchState> = _state.asStateFlow()

    private val _soundLevel = MutableStateFlow(0f)
    val soundLevel: StateFlow<Float> = _soundLevel.asStateFlow()

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(allContacts: List<Contact>) {
        if (!isAvailable()) {
            _state.value = VoiceSearchState.Error("Speech recognition is not available on this device.")
            return
        }

        stopListening()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _state.value = VoiceSearchState.Listening
                    }

                    override fun onBeginningOfSpeech() {
                        _state.value = VoiceSearchState.Processing("")
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _soundLevel.value = (rmsdB / 10f).coerceIn(0.1f, 1f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _soundLevel.value = 0f
                    }

                    override fun onError(error: Int) {
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try speaking clearly."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Please try again."
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout during recognition."
                            else -> "Could not process speech (code $error)."
                        }
                        _state.value = VoiceSearchState.Error(errorMsg)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val topMatch = matches?.firstOrNull() ?: ""
                        processSpokenQuery(topMatch, allContacts)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partials = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        partials?.firstOrNull()?.let {
                            _state.value = VoiceSearchState.Processing(it)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _state.value = VoiceSearchState.Error("Failed to start voice search: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        }
        speechRecognizer = null
        _soundLevel.value = 0f
    }

    fun reset() {
        stopListening()
        _state.value = VoiceSearchState.Idle
    }

    fun processSpokenQuery(spoken: String, allContacts: List<Contact>) {
        if (spoken.isBlank()) {
            _state.value = VoiceSearchState.Error("Nothing heard. Try saying 'Call Mom' or 'Find Sarah'.")
            return
        }

        val cleaned = cleanSpokenCommand(spoken)
        val matchedContacts = findMatches(cleaned, allContacts)
        val bestMatch = matchedContacts.firstOrNull()

        _state.value = VoiceSearchState.Success(
            spokenQuery = spoken,
            cleanedQuery = cleaned,
            matches = matchedContacts,
            bestMatch = bestMatch
        )
    }

    private fun cleanSpokenCommand(raw: String): String {
        var query = raw.trim()
        val commandPrefixes = listOf(
            "call", "dial", "phone", "find", "search for", "search", "lookup", "look for", "ring", "reach"
        )
        for (prefix in commandPrefixes) {
            if (query.startsWith(prefix, ignoreCase = true)) {
                query = query.substring(prefix.length).trim()
                break
            }
        }
        return query
    }

    private fun findMatches(query: String, allContacts: List<Contact>): List<Contact> {
        val lowerQuery = query.lowercase().trim()
        if (lowerQuery.isEmpty()) return allContacts

        val digitsQuery = lowerQuery.filter { it.isDigit() }

        return allContacts.filter { contact ->
            val nameLower = contact.name.lowercase()
            val phoneDigits = contact.phone.filter { it.isDigit() }

            // Name match
            nameLower.contains(lowerQuery) ||
                lowerQuery.contains(nameLower) ||
                // Digit match
                (digitsQuery.isNotEmpty() && phoneDigits.contains(digitsQuery)) ||
                // Label match (e.g. "Family", "Emergency")
                contact.label.lowercase().contains(lowerQuery)
        }.sortedWith(
            compareByDescending<Contact> {
                // Exact full name match first
                it.name.equals(query, ignoreCase = true)
            }.thenByDescending {
                // Starts with query
                it.name.startsWith(query, ignoreCase = true)
            }.thenByDescending {
                // Favorite
                it.isFavorite
            }
        )
    }
}
