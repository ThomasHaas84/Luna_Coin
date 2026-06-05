package de.meson_labs.luna_coin.screens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.R
import de.meson_labs.luna_coin.components.CoinDisplay
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

data class LuckyWheelResult(
    val rewardCoins: Int,
    val message: String,
    val isFortuneCookie: Boolean
)

private data class LuckyWheelSegment(
    val label: String,
    val color: Color,
    val rewardCoins: Int,
    val isFortuneCookie: Boolean,
    val message: String
)

@Composable
fun LuckyWheelShopCard(
    currentCoins: Int,
    isFreeToday: Boolean,
    isPurchaseLocked: Boolean,
    onSpinClick: () -> Unit
) {
    val price = if (isFreeToday) 0 else 1
    val canSpin = isFreeToday || currentCoins >= price

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Glücksrad",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = if (isFreeToday) {
                        "Einmal pro Tag kostenlos drehen."
                    } else {
                        "Heute schon kostenlos gedreht. Jeder weitere Dreh kostet 1 Luna Coin."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                if (price > 0) {
                    CoinDisplay(amount = price)
                } else {
                    Text(
                        text = "Heute kostenlos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (!canSpin) {
                    Text(
                        text = "Nicht genug Coins",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Button(
                onClick = onSpinClick,
                enabled = canSpin && !isPurchaseLocked
            ) {
                Text("Drehen")
            }
        }
    }
}

@Composable
fun LuckyWheelDialog(
    onDismiss: () -> Unit,
    onResult: (LuckyWheelResult) -> Unit
) {
    val wheelSegments = remember {
        createLuckyWheelSegments()
    }

    val selectedSegment = remember {
        wheelSegments.random()
    }

    var result by remember {
        mutableStateOf<LuckyWheelResult?>(null)
    }

    var targetRotation by remember {
        mutableFloatStateOf(0f)
    }

    val rotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(
            durationMillis = 6500,
            easing = CubicBezierEasing(
                0.08f,
                0.95f,
                0.12f,
                1f
            )
        ),
        label = "luckyWheelRotation"
    )

    LaunchedEffect(Unit) {
        val selectedIndex = wheelSegments.indexOf(selectedSegment)

        targetRotation = calculateTargetRotation(
            selectedIndex = selectedIndex,
            segmentCount = wheelSegments.size
        )

        delay(6600)

        val newResult = LuckyWheelResult(
            rewardCoins = selectedSegment.rewardCoins,
            message = selectedSegment.message,
            isFortuneCookie = selectedSegment.isFortuneCookie
        )

        result = newResult
        onResult(newResult)
    }

    AlertDialog(
        onDismissRequest = {
            if (result != null) {
                onDismiss()
            }
        },
        title = {
            Text("Glücksrad")
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier.size(260.dp)
                    ) {
                        val wheelSize = size.minDimension
                        val radius = wheelSize / 2f

                        val topLeft = Offset(
                            x = center.x - radius,
                            y = center.y - radius
                        )

                        val sectionCount = wheelSegments.size
                        val sweepAngle = 360f / sectionCount

                        rotate(rotation) {
                            wheelSegments.forEachIndexed { index, segment ->
                                drawArc(
                                    color = segment.color,
                                    startAngle = index * sweepAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = true,
                                    topLeft = topLeft,
                                    size = Size(
                                        width = wheelSize,
                                        height = wheelSize
                                    )
                                )
                            }

                            repeat(sectionCount) { index ->
                                val angle =
                                    Math.toRadians((index * sweepAngle).toDouble())

                                val endX =
                                    center.x + cos(angle).toFloat() * radius

                                val endY =
                                    center.y + sin(angle).toFloat() * radius

                                drawLine(
                                    color = Color.White.copy(alpha = 0.65f),
                                    start = center,
                                    end = Offset(
                                        x = endX,
                                        y = endY
                                    ),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                        }

                        drawCircle(
                            color = Color.Black.copy(alpha = 0.18f),
                            radius = radius,
                            center = center,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 4.dp.toPx()
                            )
                        )

                        drawLine(
                            color = Color.Black,
                            start = Offset(
                                x = center.x,
                                y = -2.dp.toPx()
                            ),
                            end = Offset(
                                x = center.x - 14.dp.toPx(),
                                y = 34.dp.toPx()
                            ),
                            strokeWidth = 6.dp.toPx()
                        )

                        drawLine(
                            color = Color.Black,
                            start = Offset(
                                x = center.x,
                                y = -2.dp.toPx()
                            ),
                            end = Offset(
                                x = center.x + 14.dp.toPx(),
                                y = 34.dp.toPx()
                            ),
                            strokeWidth = 6.dp.toPx()
                        )
                    }

                    LuckyWheelLabels(
                        segments = wheelSegments,
                        rotation = rotation
                    )

                    Image(
                        painter = painterResource(
                            id = R.drawable.luna_coin_small
                        ),
                        contentDescription = "Luna Coin",
                        modifier = Modifier.size(58.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = result?.message ?: "Das Glücksrad dreht sich...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                enabled = result != null
            ) {
                Text("OK")
            }
        }
    )
}

@Composable
private fun LuckyWheelLabels(
    segments: List<LuckyWheelSegment>,
    rotation: Float
) {
    Canvas(
        modifier = Modifier.size(260.dp)
    ) {
        val sectionCount = segments.size
        val sweepAngle = 360f / sectionCount
        val labelRadius = size.minDimension * 0.34f

        segments.forEachIndexed { index, segment ->
            val centerAngleDegrees =
                index * sweepAngle + sweepAngle / 2f + rotation

            val angleRadians =
                Math.toRadians(centerAngleDegrees.toDouble())

            val labelX =
                center.x + cos(angleRadians).toFloat() * labelRadius

            val labelY =
                center.y + sin(angleRadians).toFloat() * labelRadius

            drawCircle(
                color = Color.White.copy(alpha = 0.78f),
                radius = 16.dp.toPx(),
                center = Offset(
                    x = labelX,
                    y = labelY
                )
            )

            drawContext.canvas.nativeCanvas.drawText(
                segment.label,
                labelX,
                labelY + 5.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 12.dp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                    isAntiAlias = true
                }
            )
        }
    }
}

private fun createLuckyWheelSegments(): List<LuckyWheelSegment> {
    val fortuneMessages = listOf(
        "Dein Glückskeks sagt: Heute ist ein guter Tag für Luna Coins.",
        "Dein Glückskeks sagt: Wer aufräumt, gewinnt.",
        "Dein Glückskeks sagt: Kleine Aufgaben bringen großes Glück.",
        "Dein Glückskeks sagt: Ein Lächeln zählt auch als Bonus.",
        "Dein Glückskeks sagt: Morgen wartet neues Glück.",
        "Dein Glückskeks sagt: Das Sofa ruft, aber die Aufgabe ruft lauter.",
        "Dein Glückskeks sagt: Ordnung ist nur Chaos mit Plan.",
        "Dein Glückskeks sagt: Ein Luna Coin kommt selten allein."
    )

    return listOf(
        LuckyWheelSegment(
            label = "Nix",
            color = Color(0xFF94A3B8),
            rewardCoins = 0,
            isFortuneCookie = false,
            message = "Leider nichts gewonnen."
        ),
        LuckyWheelSegment(
            label = "Nix",
            color = Color(0xFF64748B),
            rewardCoins = 0,
            isFortuneCookie = false,
            message = "Leider nichts gewonnen."
        ),
        LuckyWheelSegment(
            label = "Nix",
            color = Color(0xFF94A3B8),
            rewardCoins = 0,
            isFortuneCookie = false,
            message = "Leider nichts gewonnen."
        ),
        LuckyWheelSegment(
            label = "Nix",
            color = Color(0xFF64748B),
            rewardCoins = 0,
            isFortuneCookie = false,
            message = "Leider nichts gewonnen."
        ),
        LuckyWheelSegment(
            label = "+1",
            color = Color(0xFF3B82F6),
            rewardCoins = 1,
            isFortuneCookie = false,
            message = "Du hast 1 Luna Coin gewonnen!"
        ),
        LuckyWheelSegment(
            label = "+1",
            color = Color(0xFF60A5FA),
            rewardCoins = 1,
            isFortuneCookie = false,
            message = "Du hast 1 Luna Coin gewonnen!"
        ),
        LuckyWheelSegment(
            label = "+1",
            color = Color(0xFF2563EB),
            rewardCoins = 1,
            isFortuneCookie = false,
            message = "Du hast 1 Luna Coin gewonnen!"
        ),
        LuckyWheelSegment(
            label = "Keks",
            color = Color(0xFFFACC15),
            rewardCoins = 0,
            isFortuneCookie = true,
            message = fortuneMessages.random()
        ),
        LuckyWheelSegment(
            label = "Keks",
            color = Color(0xFFEAB308),
            rewardCoins = 0,
            isFortuneCookie = true,
            message = fortuneMessages.random()
        ),
        LuckyWheelSegment(
            label = "Keks",
            color = Color(0xFFFDE047),
            rewardCoins = 0,
            isFortuneCookie = true,
            message = fortuneMessages.random()
        ),
        LuckyWheelSegment(
            label = "+2",
            color = Color(0xFF22C55E),
            rewardCoins = 2,
            isFortuneCookie = false,
            message = "Du hast 2 Luna Coins gewonnen!"
        ),
        LuckyWheelSegment(
            label = "+3",
            color = Color(0xFFA855F7),
            rewardCoins = 3,
            isFortuneCookie = false,
            message = "Du hast 3 Luna Coins gewonnen!"
        )
    )
}

private fun calculateTargetRotation(
    selectedIndex: Int,
    segmentCount: Int
): Float {
    val sweepAngle = 360f / segmentCount

    val selectedSegmentCenterAngle =
        selectedIndex * sweepAngle + sweepAngle / 2f

    val pointerAngle = 270f

    val baseRotation =
        pointerAngle - selectedSegmentCenterAngle

    val fullRotations = 360f * 10f

    return fullRotations + baseRotation
}