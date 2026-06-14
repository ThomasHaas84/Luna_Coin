package de.meson_labs.luna_coin.screens.image_mode

import de.meson_labs.luna_coin.R

enum class LunaImagePlayMode {
    SEQUENTIAL,
    RANDOM
}

object LunaImageModeConfig {

    const val AUTO_START_DELAY_MS = 10_000L
    const val DEFAULT_IMAGE_CHANGE_DELAY_MS = 10_000L

    const val MIN_IMAGE_CHANGE_DELAY_SECONDS = 1L
    const val MAX_IMAGE_CHANGE_DELAY_SECONDS = 300L

    val images = listOf(
        R.drawable.cyberluna,
        R.drawable.gtl,
        R.drawable.lunarim,
        R.drawable.reddeadluna,
        R.drawable.witcher_luna
    )
}