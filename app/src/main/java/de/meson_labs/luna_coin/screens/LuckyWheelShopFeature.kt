package de.meson_labs.luna_coin.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.R
import de.meson_labs.luna_coin.components.CoinDisplay
import de.meson_labs.luna_coin.data.FortuneCookieMessages
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

    val scrollState = rememberScrollState()

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

    LaunchedEffect(result) {
        if (result?.isFortuneCookie == true) {
            delay(350)
            scrollState.animateScrollTo(
                scrollState.maxValue
            )
        }
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
                modifier = Modifier.verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier.size(420.dp)
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
                            style = Stroke(
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
                        modifier = Modifier.size(180.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                if (result?.isFortuneCookie == true) {
                    Spacer(modifier = Modifier.height(12.dp))

                    FortuneCookieBreakAnimation()
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (result?.isFortuneCookie == true) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF8DC)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🥠 Glückskeks",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color(0xFFEAB308)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = result?.message ?: "",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                } else {
                    Text(
                        text = result?.message ?: "Das Glücksrad dreht sich...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
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
private fun FortuneCookieBreakAnimation() {
    val progress = remember {
        Animatable(0f)
    }

    LaunchedEffect(Unit) {
        progress.snapTo(0f)

        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 900,
                easing = FastOutSlowInEasing
            )
        )
    }

    Canvas(
        modifier = Modifier.size(
            width = 220.dp,
            height = 110.dp
        )
    ) {
        val animatedProgress = progress.value

        val centerY = size.height * 0.56f
        val centerX = size.width / 2f

        val leftOffset = -42.dp.toPx() * animatedProgress
        val rightOffset = 42.dp.toPx() * animatedProgress
        val lift = -10.dp.toPx() * animatedProgress

        val cookieBase = Color(0xFFD89224)
        val cookieEdge = Color(0xFF7A430B)
        val cookieLight = Color(0xFFFFD074)
        val cookieShadow = Color(0xFF5E3408)

        val paperAlpha = ((animatedProgress - 0.22f) / 0.78f)
            .coerceIn(0f, 1f)

        drawOval(
            color = Color.Black.copy(alpha = 0.16f),
            topLeft = Offset(
                x = centerX - 68.dp.toPx(),
                y = centerY + 23.dp.toPx()
            ),
            size = Size(
                width = 136.dp.toPx(),
                height = 18.dp.toPx()
            )
        )

        rotate(
            degrees = -18f * animatedProgress,
            pivot = Offset(centerX + leftOffset, centerY + lift)
        ) {
            translate(left = leftOffset, top = lift) {
                drawCookieHalf(
                    center = Offset(centerX, centerY),
                    isLeft = true,
                    cookieColor = cookieBase,
                    cookieDark = cookieEdge,
                    cookieLight = cookieLight,
                    cookieShadow = cookieShadow
                )
            }
        }

        rotate(
            degrees = 18f * animatedProgress,
            pivot = Offset(centerX + rightOffset, centerY + lift)
        ) {
            translate(left = rightOffset, top = lift) {
                drawCookieHalf(
                    center = Offset(centerX, centerY),
                    isLeft = false,
                    cookieColor = cookieBase,
                    cookieDark = cookieEdge,
                    cookieLight = cookieLight,
                    cookieShadow = cookieShadow
                )
            }
        }

        if (paperAlpha > 0f) {
            val paperWidth = 98.dp.toPx()
            val paperHeight = 20.dp.toPx()

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = paperAlpha),
                        Color(0xFFE5E7EB).copy(alpha = paperAlpha)
                    )
                ),
                topLeft = Offset(
                    x = centerX - paperWidth / 2f,
                    y = centerY - 13.dp.toPx()
                ),
                size = Size(
                    width = paperWidth,
                    height = paperHeight
                ),
                cornerRadius = CornerRadius(
                    x = 6.dp.toPx(),
                    y = 6.dp.toPx()
                )
            )

            drawLine(
                color = Color(0xFF94A3B8).copy(alpha = paperAlpha * 0.7f),
                start = Offset(centerX - 33.dp.toPx(), centerY - 5.dp.toPx()),
                end = Offset(centerX + 33.dp.toPx(), centerY - 5.dp.toPx()),
                strokeWidth = 1.4.dp.toPx()
            )

            drawLine(
                color = Color(0xFF94A3B8).copy(alpha = paperAlpha * 0.5f),
                start = Offset(centerX - 24.dp.toPx(), centerY + 3.dp.toPx()),
                end = Offset(centerX + 24.dp.toPx(), centerY + 3.dp.toPx()),
                strokeWidth = 1.2.dp.toPx()
            )
        }
    }
}

private fun DrawScope.drawCookieHalf(
    center: Offset,
    isLeft: Boolean,
    cookieColor: Color,
    cookieDark: Color,
    cookieLight: Color,
    cookieShadow: Color
) {
    val height = 60.dp.toPx()
    val path = Path()

    if (isLeft) {
        path.moveTo(center.x + 2.dp.toPx(), center.y - height / 2f)
        path.cubicTo(center.x - 46.dp.toPx(), center.y - 42.dp.toPx(), center.x - 76.dp.toPx(), center.y - 5.dp.toPx(), center.x - 55.dp.toPx(), center.y + 31.dp.toPx())
        path.cubicTo(center.x - 37.dp.toPx(), center.y + 55.dp.toPx(), center.x - 9.dp.toPx(), center.y + 36.dp.toPx(), center.x + 1.dp.toPx(), center.y + 23.dp.toPx())
        path.lineTo(center.x - 9.dp.toPx(), center.y + 14.dp.toPx())
        path.lineTo(center.x + 3.dp.toPx(), center.y + 5.dp.toPx())
        path.lineTo(center.x - 7.dp.toPx(), center.y - 5.dp.toPx())
        path.lineTo(center.x + 4.dp.toPx(), center.y - 17.dp.toPx())
        path.close()
    } else {
        path.moveTo(center.x - 2.dp.toPx(), center.y - height / 2f)
        path.cubicTo(center.x + 46.dp.toPx(), center.y - 42.dp.toPx(), center.x + 76.dp.toPx(), center.y - 5.dp.toPx(), center.x + 55.dp.toPx(), center.y + 31.dp.toPx())
        path.cubicTo(center.x + 37.dp.toPx(), center.y + 55.dp.toPx(), center.x + 9.dp.toPx(), center.y + 36.dp.toPx(), center.x - 1.dp.toPx(), center.y + 23.dp.toPx())
        path.lineTo(center.x + 9.dp.toPx(), center.y + 14.dp.toPx())
        path.lineTo(center.x - 3.dp.toPx(), center.y + 5.dp.toPx())
        path.lineTo(center.x + 7.dp.toPx(), center.y - 5.dp.toPx())
        path.lineTo(center.x - 4.dp.toPx(), center.y - 17.dp.toPx())
        path.close()
    }

    drawPath(
        path = path,
        brush = Brush.linearGradient(
            colors = listOf(
                cookieLight,
                cookieColor,
                cookieDark.copy(alpha = 0.82f)
            ),
            start = Offset(center.x, center.y - 34.dp.toPx()),
            end = Offset(center.x, center.y + 36.dp.toPx())
        )
    )

    drawPath(
        path = path,
        color = cookieDark.copy(alpha = 0.95f),
        style = Stroke(width = 3.dp.toPx())
    )

    drawOval(
        color = Color.White.copy(alpha = 0.16f),
        topLeft = if (isLeft) {
            Offset(center.x - 52.dp.toPx(), center.y - 25.dp.toPx())
        } else {
            Offset(center.x + 16.dp.toPx(), center.y - 25.dp.toPx())
        },
        size = Size(
            width = 34.dp.toPx(),
            height = 15.dp.toPx()
        )
    )

    drawArc(
        color = cookieShadow.copy(alpha = 0.34f),
        startAngle = if (isLeft) 100f else 260f,
        sweepAngle = if (isLeft) 112f else -112f,
        useCenter = false,
        topLeft = if (isLeft) {
            Offset(center.x - 60.dp.toPx(), center.y - 18.dp.toPx())
        } else {
            Offset(center.x + 4.dp.toPx(), center.y - 18.dp.toPx())
        },
        size = Size(
            width = 60.dp.toPx(),
            height = 54.dp.toPx()
        ),
        style = Stroke(width = 3.dp.toPx())
    )

    val crackX = if (isLeft) center.x - 3.dp.toPx() else center.x + 3.dp.toPx()
    val side = if (isLeft) -1f else 1f

    drawLine(
        color = cookieShadow.copy(alpha = 0.62f),
        start = Offset(crackX, center.y - 20.dp.toPx()),
        end = Offset(crackX + side * 8.dp.toPx(), center.y - 9.dp.toPx()),
        strokeWidth = 2.dp.toPx()
    )

    drawLine(
        color = cookieShadow.copy(alpha = 0.52f),
        start = Offset(crackX + side * 8.dp.toPx(), center.y - 9.dp.toPx()),
        end = Offset(crackX + side * 2.dp.toPx(), center.y + 6.dp.toPx()),
        strokeWidth = 1.7.dp.toPx()
    )

    drawLine(
        color = cookieShadow.copy(alpha = 0.42f),
        start = Offset(crackX + side * 2.dp.toPx(), center.y + 6.dp.toPx()),
        end = Offset(crackX + side * 10.dp.toPx(), center.y + 16.dp.toPx()),
        strokeWidth = 1.3.dp.toPx()
    )

    listOf(
        Offset(side * 34.dp.toPx(), 8.dp.toPx()) to 2.2.dp.toPx(),
        Offset(side * 24.dp.toPx(), -13.dp.toPx()) to 1.7.dp.toPx(),
        Offset(side * 47.dp.toPx(), -2.dp.toPx()) to 1.4.dp.toPx()
    ).forEach { crumb ->
        drawCircle(
            color = cookieDark.copy(alpha = 0.28f),
            radius = crumb.second,
            center = Offset(
                x = center.x + crumb.first.x,
                y = center.y + crumb.first.y
            )
        )
    }
}

@Composable
private fun LuckyWheelLabels(
    segments: List<LuckyWheelSegment>,
    rotation: Float
) {
    Canvas(
        modifier = Modifier.size(420.dp)
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
    val fortuneMessages = FortuneCookieMessages.messages

    return listOf(
        LuckyWheelSegment(
            label = "Nix",
            color = Color(0xFF94A3B8),
            rewardCoins = 0,
            isFortuneCookie = false,
            message = "Leider nichts gewonnen."
        ),
        LuckyWheelSegment(
            label = "Keks",
            color = Color(0xFFFACC15),
            rewardCoins = 0,
            isFortuneCookie = true,
            message = fortuneMessages.random()
        ),
        LuckyWheelSegment(
            label = "Nix",
            color = Color(0xFF64748B),
            rewardCoins = 0,
            isFortuneCookie = false,
            message = "Leider nichts gewonnen."
        ),
        LuckyWheelSegment(
            label = "+2",
            color = Color(0xFF2563EB),
            rewardCoins = 2,
            isFortuneCookie = false,
            message = "Du hast 2 Luna Coins gewonnen!"
        ),
        LuckyWheelSegment(
            label = "Keks",
            color = Color(0xFFEAB308),
            rewardCoins = 0,
            isFortuneCookie = true,
            message = fortuneMessages.random()
        ),
        LuckyWheelSegment(
            label = "Nix",
            color = Color(0xFF94A3B8),
            rewardCoins = 0,
            isFortuneCookie = false,
            message = "Leider nichts gewonnen."
        ),
        LuckyWheelSegment(
            label = "+1",
            color = Color(0xFF22C55E),
            rewardCoins = 1,
            isFortuneCookie = false,
            message = "Du hast 1 Luna Coin gewonnen!"
        ),
        LuckyWheelSegment(
            label = "Keks",
            color = Color(0xFFFDE047),
            rewardCoins = 0,
            isFortuneCookie = true,
            message = fortuneMessages.random()
        ),
        LuckyWheelSegment(
            label = "Nix",
            color = Color(0xFF64748B),
            rewardCoins = 0,
            isFortuneCookie = false,
            message = "Leider nichts gewonnen."
        ),
        LuckyWheelSegment(
            label = "+3",
            color = Color(0xFFA855F7),
            rewardCoins = 3,
            isFortuneCookie = false,
            message = "Du hast 3 Luna Coins gewonnen!"
        ),
        LuckyWheelSegment(
            label = "Keks",
            color = Color(0xFFFACC15),
            rewardCoins = 0,
            isFortuneCookie = true,
            message = fortuneMessages.random()
        ),
        LuckyWheelSegment(
            label = "+2",
            color = Color(0xFF3B82F6),
            rewardCoins = 2,
            isFortuneCookie = false,
            message = "Du hast 2 Luna Coins gewonnen!"
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