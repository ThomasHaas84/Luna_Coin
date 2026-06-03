package de.meson_labs.luna_coin.storage

import android.content.Context
import de.meson_labs.luna_coin.models.LunaCoinData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class LunaCoinStorage(
    private val context: Context
) {

    companion object {
        private const val FILE_NAME = "luna_coin_data.json"
    }

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun loadData(): LunaCoinData? {
        return try {

            val file = File(
                context.filesDir,
                FILE_NAME
            )

            if (!file.exists()) {
                null
            } else {
                json.decodeFromString<LunaCoinData>(
                    file.readText()
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveData(data: LunaCoinData) {

        try {

            val file = File(
                context.filesDir,
                FILE_NAME
            )

            val jsonText = json.encodeToString(data)

            file.writeText(jsonText)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteData() {

        try {

            val file = File(
                context.filesDir,
                FILE_NAME
            )

            if (file.exists()) {
                file.delete()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getJsonText(): String {

        return try {

            val file = File(
                context.filesDir,
                FILE_NAME
            )

            if (file.exists()) {
                file.readText()
            } else {
                ""
            }

        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun fileExists(): Boolean {

        return File(
            context.filesDir,
            FILE_NAME
        ).exists()
    }
}