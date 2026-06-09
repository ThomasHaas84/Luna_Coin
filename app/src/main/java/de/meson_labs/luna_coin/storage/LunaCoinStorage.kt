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
        private const val BACKUP_FILE_NAME = "luna_coin_backup.json"
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

    fun saveBackup(data: LunaCoinData): Boolean {
        return try {
            val file = File(
                context.filesDir,
                BACKUP_FILE_NAME
            )

            val jsonText = json.encodeToString(data)

            file.writeText(jsonText)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun loadBackup(): LunaCoinData? {
        return try {
            val file = File(
                context.filesDir,
                BACKUP_FILE_NAME
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

    fun backupExists(): Boolean {
        return File(
            context.filesDir,
            BACKUP_FILE_NAME
        ).exists()
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

    fun getBackupJsonText(): String {
        return try {
            val file = File(
                context.filesDir,
                BACKUP_FILE_NAME
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