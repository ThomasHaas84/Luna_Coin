package de.meson_labs.luna_coin.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.meson_labs.luna_coin.viewmodel.CelebrationEvent
import de.meson_labs.luna_coin.viewmodel.CelebrationType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class CelebrationParticle(
    val startX: Float,
    val delay: Float,
    val speed: Float,
    val drift: Float,
    val size: Float,
    val rotation: Float,
    val colorIndex: Int
)

@Composable
fun CelebrationOverlay(
    event: CelebrationEvent,
    isPhone: Boolean,
    onFinished: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val overlayAlpha = remember(event.id) { Animatable(0f) }
    val contentScale = remember(event.id) { Animatable(0.55f) }
    val particleProgress = remember(event.id) { Animatable(0f) }
    val fireworkProgress = remember(event.id) { Animatable(0f) }

    val iconScale by animateFloatAsState(
        targetValue = if (contentScale.value >= 0.98f) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "celebrationIconScale"
    )

    LaunchedEffect(event.id) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

        launch {
            overlayAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(280, easing = FastOutSlowInEasing)
            )
        }

        launch {
            contentScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        launch {
            particleProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(2500, easing = LinearEasing)
            )
        }

        launch {
            fireworkProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(1800, easing = LinearEasing)
            )
        }

        delay(2450)

        overlayAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        )

        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(overlayAlpha.value)
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        CelebrationEffects(
            eventId = event.id,
            progress = particleProgress.value,
            fireworkProgress = fireworkProgress.value,
            type = event.type
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isPhone) 24.dp else 150.dp)
                .graphicsLayer {
                    scaleX = contentScale.value
                    scaleY = contentScale.value
                },
            shape = RoundedCornerShape(if (isPhone) 28.dp else 36.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (event.type) {
                    CelebrationType.HIGHSCORE ->
                        MaterialTheme.colorScheme.tertiaryContainer
                    CelebrationType.LEVEL_UP ->
                        MaterialTheme.colorScheme.primaryContainer
                },
                contentColor = when (event.type) {
                    CelebrationType.HIGHSCORE ->
                        MaterialTheme.colorScheme.onTertiaryContainer
                    CelebrationType.LEVEL_UP ->
                        MaterialTheme.colorScheme.onPrimaryContainer
                }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (isPhone) 22.dp else 42.dp,
                        vertical = if (isPhone) 30.dp else 42.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = when (event.type) {
                        CelebrationType.HIGHSCORE -> "🏆"
                        CelebrationType.LEVEL_UP -> "⭐"
                    },
                    fontSize = if (isPhone) 78.sp else 108.sp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
                )

                Spacer(modifier = Modifier.height(if (isPhone) 10.dp else 16.dp))

                Text(
                    text = event.title,
                    fontSize = if (isPhone) 28.sp else 42.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = if (isPhone) 33.sp else 48.sp
                )

                Spacer(modifier = Modifier.height(if (isPhone) 12.dp else 18.dp))

                Text(
                    text = event.subtitle,
                    style = if (isPhone) {
                        MaterialTheme.typography.titleLarge
                    } else {
                        MaterialTheme.typography.headlineMedium
                    },
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(if (isPhone) 8.dp else 12.dp))

                Text(
                    text = event.details,
                    style = if (isPhone) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.titleLarge
                    },
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(if (isPhone) 16.dp else 22.dp))

                Text(
                    text = event.footer,
                    style = if (isPhone) {
                        MaterialTheme.typography.bodyLarge
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CelebrationEffects(
    eventId: String,
    progress: Float,
    fireworkProgress: Float,
    type: CelebrationType
) {
    val particles = remember(eventId) {
        val random = Random(eventId.hashCode())
        List(95) {
            CelebrationParticle(
                startX = random.nextFloat(),
                delay = random.nextFloat() * 0.35f,
                speed = 0.75f + random.nextFloat() * 0.75f,
                drift = -0.18f + random.nextFloat() * 0.36f,
                size = 5f + random.nextFloat() * 10f,
                rotation = random.nextFloat() * 360f,
                colorIndex = random.nextInt(6)
            )
        }
    }

    val colors = when (type) {
        CelebrationType.HIGHSCORE -> listOf(
            Color(0xFFFFD54F),
            Color(0xFFFF7043),
            Color(0xFFAB47BC),
            Color(0xFF42A5F5),
            Color(0xFF66BB6A),
            Color.White
        )

        CelebrationType.LEVEL_UP -> listOf(
            Color(0xFFFFD54F),
            Color(0xFFFFB300),
            Color(0xFFFFF176),
            Color.White,
            Color(0xFF81D4FA),
            Color(0xFFFF8A80)
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val localProgress = (
                    (progress - particle.delay) / (1f - particle.delay)
                    ).coerceIn(0f, 1f)

            if (localProgress > 0f && localProgress < 1f) {
                val x = size.width * (
                        particle.startX +
                                particle.drift * sin(localProgress * PI).toFloat()
                        )
                val y = -30f + (
                        size.height + 70f
                        ) * localProgress * particle.speed

                val fade = when {
                    localProgress < 0.15f -> localProgress / 0.15f
                    localProgress > 0.82f -> (1f - localProgress) / 0.18f
                    else -> 1f
                }.coerceIn(0f, 1f)

                val color = colors[particle.colorIndex].copy(alpha = fade)
                val angle = (
                        particle.rotation +
                                localProgress * 540f
                        ) * PI.toFloat() / 180f

                val halfLength = particle.size
                val dx = cos(angle) * halfLength
                val dy = sin(angle) * halfLength

                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(x - dx, y - dy),
                    end = androidx.compose.ui.geometry.Offset(x + dx, y + dy),
                    strokeWidth = particle.size * 0.65f
                )
            }
        }

        val burstCenters = listOf(
            androidx.compose.ui.geometry.Offset(size.width * 0.18f, size.height * 0.22f),
            androidx.compose.ui.geometry.Offset(size.width * 0.82f, size.height * 0.26f),
            androidx.compose.ui.geometry.Offset(size.width * 0.50f, size.height * 0.12f)
        )

        burstCenters.forEachIndexed { burstIndex, center ->
            val delayedProgress = (
                    fireworkProgress - burstIndex * 0.16f
                    ).coerceIn(0f, 1f)

            if (delayedProgress > 0f) {
                val expansion = if (delayedProgress < 0.55f) {
                    delayedProgress / 0.55f
                } else {
                    1f
                }
                val fade = (1f - delayedProgress).coerceIn(0f, 1f)
                val radius = 24f + 130f * expansion

                repeat(18) { ray ->
                    val angle = (2.0 * PI * ray / 18.0).toFloat()
                    val inner = radius * 0.34f
                    val outer = radius
                    val rayColor = lerp(
                        colors[(ray + burstIndex) % colors.size],
                        Color.White,
                        0.2f
                    ).copy(alpha = fade)

                    drawLine(
                        color = rayColor,
                        start = androidx.compose.ui.geometry.Offset(
                            center.x + cos(angle) * inner,
                            center.y + sin(angle) * inner
                        ),
                        end = androidx.compose.ui.geometry.Offset(
                            center.x + cos(angle) * outer,
                            center.y + sin(angle) * outer
                        ),
                        strokeWidth = 5f
                    )

                    drawCircle(
                        color = rayColor,
                        radius = 5f,
                        center = androidx.compose.ui.geometry.Offset(
                            center.x + cos(angle) * outer,
                            center.y + sin(angle) * outer
                        )
                    )
                }
            }
        }
    }
}
