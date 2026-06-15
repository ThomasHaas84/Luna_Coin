package de.meson_labs.luna_coin.data.repository

import de.meson_labs.luna_coin.models.LunaCoinData

interface DataRepository {
    suspend fun loadData(): LunaCoinData?
    suspend fun saveData(data: LunaCoinData)
}