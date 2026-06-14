package de.meson_labs.luna_coin.screens.image_mode

import android.content.Context
import java.io.File

object LunaImageModeStorage {

    fun getImageFiles(
        context: Context
    ): List<File> {

        val imageDir = File(
            context.getExternalFilesDir(null),
            "Bilderrahmen"
        )

        if (!imageDir.exists()) {
            imageDir.mkdirs()
        }

        return imageDir.listFiles()
            ?.filter {
                it.extension.lowercase() in listOf(
                    "jpg",
                    "jpeg",
                    "png",
                    "webp"
                )
            }
            ?.sortedBy {
                it.name
            }
            ?: emptyList()
    }
}