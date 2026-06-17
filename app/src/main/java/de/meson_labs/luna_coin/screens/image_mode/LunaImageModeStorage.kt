package de.meson_labs.luna_coin.screens.image_mode

import android.content.Context
import java.io.File

object LunaImageModeStorage {

    private const val PREFS_NAME = "luna_image_mode_prefs"

    private const val KEY_IMAGE_CHANGE_DELAY_MS = "image_change_delay_ms"
    private const val KEY_PLAY_MODE = "play_mode"

    private const val KEY_AUTO_START_ENABLED = "auto_start_enabled"
    private const val KEY_AUTO_START_DELAY_SECONDS = "auto_start_delay_seconds"

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

    fun getImageChangeDelayMs(
        context: Context
    ): Long {
        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        return prefs.getLong(
            KEY_IMAGE_CHANGE_DELAY_MS,
            LunaImageModeConfig.DEFAULT_IMAGE_CHANGE_DELAY_MS
        )
    }

    fun setImageChangeDelayMs(
        context: Context,
        delayMs: Long
    ) {
        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        prefs.edit()
            .putLong(
                KEY_IMAGE_CHANGE_DELAY_MS,
                delayMs
            )
            .apply()
    }

    fun getPlayMode(
        context: Context
    ): LunaImagePlayMode {
        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val value = prefs.getString(
            KEY_PLAY_MODE,
            LunaImagePlayMode.SEQUENTIAL.name
        )

        return try {
            LunaImagePlayMode.valueOf(
                value ?: LunaImagePlayMode.SEQUENTIAL.name
            )
        } catch (_: Exception) {
            LunaImagePlayMode.SEQUENTIAL
        }
    }

    fun setPlayMode(
        context: Context,
        playMode: LunaImagePlayMode
    ) {
        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        prefs.edit()
            .putString(
                KEY_PLAY_MODE,
                playMode.name
            )
            .apply()
    }

    fun isAutoStartEnabled(
        context: Context
    ): Boolean {
        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        return prefs.getBoolean(
            KEY_AUTO_START_ENABLED,
            true
        )
    }

    fun setAutoStartEnabled(
        context: Context,
        enabled: Boolean
    ) {
        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        prefs.edit()
            .putBoolean(
                KEY_AUTO_START_ENABLED,
                enabled
            )
            .apply()
    }

    fun getAutoStartDelaySeconds(
        context: Context
    ): Long {
        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        return prefs.getLong(
            KEY_AUTO_START_DELAY_SECONDS,
            10L
        )
    }

    fun setAutoStartDelaySeconds(
        context: Context,
        seconds: Long
    ) {
        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        prefs.edit()
            .putLong(
                KEY_AUTO_START_DELAY_SECONDS,
                seconds
            )
            .apply()
    }
}