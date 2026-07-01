package de.meson_labs.luna_coin.manager

import de.meson_labs.luna_coin.data.repository.DataRepository
import de.meson_labs.luna_coin.models.LunaCoinData

class BackupManager(
    private val repository: DataRepository
) {

    suspend fun createCloudBackup(data: LunaCoinData) {
        repository.createCloudBackup(data)
    }

    suspend fun loadSafeCloudBackup(): LunaCoinData? {
        val backup = repository.loadCloudBackup()

        return if (backup != null && backup.children.isNotEmpty()) {
            sortChildrenInData(ensureBuiltInAdmin(backup))
        } else {
            null
        }
    }

    suspend fun persistRestoredBackup(data: LunaCoinData) {
        repository.saveData(data)
    }

    fun getImportFromJsonMessage(): String {
        return "📂 JSON-Import wird vorbereitet..."
    }
}
