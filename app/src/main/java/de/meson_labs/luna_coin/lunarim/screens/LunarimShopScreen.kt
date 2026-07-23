package de.meson_labs.luna_coin.lunarim.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.meson_labs.luna_coin.models.Child

/**
 * Platzhalter für den zukünftigen Lunarim-Shop.
 *
 * onSaveCurrentPlayer muss nach jedem Kauf oder Verkauf aufgerufen werden,
 * sobald die eigentliche Shop-Logik ergänzt wird.
 *
 * Bei einem Handel mit einem anderen Spieler darf später nicht nur dieser
 * Callback verwendet werden. Dafür steht in LunarimCloudStorage die atomare
 * Methode savePlayerTrade(buyerState, sellerState) bereit.
 */
@Composable
internal fun LunarimShopScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?,
    onSaveCurrentPlayer: () -> Unit
) {
    LunarimPlaceholderScreen(
        modifier = modifier.fillMaxSize(),
        title = "Shop",
        symbol = "",
        text = selectedChild?.name
            ?.takeIf { name -> name.isNotBlank() }
            ?.let { name ->
                "Hier kann $name später Gegenstände kaufen, anbieten und auf dem Familienmarkt handeln."
            }
            ?: "Hier entstehen später der Systemshop und der Familienmarkt."
    )

    /*
     * onSaveCurrentPlayer wird absichtlich noch nicht automatisch aufgerufen:
     * Der aktuelle Shop ist nur ein Platzhalter und führt noch keinen Kauf
     * oder Verkauf aus. Sobald die Shop-Aktionen eingebaut werden, wird der
     * Callback direkt nach jeder erfolgreichen Aktion ausgelöst.
     */
}
