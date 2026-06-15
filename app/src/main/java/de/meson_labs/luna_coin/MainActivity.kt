package de.meson_labs.luna_coin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import de.meson_labs.luna_coin.data.repository.DataRepository
import de.meson_labs.luna_coin.data.repository.FirestoreRepository
import de.meson_labs.luna_coin.screens.MainScreen
import de.meson_labs.luna_coin.sound.LunaSoundManager
import de.meson_labs.luna_coin.storage.LunaCoinStorage
import de.meson_labs.luna_coin.ui.theme.LunaCoinTheme
import de.meson_labs.luna_coin.viewmodel.LunaCoinViewModel
import de.meson_labs.luna_coin.viewmodel.LunaCoinViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LunaSoundManager.init(context = applicationContext)

        val legacyStorage = LunaCoinStorage(context = applicationContext)

        // Neues Firestore Repository
        val repository: DataRepository = FirestoreRepository()

        setContent {
            LunaCoinTheme {
                val viewModel: LunaCoinViewModel = viewModel(        // ← Typ explizit angegeben
                    factory = LunaCoinViewModelFactory(
                        repository = repository,
                        legacyStorage = legacyStorage
                    )
                )

                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onDestroy() {
        LunaSoundManager.release()
        super.onDestroy()
    }
}