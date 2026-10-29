package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KeypadShape
import com.example.ui.theme.LocalPulseDialTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialpadKey(
    digit: String,
    letters: String = "",
    assignedContactName: String? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val themeConfig = LocalPulseDialTheme.current
    val shape: Shape = when (themeConfig.keypadShape) {
        KeypadShape.CIRCLE -> CircleShape
        KeypadShape.ROUNDED_SQUIRCLE -> RoundedCornerShape(22.dp)
        KeypadShape.NEUMORPHIC_PILL -> RoundedCornerShape(16.dp)
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .size(76.dp)
            .clip(shape)
            .background(surfaceColor.copy(alpha = 0.9f))
            .border(
                width = 1.dp,
                color = primaryColor.copy(alpha = 0.22f),
                shape = shape
            )
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = primaryColor),
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("dialpad_key_$digit"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = digit,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = onSurface,
                lineHeight = 28.sp
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurface.copy(alpha = 0.6f),
                    letterSpacing = 1.5.sp
                )
            }
        }

        // Speed Dial Shortcut badge
        if (!assignedContactName.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(primaryColor.copy(alpha = 0.18f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = assignedContactName.take(5),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
