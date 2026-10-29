package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Contact
import com.example.ui.components.ContactCard

enum class ContactFilter {
    ALL,
    FAVORITES,
    SPEED_DIAL
}

@Composable
fun ContactsScreen(
    contacts: List<Contact>,
    onCallContact: (phone: String, name: String, avatarColorIndex: Int, contactId: Long) -> Unit,
    onSmsContact: (phone: String) -> Unit,
    onToggleFavorite: (Contact) -> Unit,
    onEditContact: (Contact) -> Unit,
    onAddContact: () -> Unit,
    onVoiceSearchClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(ContactFilter.ALL) }

    val filteredContacts = remember(contacts, searchQuery, selectedFilter) {
        contacts.filter { contact ->
            val matchesQuery = if (searchQuery.isBlank()) true else {
                contact.name.contains(searchQuery, ignoreCase = true) ||
                    contact.phone.contains(searchQuery, ignoreCase = true) ||
                    contact.label.contains(searchQuery, ignoreCase = true)
            }
            val matchesFilter = when (selectedFilter) {
                ContactFilter.ALL -> true
                ContactFilter.FAVORITES -> contact.isFavorite
                ContactFilter.SPEED_DIAL -> contact.speedDialKey != null
            }
            matchesQuery && matchesFilter
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar with Voice Search button integrated
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search name, phone, or label...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = onVoiceSearchClick,
                        modifier = Modifier.testTag("voice_search_bar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contacts_search_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipItem(
                    label = "All (${contacts.size})",
                    isSelected = selectedFilter == ContactFilter.ALL,
                    onClick = { selectedFilter = ContactFilter.ALL }
                )
                FilterChipItem(
                    label = "Favorites (${contacts.count { it.isFavorite }})",
                    icon = Icons.Default.Star,
                    isSelected = selectedFilter == ContactFilter.FAVORITES,
                    onClick = { selectedFilter = ContactFilter.FAVORITES }
                )
                FilterChipItem(
                    label = "Speed Dial (${contacts.count { it.speedDialKey != null }})",
                    icon = Icons.Default.FlashOn,
                    isSelected = selectedFilter == ContactFilter.SPEED_DIAL,
                    onClick = { selectedFilter = ContactFilter.SPEED_DIAL }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Contacts List
            if (filteredContacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Contacts,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No contacts match \"$searchQuery\""
                            else "No contacts found in this list",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the + button below to add your first contact or use voice search.",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredContacts, key = { it.id }) { contact ->
                        ContactCard(
                            contact = contact,
                            onCallClick = {
                                onCallContact(contact.phone, contact.name, contact.avatarColorIndex, contact.id)
                            },
                            onSmsClick = {
                                onSmsContact(contact.phone)
                            },
                            onToggleFavorite = {
                                onToggleFavorite(contact)
                            },
                            onClick = {
                                onEditContact(contact)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Add Contact FAB
        FloatingActionButton(
            onClick = onAddContact,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 20.dp)
                .testTag("add_contact_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Contact")
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
