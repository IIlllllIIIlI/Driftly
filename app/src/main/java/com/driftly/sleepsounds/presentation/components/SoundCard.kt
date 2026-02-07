package com.driftly.sleepsounds.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.driftly.sleepsounds.domain.models.Sound
import com.driftly.sleepsounds.presentation.theme.Primary
import com.driftly.sleepsounds.presentation.theme.PrimaryDim
import com.driftly.sleepsounds.presentation.theme.PremiumGold
import com.driftly.sleepsounds.presentation.theme.Surface
import com.driftly.sleepsounds.presentation.theme.SurfaceElevated
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
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isActive -> Primary.copy(alpha = glowAlpha)
            else -> SurfaceVariant.copy(alpha = 0.6f)
        },
        label = "borderColor"
    )

    val cardColor by animateColorAsState(
        targetValue = if (isActive) SurfaceElevated else SurfaceVariant,
        label = "cardColor"
    )

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 0.5.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 4.dp else 0.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Icon circle background
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) PrimaryDim.copy(alpha = 0.3f)
                            else Surface.copy(alpha = 0.6f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = sound.icon,
                        fontSize = 26.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = sound.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isActive) Primary else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Premium lock badge
            if (sound.isPremium && !isPremiumUser) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(PremiumGold.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Premium",
                        tint = PremiumGold,
                        modifier = Modifier
                            .size(11.dp)
                            .alpha(0.9f)
                    )
                }
            }
        }
    }
}
