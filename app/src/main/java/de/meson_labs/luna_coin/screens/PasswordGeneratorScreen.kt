package de.meson_labs.luna_coin.screens

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.tools.LunaPasswordGenerator
import org.json.JSONArray
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val PASSWORD_FAVORITES_PREFS = "password_generator_favorites"
private const val MAX_PASSWORD_FAVORITES = 10
private const val PASSWORD_KEY_ALIAS = "luna_coin_password_favorites_key"
private const val ENCRYPTED_VALUE_PREFIX = "enc_v1:"

private data class PasswordFavorite(
    val password: String,
    val comment: String
)

@Composable
fun PasswordGeneratorScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isPhone = configuration.smallestScreenWidthDp < 600
    val screenPadding = if (isPhone) 14.dp else 24.dp
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val userId = selectedChild?.id ?: "unknown_user"

    var currentPassword by remember { mutableStateOf(LunaPasswordGenerator.generate()) }
    var previousPasswords by remember { mutableStateOf(emptyList<String>()) }
    val favorites = remember(userId) { mutableStateListOf<PasswordFavorite>() }
    val visibleFavoritePasswords = remember(userId) { mutableStateMapOf<Int, Boolean>() }
    var favoritesLoaded by remember(userId) { mutableStateOf(false) }
    var favoriteToDeleteIndex by remember(userId) { mutableStateOf<Int?>(null) }

    LaunchedEffect(userId) {
        favorites.clear()
        favorites.addAll(loadPasswordFavorites(context, userId))
        visibleFavoritePasswords.clear()
        favoritesLoaded = true
    }

    fun persistFavorites() {
        savePasswordFavorites(
            context = context,
            userId = userId,
            favorites = favorites
        )
    }

    fun generateNextPassword() {
        previousPasswords = (listOf(currentPassword) + previousPasswords).take(10)
        currentPassword = LunaPasswordGenerator.generate()
    }

    fun copyPassword(password: String) {
        clipboardManager.setText(AnnotatedString(password))
        Toast.makeText(
            context,
            "Passwort kopiert",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun addCurrentPasswordToFavorites() {
        if (!favoritesLoaded) {
            return
        }

        if (favorites.any { it.password == currentPassword }) {
            Toast.makeText(
                context,
                "Dieses Passwort ist bereits in den Favoriten",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (favorites.size >= MAX_PASSWORD_FAVORITES) {
            Toast.makeText(
                context,
                "Es können maximal 10 Favoriten gespeichert werden",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        favorites.add(
            0,
            PasswordFavorite(
                password = currentPassword,
                comment = ""
            )
        )
        persistFavorites()

        Toast.makeText(
            context,
            "Passwort zu Favoriten hinzugefügt",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun deleteFavorite(index: Int) {
        if (index !in favorites.indices) {
            return
        }

        favorites.removeAt(index)
        visibleFavoritePasswords.clear()
        persistFavorites()

        Toast.makeText(
            context,
            "Favorit gelöscht",
            Toast.LENGTH_SHORT
        ).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(screenPadding)
    ) {
        LunaScreenHeader(
            title = "Passwort-Generator",
            selectedChild = selectedChild,
            onLogout = onLogout
        )

        Spacer(modifier = Modifier.height(if (isPhone) 12.dp else 14.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zurück zu Games & Tools")
        }

        Spacer(modifier = Modifier.height(if (isPhone) 16.dp else 24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isPhone) 16.dp else 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Aktuelles Passwort:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(if (isPhone) 18.dp else 22.dp))

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(
                            min = if (isPhone) 82.dp else 104.dp
                        )
                        .padding(
                            horizontal = 4.dp,
                            vertical = if (isPhone) 14.dp else 18.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentPassword,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = if (isPhone) 20.sp else 28.sp,
                        lineHeight = if (isPhone) 27.sp else 36.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Spacer(modifier = Modifier.height(if (isPhone) 26.dp else 30.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = ::generateNextPassword,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text(
                            text = "🎲  Neu würfeln",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = { copyPassword(currentPassword) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text(
                            text = "📋  Kopieren",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = ::addCurrentPasswordToFavorites,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "⭐  Zu Favoriten",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(if (isPhone) 18.dp else 28.dp))

        Text(
            text = "Letzte Passwörter",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (previousPasswords.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                )
            ) {
                Text(
                    text = "Noch keine vorherigen Passwörter vorhanden.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                previousPasswords.forEachIndexed { index, password ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "${index + 1}.",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = password,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            OutlinedButton(
                                onClick = { copyPassword(password) }
                            ) {
                                Text("Kopieren")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(if (isPhone) 22.dp else 32.dp))

        Text(
            text = "Favoriten (${favorites.size}/$MAX_PASSWORD_FAVORITES)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (favorites.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                )
            ) {
                Text(
                    text = "Noch keine Favoriten gespeichert.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                favorites.forEachIndexed { index, favorite ->
                    val isPasswordVisible = visibleFavoritePasswords[index] == true

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Favorit ${index + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = favorite.password,
                                onValueChange = { newPassword ->
                                    favorites[index] = favorite.copy(password = newPassword)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Passwort") },
                                singleLine = false,
                                minLines = 1,
                                maxLines = 3,
                                visualTransformation = if (isPasswordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            visibleFavoritePasswords[index] =
                                                !isPasswordVisible
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) {
                                                Icons.Default.VisibilityOff
                                            } else {
                                                Icons.Default.Visibility
                                            },
                                            contentDescription = if (isPasswordVisible) {
                                                "Passwort ausblenden"
                                            } else {
                                                "Passwort anzeigen"
                                            }
                                        )
                                    }
                                }
                            )

                            OutlinedTextField(
                                value = favorite.comment,
                                onValueChange = { newComment ->
                                    favorites[index] = favorite.copy(comment = newComment)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Kommentar") },
                                placeholder = {
                                    Text("Zum Beispiel: WLAN, E-Mail oder Spielkonto")
                                },
                                minLines = 1,
                                maxLines = 3
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { copyPassword(favorites[index].password) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Kopieren")
                                }

                                OutlinedButton(
                                    onClick = { favoriteToDeleteIndex = index },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Löschen")
                                }
                            }

                            Button(
                                onClick = {
                                    val editedFavorite = favorites[index]

                                    if (editedFavorite.password.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            "Das Passwort darf nicht leer sein",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        persistFavorites()
                                        Toast.makeText(
                                            context,
                                            "Favorit gespeichert",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Speichern")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    favoriteToDeleteIndex?.let { index ->
        val favorite = favorites.getOrNull(index)

        if (favorite != null) {
            AlertDialog(
                onDismissRequest = { favoriteToDeleteIndex = null },
                title = { Text("Favorit löschen?") },
                text = {
                    Text(
                        "Soll dieser Passwort-Favorit wirklich gelöscht werden?"
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            deleteFavorite(index)
                            favoriteToDeleteIndex = null
                        }
                    ) {
                        Text("Ja")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { favoriteToDeleteIndex = null }
                    ) {
                        Text("Nein")
                    }
                }
            )
        } else {
            favoriteToDeleteIndex = null
        }
    }
}

private fun loadPasswordFavorites(
    context: Context,
    userId: String
): List<PasswordFavorite> {
    return runCatching {
        val preferences = context.getSharedPreferences(
            PASSWORD_FAVORITES_PREFS,
            Context.MODE_PRIVATE
        )

        val storedValue = preferences.getString(userId, null)
            ?: return emptyList()

        val jsonText: String
        val wasLegacyPlainText: Boolean

        if (storedValue.startsWith(ENCRYPTED_VALUE_PREFIX)) {
            jsonText = decryptPasswordFavorites(storedValue)
            wasLegacyPlainText = false
        } else {
            jsonText = storedValue
            wasLegacyPlainText = true
        }

        val favorites = parsePasswordFavorites(jsonText)

        if (wasLegacyPlainText && favorites.isNotEmpty()) {
            savePasswordFavorites(
                context = context,
                userId = userId,
                favorites = favorites
            )
        }

        favorites
    }.getOrElse {
        emptyList()
    }
}

private fun parsePasswordFavorites(
    jsonText: String
): List<PasswordFavorite> {
    val jsonArray = JSONArray(jsonText)

    return buildList {
        for (index in 0 until jsonArray.length()) {
            val item = jsonArray.optJSONObject(index) ?: continue
            val password = item.optString("password")
            val comment = item.optString("comment")

            if (password.isNotBlank()) {
                add(
                    PasswordFavorite(
                        password = password,
                        comment = comment
                    )
                )
            }
        }
    }.take(MAX_PASSWORD_FAVORITES)
}

private fun savePasswordFavorites(
    context: Context,
    userId: String,
    favorites: List<PasswordFavorite>
) {
    runCatching {
        val jsonArray = JSONArray()

        favorites
            .take(MAX_PASSWORD_FAVORITES)
            .forEach { favorite ->
                jsonArray.put(
                    JSONObject().apply {
                        put("password", favorite.password)
                        put("comment", favorite.comment)
                    }
                )
            }

        val encryptedValue = encryptPasswordFavorites(
            plainText = jsonArray.toString()
        )

        context.getSharedPreferences(
            PASSWORD_FAVORITES_PREFS,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(userId, encryptedValue)
            .apply()
    }
}

private fun encryptPasswordFavorites(
    plainText: String
): String {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(
        Cipher.ENCRYPT_MODE,
        getOrCreatePasswordFavoritesKey()
    )

    val encryptedBytes = cipher.doFinal(
        plainText.toByteArray(Charsets.UTF_8)
    )

    val ivBase64 = Base64.encodeToString(
        cipher.iv,
        Base64.NO_WRAP
    )

    val encryptedBase64 = Base64.encodeToString(
        encryptedBytes,
        Base64.NO_WRAP
    )

    return ENCRYPTED_VALUE_PREFIX + ivBase64 + ":" + encryptedBase64
}

private fun decryptPasswordFavorites(
    encryptedValue: String
): String {
    val encryptedParts = encryptedValue
        .removePrefix(ENCRYPTED_VALUE_PREFIX)
        .split(":", limit = 2)

    require(encryptedParts.size == 2) {
        "Ungültiges Format der verschlüsselten Passwort-Favoriten."
    }

    val iv = Base64.decode(
        encryptedParts[0],
        Base64.NO_WRAP
    )

    val encryptedBytes = Base64.decode(
        encryptedParts[1],
        Base64.NO_WRAP
    )

    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(
        Cipher.DECRYPT_MODE,
        getOrCreatePasswordFavoritesKey(),
        GCMParameterSpec(128, iv)
    )

    return cipher.doFinal(encryptedBytes)
        .toString(Charsets.UTF_8)
}

private fun getOrCreatePasswordFavoritesKey(): SecretKey {
    val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    val existingKey = keyStore.getKey(
        PASSWORD_KEY_ALIAS,
        null
    ) as? SecretKey

    if (existingKey != null) {
        return existingKey
    }

    val keyGenerator = KeyGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_AES,
        "AndroidKeyStore"
    )

    val keySpec = KeyGenParameterSpec.Builder(
        PASSWORD_KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setKeySize(256)
        .build()

    keyGenerator.init(keySpec)
    return keyGenerator.generateKey()
}
