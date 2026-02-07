package com.driftly.sleepsounds.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driftly.sleepsounds.domain.models.Sound
import com.driftly.sleepsounds.presentation.theme.Primary
import com.driftly.sleepsounds.presentation.theme.PremiumGold
import com.driftly.sleepsounds.presentation.theme.Surface
import com.driftly.sleepsounds.presentation.theme.SurfaceVariant

@Composable
fun SoundCard(
    sound: Sound,
    isActive: Boolean,
    isPremiumUser: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isActive -> Primary.copy(alpha = glowAlpha)
            else -> SurfaceVariant
        },
        label = "borderColor"
    )

    val cardColor by animateColorAsState(
        targetValue = if (isActive) SurfaceVariant else Surface,
        label = "cardColor"
    )

    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = sound.icon,
                    fontSize = 28.sp
                )
                Text(
                    text = sound.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) Primary else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (sound.isPremium && !isPremiumUser) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Premium",
                    tint = PremiumGold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(14.dp)
                        .alpha(0.8f)
                )
            }
        }
    }
}
