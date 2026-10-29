package com.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainTab
import com.example.ui.MainViewModel
import com.example.ui.components.ActiveCallDialog
import com.example.ui.components.AddEditContactDialog
import com.example.ui.components.SpeedDialAssignDialog
import com.example.ui.components.VoiceSearchDialog
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.DialerScreen
import com.example.ui.screens.RecentsScreen
import com.example.ui.screens.SpeedDialScreen
import com.example.ui.screens.ThemeSettingsScreen
import com.example.ui.theme.PulseDialCustomTheme
import com.example.ui.theme.PulseDialTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themePreset by viewModel.themePreset.collectAsStateWithLifecycle()
            val keypadShape by viewModel.keypadShape.collectAsStateWithLifecycle()
            val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
            val hapticEnabled by viewModel.hapticEnabled.collectAsStateWithLifecycle()

            val customTheme = remember(themePreset, keypadShape, soundEnabled, hapticEnabled) {
                PulseDialCustomTheme(
                    preset = themePreset,
                    keypadShape = keypadShape,
                    soundEnabled = soundEnabled,
                    hapticEnabled = hapticEnabled
                )
            }

            PulseDialTheme(themeConfig = customTheme) {
                PulseDialApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PulseDialApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val dialedDigits by viewModel.dialedDigits.collectAsStateWithLifecycle()
    val allContacts by viewModel.allContacts.collectAsStateWithLifecycle()
    val speedDialContacts by viewModel.speedDialContacts.collectAsStateWithLifecycle()
    val t9Matches by viewModel.t9Matches.collectAsStateWithLifecycle()
    val callLogs by viewModel.callLogs.collectAsStateWithLifecycle()

    val currentTheme by viewModel.themePreset.collectAsStateWithLifecycle()
    val currentShape by viewModel.keypadShape.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val hapticEnabled by viewModel.hapticEnabled.collectAsStateWithLifecycle()

    val activeCall by viewModel.activeCall.collectAsStateWithLifecycle()
    val isVoiceDialogOpen by viewModel.isVoiceDialogOpen.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()
    val voiceSoundLevel by viewModel.voiceSoundLevel.collectAsStateWithLifecycle()

    val editingContact by viewModel.editingContact.collectAsStateWithLifecycle()
    val isAddContactOpen by viewModel.isAddContactOpen.collectAsStateWithLifecycle()
    val selectedSpeedDialSlot by viewModel.selectedSpeedDialSlot.collectAsStateWithLifecycle()
    val infoBanner by viewModel.infoBanner.collectAsStateWithLifecycle()

    // Permission launchers
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.openVoiceSearch()
        } else {
            Toast.makeText(context, "Microphone permission is needed for voice search", Toast.LENGTH_SHORT).show()
        }
    }

    // Speech Intent Fallback Launcher
    val speechIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.processVoiceSpoken(spokenText)
            }
        }
    }

    val readContactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.importDeviceContacts(context)
        } else {
            Toast.makeText(context, "Contacts permission required to import from device", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper for initiating calls
    val initiateCall: (String, String, Int, Long?) -> Unit = { phone, name, avatarIdx, contactId ->
        viewModel.startCall(name, phone, avatarIdx, contactId)
    }

    // Helper for launching native dialer
    val launchNativeDialer: (String) -> Unit = { phone ->
        val cleanPhone = phone.filter { it.isDigit() || it == '+' }
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanPhone")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "No phone dialer application found", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper for SMS
    val launchSms: (String) -> Unit = { phone ->
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phone")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "No messaging application found", Toast.LENGTH_SHORT).show()
        }
    }

    // Voice search trigger with permission check
    val onTriggerVoiceSearch = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.openVoiceSearch()
        } else {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Auto dismiss info banner
    LaunchedEffect(infoBanner) {
        if (infoBanner != null) {
            delay(3000)
            viewModel.dismissInfoBanner()
        }
    }

    // BackHandler: return to DIALER tab or clear dialed input
    BackHandler(enabled = currentTab != MainTab.DIALER || dialedDigits.isNotEmpty()) {
        if (dialedDigits.isNotEmpty()) {
            viewModel.onClearDigits()
        } else {
            viewModel.setTab(MainTab.DIALER)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                NavBarItem(
                    selected = currentTab == MainTab.DIALER,
                    onClick = { viewModel.setTab(MainTab.DIALER) },
                    icon = Icons.Default.Dialpad,
                    label = "Dialer",
                    tag = "nav_dialer"
                )
                NavBarItem(
                    selected = currentTab == MainTab.CONTACTS,
                    onClick = { viewModel.setTab(MainTab.CONTACTS) },
                    icon = Icons.Default.Contacts,
                    label = "Contacts",
                    tag = "nav_contacts"
                )
                NavBarItem(
                    selected = currentTab == MainTab.RECENTS,
                    onClick = { viewModel.setTab(MainTab.RECENTS) },
                    icon = Icons.Default.History,
                    label = "Recents",
                    tag = "nav_recents"
                )
                NavBarItem(
                    selected = currentTab == MainTab.SPEED_DIAL,
                    onClick = { viewModel.setTab(MainTab.SPEED_DIAL) },
                    icon = Icons.Default.FlashOn,
                    label = "Quick Dial",
                    tag = "nav_speed_dial"
                )
                NavBarItem(
                    selected = currentTab == MainTab.THEMES,
                    onClick = { viewModel.setTab(MainTab.THEMES) },
                    icon = Icons.Default.ColorLens,
                    label = "Themes",
                    tag = "nav_themes"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main Screen Content
            when (currentTab) {
                MainTab.DIALER -> {
                    DialerScreen(
                        dialedDigits = dialedDigits,
                        speedDialContacts = speedDialContacts,
                        t9Matches = t9Matches,
                        onDigitPress = { viewModel.onDigitPress(it) },
                        onDigitLongPress = { viewModel.onDigitLongPress(it) },
                        onBackspace = { viewModel.onBackspace() },
                        onClearDigits = { viewModel.onClearDigits() },
                        onCallClick = { phone, name ->
                            initiateCall(phone, name, 0, null)
                        },
                        onVoiceSearchClick = onTriggerVoiceSearch,
                        onAddContact = { viewModel.openAddContact(it) },
                        onSpeedDialClick = { viewModel.triggerSpeedDial(it) },
                        onOpenSpeedDialAssign = { viewModel.openSpeedDialAssign(it) }
                    )
                }
                MainTab.CONTACTS -> {
                    ContactsScreen(
                        contacts = allContacts,
                        onCallContact = { phone, name, avatarIdx, id ->
                            initiateCall(phone, name, avatarIdx, id)
                        },
                        onSmsContact = { launchSms(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onEditContact = { viewModel.openEditContact(it) },
                        onAddContact = { viewModel.openAddContact() },
                        onVoiceSearchClick = onTriggerVoiceSearch
                    )
                }
                MainTab.RECENTS -> {
                    RecentsScreen(
                        callLogs = callLogs,
                        onCallClick = { phone, name -> initiateCall(phone, name, 0, null) },
                        onDeleteLog = { viewModel.deleteCallLog(it) },
                        onClearAll = { viewModel.clearCallLogs() }
                    )
                }
                MainTab.SPEED_DIAL -> {
                    SpeedDialScreen(
                        speedDialContacts = speedDialContacts,
                        onOpenAssignSlot = { viewModel.openSpeedDialAssign(it) },
                        onRemoveSlot = { viewModel.removeSpeedDial(it) },
                        onCallContact = { phone, name, avatarIdx, id ->
                            initiateCall(phone, name, avatarIdx, id)
                        }
                    )
                }
                MainTab.THEMES -> {
                    ThemeSettingsScreen(
                        currentTheme = currentTheme,
                        currentShape = currentShape,
                        soundEnabled = soundEnabled,
                        hapticsEnabled = hapticEnabled,
                        onSelectTheme = { viewModel.setTheme(it) },
                        onSelectShape = { viewModel.setKeypadShape(it) },
                        onToggleSound = { viewModel.toggleSound() },
                        onToggleHaptics = { viewModel.toggleHaptics() },
                        onImportContacts = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.READ_CONTACTS
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                viewModel.importDeviceContacts(context)
                            } else {
                                readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            }
                        }
                    )
                }
            }

            // Info banner notification
            AnimatedVisibility(
                visible = infoBanner != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = infoBanner ?: "",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // MODAL DIALOGS
    // 1. Voice Search Dialog
    if (isVoiceDialogOpen) {
        VoiceSearchDialog(
            voiceState = voiceState,
            soundLevel = voiceSoundLevel,
            allContacts = allContacts,
            onDismiss = { viewModel.closeVoiceSearch() },
            onRetry = { viewModel.retryVoiceSearch() },
            onSelectContact = { contact ->
                viewModel.closeVoiceSearch()
                initiateCall(contact.phone, contact.name, contact.avatarColorIndex, contact.id)
            },
            onManualSearch = { viewModel.onManualVoiceQuery(it) }
        )
    }

    // 2. Active Call Dialog
    activeCall?.let { call ->
        ActiveCallDialog(
            contactName = call.contactName,
            phoneNumber = call.phoneNumber,
            avatarColorIndex = call.avatarColorIndex,
            onEndCall = { durationSecs ->
                viewModel.endCall(durationSecs)
            },
            onLaunchNativeDialer = {
                launchNativeDialer(call.phoneNumber)
            }
        )
    }

    // 3. Add / Edit Contact Dialog
    if (isAddContactOpen) {
        AddEditContactDialog(
            initialContact = editingContact,
            onDismiss = { viewModel.closeContactDialog() },
            onSave = { contact -> viewModel.saveContact(contact) },
            onDelete = { contact -> viewModel.deleteContact(contact) }
        )
    }

    // 4. Speed Dial Assign Dialog
    selectedSpeedDialSlot?.let { keySlot ->
        val currentAssigned = speedDialContacts.firstOrNull { it.speedDialKey == keySlot }
        SpeedDialAssignDialog(
            keySlot = keySlot,
            currentAssignedContact = currentAssigned,
            allContacts = allContacts,
            onDismiss = { viewModel.closeSpeedDialAssign() },
            onAssignContact = { contactId, key ->
                viewModel.assignSpeedDial(contactId, key)
            },
            onRemoveAssignment = { key ->
                viewModel.removeSpeedDial(key)
            }
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.NavBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    tag: String
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp)
            )
        },
        label = {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        ),
        modifier = Modifier.testTag(tag)
    )
}
