package de.meson_labs.luna_coin.components.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import de.meson_labs.luna_coin.models.CurrencyType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinTransferDialog(
    sender: Child,
    recipients: List<Child>,
    onDismiss: () -> Unit,
    onSend: (
        recipientId: String,
        amount: Long,
        comment: String,
        currency: CurrencyType,
        onResult: (Boolean) -> Unit
    ) -> Unit
) {
    var selectedRecipient by remember(recipients) {
        mutableStateOf(recipients.firstOrNull())
    }
    var recipientMenuExpanded by remember {
        mutableStateOf(false)
    }
    var selectedCurrency by remember {
        mutableStateOf(CurrencyType.LUNA_COIN)
    }
    var amountText by remember {
        mutableStateOf("")
    }
    var comment by remember {
        mutableStateOf("")
    }
    var showConfirmation by remember {
        mutableStateOf(false)
    }
    var isSending by remember {
        mutableStateOf(false)
    }
    var errorText by remember {
        mutableStateOf<String?>(null)
    }

    val amount = amountText.toLongOrNull() ?: 0L
    val cleanedComment = comment.trim()

    val availableBalance = when (selectedCurrency) {
        CurrencyType.LUNA_COIN -> sender.coins.toLong()
        CurrencyType.LUNA_SILVER -> sender.silver
    }

    val currencyLabel = when (selectedCurrency) {
        CurrencyType.LUNA_COIN -> "Luna Coins"
        CurrencyType.LUNA_SILVER -> "Luna Silver"
    }

    val amountLabel = when (selectedCurrency) {
        CurrencyType.LUNA_COIN -> "Anzahl Luna Coins"
        CurrencyType.LUNA_SILVER -> "Anzahl Luna Silver"
    }

    val canContinue =
        selectedRecipient != null &&
                amount > 0L &&
                amount <= availableBalance &&
                cleanedComment.isNotBlank()

    AlertDialog(
        onDismissRequest = {
            if (!isSending) {
                onDismiss()
            }
        },
        title = {
            Text("Coins senden")
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Währung",
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCurrency == CurrencyType.LUNA_COIN,
                        onClick = {
                            selectedCurrency = CurrencyType.LUNA_COIN
                            amountText = ""
                            errorText = null
                        },
                        label = {
                            Text("Luna Coins")
                        }
                    )

                    FilterChip(
                        selected = selectedCurrency == CurrencyType.LUNA_SILVER,
                        onClick = {
                            selectedCurrency = CurrencyType.LUNA_SILVER
                            amountText = ""
                            errorText = null
                        },
                        label = {
                            Text("Luna Silver")
                        }
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(
                                id = when (selectedCurrency) {
                                    CurrencyType.LUNA_COIN -> R.drawable.luna_coin_small
                                    CurrencyType.LUNA_SILVER -> R.drawable.luna_silver
                                }
                            ),
                            contentDescription = when (selectedCurrency) {
                                CurrencyType.LUNA_COIN -> "Luna Coin"
                                CurrencyType.LUNA_SILVER -> "Luna Silver"
                            },
                            modifier = Modifier.size(
                                when (selectedCurrency) {
                                    CurrencyType.LUNA_COIN -> 64.dp
                                    CurrencyType.LUNA_SILVER -> 52.dp
                                }
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Dein Guthaben:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "$availableBalance $currencyLabel",
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                        label = {
                            Text("Empfänger")
                        },
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
                                text = {
                                    Text(recipient.name)
                                },
                                onClick = {
                                    selectedRecipient = recipient
                                    recipientMenuExpanded = false
                                    errorText = null
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
                            .take(12)
                        errorText = null
                    },
                    label = {
                        Text(amountLabel)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = comment,
                    onValueChange = { value ->
                        if (value.length <= 150) {
                            comment = value
                            errorText = null
                        }
                    },
                    label = {
                        Text("Kommentar (Pflichtfeld)")
                    },
                    supportingText = {
                        Text("${comment.length}/150 Zeichen")
                    },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                if (amount > 0L) {
                    Text(
                        text = "Danach verbleiben dir " +
                                "${availableBalance - amount} $currencyLabel."
                    )
                }

                errorText?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    errorText = when {
                        selectedRecipient == null -> {
                            "Bitte wähle einen Empfänger aus."
                        }

                        amount <= 0L -> {
                            "Bitte gib einen gültigen Betrag ein."
                        }

                        amount > availableBalance -> {
                            when (selectedCurrency) {
                                CurrencyType.LUNA_COIN ->
                                    "Du hast nicht genug Luna Coins."

                                CurrencyType.LUNA_SILVER ->
                                    "Du hast nicht genug Luna Silver."
                            }
                        }

                        cleanedComment.isBlank() -> {
                            "Bitte gib einen Kommentar ein."
                        }

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
                        text = "Du sendest $amount $currencyLabel an " +
                                "${selectedRecipient!!.name}."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Kommentar:")

                    Text(
                        text = "„$cleanedComment“",
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Diese Aktion kann nicht selbstständig " +
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
                            cleanedComment,
                            selectedCurrency
                        ) { success ->
                            isSending = false

                            if (success) {
                                onDismiss()
                            } else {
                                showConfirmation = false
                                errorText = "Die Übertragung ist fehlgeschlagen."
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
