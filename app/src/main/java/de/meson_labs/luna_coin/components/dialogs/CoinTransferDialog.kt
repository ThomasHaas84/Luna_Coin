package de.meson_labs.luna_coin.components.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.meson_labs.luna_coin.R
import de.meson_labs.luna_coin.models.Child

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinTransferDialog(
    sender: Child,
    recipients: List<Child>,
    onDismiss: () -> Unit,
    onSend: (recipientId: String, amount: Int, comment: String, onResult: (Boolean) -> Unit) -> Unit
) {
    var selectedRecipient by remember(recipients) { mutableStateOf(recipients.firstOrNull()) }
    var recipientMenuExpanded by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val amount = amountText.toIntOrNull() ?: 0
    val cleanedComment = comment.trim()
    val canContinue =
        selectedRecipient != null &&
                amount > 0 &&
                amount <= sender.coins &&
                cleanedComment.isNotBlank()

    AlertDialog(
        onDismissRequest = { if (!isSending) onDismiss() },
        title = { Text("Luna Coins senden") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.luna_coin_small),
                        contentDescription = "Luna Coin",
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Dein Guthaben: ${sender.coins}",
                        fontWeight = FontWeight.Bold
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = recipientMenuExpanded,
                    onExpandedChange = {
                        recipientMenuExpanded = !recipientMenuExpanded
                    }
                ) {
                    OutlinedTextField(
                        value = selectedRecipient?.name.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Empfänger") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = recipientMenuExpanded
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = recipientMenuExpanded,
                        onDismissRequest = {
                            recipientMenuExpanded = false
                        }
                    ) {
                        recipients.forEach { recipient ->
                            DropdownMenuItem(
                                text = { Text(recipient.name) },
                                onClick = {
                                    selectedRecipient = recipient
                                    recipientMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { value ->
                        amountText = value
                            .filter(Char::isDigit)
                            .take(6)
                    },
                    label = { Text("Anzahl Luna Coins") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = comment,
                    onValueChange = {
                        if (it.length <= 150) {
                            comment = it
                        }
                    },
                    label = { Text("Kommentar (Pflichtfeld)") },
                    supportingText = {
                        Text("${comment.length}/150 Zeichen")
                    },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                if (amount > 0) {
                    Text(
                        "Danach verbleiben dir ${sender.coins - amount} Luna Coins."
                    )
                }

                errorText?.let { error ->
                    Text(error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    errorText = when {
                        selectedRecipient == null ->
                            "Bitte wähle einen Empfänger aus."

                        amount <= 0 ->
                            "Bitte gib einen gültigen Betrag ein."

                        amount > sender.coins ->
                            "Du hast nicht genug Luna Coins."

                        cleanedComment.isBlank() ->
                            "Bitte gib einen Kommentar ein."

                        else -> null
                    }

                    if (errorText == null) {
                        showConfirmation = true
                    }
                },
                enabled = canContinue && !isSending
            ) {
                Text("Weiter")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isSending
            ) {
                Text("Abbrechen")
            }
        }
    )

    if (showConfirmation && selectedRecipient != null) {
        AlertDialog(
            onDismissRequest = {
                if (!isSending) {
                    showConfirmation = false
                }
            },
            title = {
                Text("Coins wirklich senden?")
            },
            text = {
                Column {
                    Text(
                        "Du sendest $amount Luna Coin" +
                                "${if (amount == 1) "" else "s"} an " +
                                "${selectedRecipient!!.name}."
                    )

                    Spacer(Modifier.height(12.dp))

                    Text("Kommentar:")
                    Text(
                        text = "„$cleanedComment“",
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Diese Aktion kann nicht selbstständig " +
                                "rückgängig gemacht werden."
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSending = true

                        onSend(
                            selectedRecipient!!.id,
                            amount,
                            cleanedComment
                        ) { success ->
                            isSending = false

                            if (success) {
                                onDismiss()
                            } else {
                                showConfirmation = false
                            }
                        }
                    },
                    enabled = !isSending
                ) {
                    Text(
                        if (isSending) {
                            "Wird gesendet…"
                        } else {
                            "Jetzt senden"
                        }
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showConfirmation = false
                    },
                    enabled = !isSending
                ) {
                    Text("Zurück")
                }
            }
        )
    }
}
