package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
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
import com.example.ui.theme.AvatarColors

@Composable
fun SpeedDialScreen(
    speedDialContacts: List<Contact>,
    onOpenAssignSlot: (key: Int) -> Unit,
    onRemoveSlot: (key: Int) -> Unit,
    onCallContact: (phone: String, name: String, avatarColorIndex: Int, contactId: Long) -> Unit
) {
    val speedDialMap = remember(speedDialContacts) {
        speedDialContacts.associateBy { it.speedDialKey ?: -1 }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Quick-Dial Shortcuts",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Assign favorite contacts to keys 1 - 9",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        // Info banner
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Tip: In the dialpad, simply press and hold any number key (1-9) to instantly speed dial the assigned contact!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3x3 Grid of Keys 1 to 9
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items((1..9).toList()) { key ->
                val contact = speedDialMap[key]
                SpeedDialGridSlot(
                    keyNumber = key,
                    contact = contact,
                    onClick = {
                        if (contact != null) {
                            onCallContact(contact.phone, contact.name, contact.avatarColorIndex, contact.id)
                        } else {
                            onOpenAssignSlot(key)
                        }
                    },
                    onEditClick = { onOpenAssignSlot(key) },
                    onDeleteClick = { onRemoveSlot(key) }
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun SpeedDialGridSlot(
    keyNumber: Int,
    contact: Contact?,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable(onClick = onClick)
            .testTag("speed_dial_slot_$keyNumber")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Key Number Badge in top-left
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (contact != null) primaryColor
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$keyNumber",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (contact != null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }

            if (contact != null) {
                // Remove button in top-right
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                        .padding(2.dp)
                        .testTag("remove_slot_button_$keyNumber")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Contact Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 28.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val avatarBg = AvatarColors[contact.avatarColorIndex % AvatarColors.size]
                    Box(
                        modifier = Modifier
                            .size(36.dp)
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

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = contact.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Tap to call",
                        fontSize = 10.sp,
                        color = primaryColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // Empty Slot
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Assign",
                        tint = primaryColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Assign Key",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryColor
                    )
                }
            }
        }
    }
}
