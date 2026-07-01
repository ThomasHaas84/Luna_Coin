package de.meson_labs.luna_coin.storage

import android.content.Context

object TrustedDeviceStorage {

    private const val PREFS_NAME = "luna_coin_trusted_device_prefs"
    private const val KEY_PREFIX_TRUSTED_USER = "trusted_user_"

    fun isTrusted(
        context: Context,
        childId: String
    ): Boolean {
        if (childId.isBlank()) return false

        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        return prefs.getBoolean(
            KEY_PREFIX_TRUSTED_USER + childId,
            false
        )
    }

    fun setTrusted(
        context: Context,
        childId: String,
        trusted: Boolean
    ) {
        if (childId.isBlank()) return

        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        prefs.edit()
            .putBoolean(
                KEY_PREFIX_TRUSTED_USER + childId,
                trusted
            )
            .apply()
    }

    fun clearTrusted(
        context: Context,
        childId: String
    ) {
        if (childId.isBlank()) return

        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        prefs.edit()
            .remove(KEY_PREFIX_TRUSTED_USER + childId)
            .apply()
    }

    fun clearAllTrustedUsers(
        context: Context
    ) {
        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        prefs.edit()
            .clear()
            .apply()
    }
}