package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Contact
import com.example.ui.components.DialpadKey
import com.example.ui.theme.AvatarColors
import com.example.util.T9SearchEngine

@Composable
fun DialerScreen(
    dialedDigits: String,
    speedDialContacts: List<Contact>,
    t9Matches: List<T9SearchEngine.T9MatchResult>,
    onDigitPress: (Char) -> Unit,
    onDigitLongPress: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClearDigits: () -> Unit,
    onCallClick: (phone: String, name: String) -> Unit,
    onVoiceSearchClick: () -> Unit,
    onAddContact: (phone: String) -> Unit,
    onSpeedDialClick: (key: Int) -> Unit,
    onOpenSpeedDialAssign: (key: Int) -> Unit
) {
    // Map speed dial keys 1-9 to assigned contacts
    val speedDialMap = remember(speedDialContacts) {
        speedDialContacts.associateBy { it.speedDialKey ?: -1 }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP: Quick-Dial Shortcuts Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Quick-Dial Shortcuts",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "Long-press keys 1-9 to dial",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..9).forEach { key ->
                    val contact = speedDialMap[key]
                    item(key = key) {
                        QuickDialChip(
                            keySlot = key,
                            contact = contact,
                            onClick = {
                                if (contact != null) {
                                    onCallClick(contact.phone, contact.name)
                                } else {
                                    onOpenSpeedDialAssign(key)
                                }
                            }
                        )
                    }
                }
            }
        }

        // T9 Matching Contacts Card (reveals when digits are entered)
        AnimatedVisibility(
            visible = dialedDigits.isNotEmpty() && t9Matches.isNotEmpty(),
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                val topMatch = t9Matches.firstOrNull()
                if (topMatch != null) {
                    val contact = topMatch.contact
                    val avatarBg = AvatarColors[contact.avatarColorIndex % AvatarColors.size]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCallClick(contact.phone, contact.name) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(avatarBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = contact.name.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = contact.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${contact.phone} • Matched by T9",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (t9Matches.size > 1) {
                            Text(
                                text = "+${t9Matches.size - 1} more",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

        // DIALED NUMBER DISPLAY
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (dialedDigits.isEmpty()) {
                Text(
                    text = "Enter a number or name",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = dialedDigits,
                    fontSize = if (dialedDigits.length > 12) 26.sp else 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("dialed_number_text")
                )

                // Add to contacts shortcut
                Row(
                    modifier = Modifier
                        .clickable { onAddContact(dialedDigits) }
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Add Contact",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add to Contacts",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // TACTILE DIALPAD GRID (1-9, *, 0, #)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Row 1: 1, 2, 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DialpadKey(
                    digit = "1",
                    letters = "VOICE",
                    assignedContactName = speedDialMap[1]?.name,
                    onClick = { onDigitPress('1') },
                    onLongClick = { onDigitLongPress('1') }
                )
                DialpadKey(
                    digit = "2",
                    letters = "ABC",
                    assignedContactName = speedDialMap[2]?.name,
                    onClick = { onDigitPress('2') },
                    onLongClick = { onDigitLongPress('2') }
                )
                DialpadKey(
                    digit = "3",
                    letters = "DEF",
                    assignedContactName = speedDialMap[3]?.name,
                    onClick = { onDigitPress('3') },
                    onLongClick = { onDigitLongPress('3') }
                )
            }

            // Row 2: 4, 5, 6
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DialpadKey(
                    digit = "4",
                    letters = "GHI",
                    assignedContactName = speedDialMap[4]?.name,
                    onClick = { onDigitPress('4') },
                    onLongClick = { onDigitLongPress('4') }
                )
                DialpadKey(
                    digit = "5",
                    letters = "JKL",
                    assignedContactName = speedDialMap[5]?.name,
                    onClick = { onDigitPress('5') },
                    onLongClick = { onDigitLongPress('5') }
                )
                DialpadKey(
                    digit = "6",
                    letters = "MNO",
                    assignedContactName = speedDialMap[6]?.name,
                    onClick = { onDigitPress('6') },
                    onLongClick = { onDigitLongPress('6') }
                )
            }

            // Row 3: 7, 8, 9
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DialpadKey(
                    digit = "7",
                    letters = "PQRS",
                    assignedContactName = speedDialMap[7]?.name,
                    onClick = { onDigitPress('7') },
                    onLongClick = { onDigitLongPress('7') }
                )
                DialpadKey(
                    digit = "8",
                    letters = "TUV",
                    assignedContactName = speedDialMap[8]?.name,
                    onClick = { onDigitPress('8') },
                    onLongClick = { onDigitLongPress('8') }
                )
                DialpadKey(
                    digit = "9",
                    letters = "WXYZ",
                    assignedContactName = speedDialMap[9]?.name,
                    onClick = { onDigitPress('9') },
                    onLongClick = { onDigitLongPress('9') }
                )
            }

            // Row 4: *, 0, #
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DialpadKey(
                    digit = "*",
                    letters = "",
                    onClick = { onDigitPress('*') }
                )
                DialpadKey(
                    digit = "0",
                    letters = "+",
                    onClick = { onDigitPress('0') },
                    onLongClick = { onDigitLongPress('0') }
                )
                DialpadKey(
                    digit = "#",
                    letters = "",
                    onClick = { onDigitPress('#') }
                )
            }
        }

        // BOTTOM ACTION ROW: Voice Search, Call Button, Backspace
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice Search Action
            IconButton(
                onClick = onVoiceSearchClick,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("voice_search_action_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Search",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Primary Call Action
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E676))
                    .clickable {
                        if (dialedDigits.isNotEmpty()) {
                            onCallClick(dialedDigits, "")
                        } else if (speedDialContacts.isNotEmpty()) {
                            val firstFav = speedDialContacts.first()
                            onCallClick(firstFav.phone, firstFav.name)
                        }
                    }
                    .testTag("primary_call_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Backspace / Clear
            IconButton(
                onClick = onBackspace,
                enabled = dialedDigits.isNotEmpty(),
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (dialedDigits.isNotEmpty()) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                    .testTag("backspace_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = if (dialedDigits.isNotEmpty()) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickDialChip(
    keySlot: Int,
    contact: Contact?,
    onClick: () -> Unit
) {
    if (contact != null) {
        val avatarBg = AvatarColors[contact.avatarColorIndex % AvatarColors.size]
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .clickable(onClick = onClick)
                .testTag("quick_dial_chip_$keySlot")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$keySlot",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = contact.name.take(8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "Key $keySlot",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    } else {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier
                .clickable(onClick = onClick)
                .testTag("quick_dial_chip_empty_$keySlot")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$keySlot",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "+ Set",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }
        }
    }
}
