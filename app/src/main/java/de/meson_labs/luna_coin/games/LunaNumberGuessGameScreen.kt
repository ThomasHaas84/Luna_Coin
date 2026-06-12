package de.meson_labs.luna_coin.games

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.components.LunaScreenHeader
import de.meson_labs.luna_coin.models.Child
import kotlin.random.Random

@Composable
fun LunaNumberGuessGameScreen(
    modifier: Modifier = Modifier,
    selectedChild: Child?,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    var targetNumber by remember { mutableIntStateOf(Random.nextInt(1, 21)) }
    var input by remember { mutableStateOf("") }
    var attempts by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("Ich denke an eine Zahl von 1 bis 20.") }
    var finished by remember { mutableStateOf(false) }

    fun resetGame() {
        targetNumber = Random.nextInt(1, 21)
        input = ""
        attempts = 0
        message = "Ich denke an eine Zahl von 1 bis 20."
        finished = false
    }

    fun checkGuess() {
        val guess = input.toIntOrNull()

        if (guess == null || guess !in 1..20) {
            message = "Bitte gib eine Zahl von 1 bis 20 ein."
            return
        }

        attempts++

        when {
            guess < targetNumber -> {
                message = "Zu klein. Versuch es nochmal!"
            }

            guess > targetNumber -> {
                message = "Zu groß. Versuch es nochmal!"
            }

            else -> {
                message = "Richtig! Du hast $attempts Versuche gebraucht. 🎉"
                finished = true
            }
        }

        input = ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        LunaScreenHeader(
            title = "Zahlenraten",
            selectedChild = selectedChild,
            onLogout = onLogout
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBack) {
            Text(text = "Zurück")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Versuche: $attempts",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { value ->
                input = value.filter { it.isDigit() }
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = "Deine Zahl")
            },
            enabled = !finished,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { checkGuess() },
            enabled = !finished
        ) {
            Text(text = "Prüfen")
        }

        if (finished) {
            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { resetGame() }) {
                Text(text = "Nochmal spielen")
            }
        }
    }
}