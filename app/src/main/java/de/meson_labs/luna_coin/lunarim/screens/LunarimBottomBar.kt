package de.meson_labs.luna_coin.lunarim

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.meson_labs.luna_coin.R
import de.meson_labs.luna_coin.lunarim.screens.LunarimDestination

@Composable
internal fun LunarimBottomBar(
    currentDestination: LunarimDestination,
    onDestinationSelected: (LunarimDestination) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LunarimBottomBarColors.background)
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = LunarimBottomBarColors.runeGold.copy(alpha = 0.70f)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            LunarimBottomBarColors.backgroundRaised,
                            LunarimBottomBarColors.background
                        )
                    )
                )
                .padding(
                    start = 3.dp,
                    end = 3.dp,
                    top = 1.dp,
                    bottom = 2.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.Top
        ) {
            LunarimDestination.entries.forEach { destination ->
                LunarimNavigationItem(
                    modifier = Modifier.weight(1f),
                    destination = destination,
                    selected = currentDestination == destination,
                    onClick = {
                        onDestinationSelected(destination)
                    }
                )
            }
        }
    }
}

@Composable
private fun LunarimNavigationItem(
    modifier: Modifier,
    destination: LunarimDestination,
    selected: Boolean,
    onClick: () -> Unit
) {
    val selectedLineColor by animateColorAsState(
        targetValue = if (selected) {
            LunarimBottomBarColors.runeGold
        } else {
            Color.Transparent
        },
        label = "LunarimNavSelectedLine"
    )

    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            LunarimBottomBarColors.selectedBackground
        } else {
            Color.Transparent
        },
        label = "LunarimNavBackground"
    )

    val iconSize by animateDpAsState(
        targetValue = if (selected) 66.dp else 62.dp,
        label = "LunarimNavIconSize"
    )

    Box(
        modifier = modifier
            .height(71.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
    ) {
        /*
         * Statt eines Rahmens markiert nur diese Linie den aktiven Tab.
         * So kann die Auswahlmarkierung die Beschriftung nicht mehr verdecken.
         */
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.72f)
                .height(2.dp)
                .background(selectedLineColor)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 1.dp,
                    end = 1.dp,
                    top = 1.dp,
                    bottom = 1.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            /*
             * Das Bildfenster ist absichtlich flacher als das Bild.
             * Das größere Bild wird darin beschnitten, wodurch der dunkle
             * Außenrand der PNG-Dateien nicht mehr sichtbar ist.
             */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(LunarimBottomBarColors.imageEdgeBackground),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(destination.iconResource()),
                    contentDescription = destination.title,
                    modifier = Modifier
                        .size(iconSize)
                        .offset(y = (-2).dp)
                        .alpha(if (selected) 1f else 0.88f),
                    contentScale = ContentScale.Fit
                )
            }

            Text(
                text = destination.title.uppercase(),
                modifier = Modifier.offset(y = (-1).dp),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                lineHeight = 13.sp,
                fontWeight = if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Medium
                },
                color = if (selected) {
                    LunarimBottomBarColors.parchment
                } else {
                    LunarimBottomBarColors.secondaryText
                }
            )
        }
    }
}

@DrawableRes
private fun LunarimDestination.iconResource(): Int {
    return when (this) {
        LunarimDestination.CAMP -> R.drawable.lunarim_nav_camp
        LunarimDestination.CHARACTER -> R.drawable.lunarim_nav_character
        LunarimDestination.SHOP -> R.drawable.lunarim_nav_shop
        LunarimDestination.MAP -> R.drawable.lunarim_nav_map
    }
}

private object LunarimBottomBarColors {
    /*
     * An die dunklen Außenränder der Navigationsbilder angepasst.
     * Dadurch verschmelzen die Bilder optisch besser mit der Leiste.
     */
    val background = Color(0xFF14130F)
    val backgroundRaised = Color(0xFF1B1914)
    val imageEdgeBackground = Color(0xFF171611)
    val selectedBackground = Color(0xFF242119)
    val runeGold = Color(0xFFD1AC62)
    val parchment = Color(0xFFE8E0CF)
    val secondaryText = Color(0xFFB2AA9C)
}
