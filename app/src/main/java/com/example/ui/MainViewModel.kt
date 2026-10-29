package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CallLog
import com.example.data.model.CallType
import com.example.data.model.Contact
import com.example.data.model.KeypadShape
import com.example.data.model.ThemePreset
import com.example.data.repository.DialerRepository
import com.example.util.DialerFeedbackHelper
import com.example.util.T9SearchEngine
import com.example.util.VoiceSearchHelper
import com.example.util.VoiceSearchState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    DIALER,
    CONTACTS,
    RECENTS,
    SPEED_DIAL,
    THEMES
}

data class ActiveCallState(
    val contactName: String,
    val phoneNumber: String,
    val avatarColorIndex: Int = 0,
    val contactId: Long? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = DialerRepository(database.contactDao(), database.callLogDao())
    val feedbackHelper = DialerFeedbackHelper(application)
    val voiceHelper = VoiceSearchHelper(application)

    val allContacts = repository.allContacts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val speedDialContacts = repository.speedDialContacts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val callLogs = repository.allCallLogs.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // UI Navigation State
    private val _currentTab = MutableStateFlow(MainTab.DIALER)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Dialpad State
    private val _dialedDigits = MutableStateFlow("")
    val dialedDigits: StateFlow<String> = _dialedDigits.asStateFlow()

    // T9 search matches derived from dialedDigits and allContacts
    val t9Matches: StateFlow<List<T9SearchEngine.T9MatchResult>> = combine(
        _dialedDigits,
        allContacts
    ) { digits, contacts ->
        if (digits.isBlank()) emptyList()
        else T9SearchEngine.match(contacts, digits)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active theme & settings
    private val _themePreset = MutableStateFlow(ThemePreset.CYBER_NEON)
    val themePreset: StateFlow<ThemePreset> = _themePreset.asStateFlow()

    private val _keypadShape = MutableStateFlow(KeypadShape.ROUNDED_SQUIRCLE)
    val keypadShape: StateFlow<KeypadShape> = _keypadShape.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(true)
    val hapticEnabled: StateFlow<Boolean> = _hapticEnabled.asStateFlow()

    // Active Call State
    private val _activeCall = MutableStateFlow<ActiveCallState?>(null)
    val activeCall: StateFlow<ActiveCallState?> = _activeCall.asStateFlow()

    // Voice Dialog State
    private val _isVoiceDialogOpen = MutableStateFlow(false)
    val isVoiceDialogOpen: StateFlow<Boolean> = _isVoiceDialogOpen.asStateFlow()

    val voiceState: StateFlow<VoiceSearchState> = voiceHelper.state
    val voiceSoundLevel: StateFlow<Float> = voiceHelper.soundLevel

    // Contact modal states
    private val _editingContact = MutableStateFlow<Contact?>(null)
    val editingContact: StateFlow<Contact?> = _editingContact.asStateFlow()

    private val _isAddContactOpen = MutableStateFlow(false)
    val isAddContactOpen: StateFlow<Boolean> = _isAddContactOpen.asStateFlow()

    private val _selectedSpeedDialSlot = MutableStateFlow<Int?>(null)
    val selectedSpeedDialSlot: StateFlow<Int?> = _selectedSpeedDialSlot.asStateFlow()

    // Notification toast / banner
    private val _infoBanner = MutableStateFlow<String?>(null)
    val infoBanner: StateFlow<String?> = _infoBanner.asStateFlow()

    fun setTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun onDigitPress(char: Char) {
        feedbackHelper.playTone(char, _soundEnabled.value)
        feedbackHelper.performHapticClick(_hapticEnabled.value)
        _dialedDigits.value += char
    }

    fun onDigitLongPress(digit: Char) {
        feedbackHelper.performLongPressHaptic(_hapticEnabled.value)
        if (digit in '1'..'9') {
            val key = digit - '0'
            triggerSpeedDial(key)
        } else if (digit == '0') {
            // Standard long press 0 adds +
            _dialedDigits.value += "+"
        }
    }

    fun onBackspace() {
        feedbackHelper.performHapticClick(_hapticEnabled.value)
        if (_dialedDigits.value.isNotEmpty()) {
            _dialedDigits.value = _dialedDigits.value.dropLast(1)
        }
    }

    fun onClearDigits() {
        feedbackHelper.performLongPressHaptic(_hapticEnabled.value)
        _dialedDigits.value = ""
    }

    fun triggerSpeedDial(key: Int) {
        viewModelScope.launch {
            val contact = repository.getContactBySpeedDialKey(key)
            if (contact != null) {
                _infoBanner.value = "Speed Dial #$key: Calling ${contact.name}"
                startCall(contact.name, contact.phone, contact.avatarColorIndex, contact.id)
            } else {
                _infoBanner.value = "Speed Dial Key $key is empty. Tap to assign."
                _selectedSpeedDialSlot.value = key
            }
        }
    }

    fun startCall(name: String, phone: String, avatarColorIndex: Int = 0, contactId: Long? = null) {
        _activeCall.value = ActiveCallState(
            contactName = name.ifBlank { phone },
            phoneNumber = phone,
            avatarColorIndex = avatarColorIndex,
            contactId = contactId
        )
    }

    fun endCall(durationSeconds: Int) {
        val current = _activeCall.value ?: return
        viewModelScope.launch {
            repository.logCall(
                name = current.contactName,
                phone = current.phoneNumber,
                type = CallType.OUTGOING,
                durationSeconds = durationSeconds,
                contactId = current.contactId
            )
        }
        _activeCall.value = null
    }

    fun openVoiceSearch() {
        _isVoiceDialogOpen.value = true
        voiceHelper.startListening(allContacts.value)
    }

    fun retryVoiceSearch() {
        voiceHelper.startListening(allContacts.value)
    }

    fun closeVoiceSearch() {
        voiceHelper.stopListening()
        voiceHelper.reset()
        _isVoiceDialogOpen.value = false
    }

    fun onManualVoiceQuery(query: String) {
        voiceHelper.processSpokenQuery(query, allContacts.value)
    }

    fun processVoiceSpoken(query: String) {
        _isVoiceDialogOpen.value = true
        voiceHelper.processSpokenQuery(query, allContacts.value)
    }

    fun setTheme(preset: ThemePreset) {
        _themePreset.value = preset
    }

    fun setKeypadShape(shape: KeypadShape) {
        _keypadShape.value = shape
    }

    fun toggleSound() {
        _soundEnabled.value = !_soundEnabled.value
    }

    fun toggleHaptics() {
        _hapticEnabled.value = !_hapticEnabled.value
    }

    fun openAddContact(initialPhone: String = "") {
        _editingContact.value = if (initialPhone.isNotBlank()) {
            Contact(name = "", phone = initialPhone)
        } else null
        _isAddContactOpen.value = true
    }

    fun openEditContact(contact: Contact) {
        _editingContact.value = contact
        _isAddContactOpen.value = true
    }

    fun closeContactDialog() {
        _editingContact.value = null
        _isAddContactOpen.value = false
    }

    fun saveContact(contact: Contact) {
        viewModelScope.launch {
            if (contact.id == 0L) {
                repository.insertContact(contact)
            } else {
                repository.updateContact(contact)
            }
            closeContactDialog()
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
            closeContactDialog()
        }
    }

    fun toggleFavorite(contact: Contact) {
        viewModelScope.launch {
            repository.updateContact(contact.copy(isFavorite = !contact.isFavorite))
        }
    }

    fun openSpeedDialAssign(key: Int) {
        _selectedSpeedDialSlot.value = key
    }

    fun closeSpeedDialAssign() {
        _selectedSpeedDialSlot.value = null
    }

    fun assignSpeedDial(contactId: Long, key: Int) {
        viewModelScope.launch {
            repository.assignSpeedDial(contactId, key)
            _selectedSpeedDialSlot.value = null
            _infoBanner.value = "Assigned to Speed Dial Key $key"
        }
    }

    fun removeSpeedDial(key: Int) {
        viewModelScope.launch {
            repository.removeSpeedDial(key)
            _selectedSpeedDialSlot.value = null
        }
    }

    fun deleteCallLog(log: CallLog) {
        viewModelScope.launch {
            repository.deleteCallLog(log)
        }
    }

    fun clearCallLogs() {
        viewModelScope.launch {
            repository.clearAllCallLogs()
        }
    }

    fun importDeviceContacts(context: Context) {
        viewModelScope.launch {
            val count = repository.importDeviceContacts(context)
            _infoBanner.value = if (count > 0) "Imported $count contacts from device" else "No new device contacts found"
        }
    }

    fun dismissInfoBanner() {
        _infoBanner.value = null
    }

    override fun onCleared() {
        super.onCleared()
        feedbackHelper.release()
        voiceHelper.stopListening()
    }
}
