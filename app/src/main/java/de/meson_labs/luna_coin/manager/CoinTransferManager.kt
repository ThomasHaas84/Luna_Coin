package de.meson_labs.luna_coin.manager

import de.meson_labs.luna_coin.data.repository.DataRepository
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.CurrencyType
import de.meson_labs.luna_coin.models.LogEntry
import de.meson_labs.luna_coin.models.LogType
import java.time.LocalDateTime
import java.util.UUID

data class CoinTransferResult(
    val currency: CurrencyType,
    val newSenderCoins: Int,
    val newRecipientCoins: Int,
    val newSenderSilver: Long,
    val newRecipientSilver: Long,
    val senderLog: LogEntry,
    val recipientLog: LogEntry
) {
    val newSenderBalance: Long
        get() = when (currency) {
            CurrencyType.LUNA_COIN -> newSenderCoins.toLong()
            CurrencyType.LUNA_SILVER -> newSenderSilver
        }

    val newRecipientBalance: Long
        get() = when (currency) {
            CurrencyType.LUNA_COIN -> newRecipientCoins.toLong()
            CurrencyType.LUNA_SILVER -> newRecipientSilver
        }
}

class CoinTransferManager(
    private val repository: DataRepository
) {

    suspend fun transfer(
        sender: Child,
        recipient: Child,
        amount: Int,
        comment: String
    ): CoinTransferResult {
        return transfer(
            sender = sender,
            recipient = recipient,
            amount = amount.toLong(),
            comment = comment,
            currency = CurrencyType.LUNA_COIN
        )
    }

    suspend fun transfer(
        sender: Child,
        recipient: Child,
        amount: Long,
        comment: String,
        currency: CurrencyType
    ): CoinTransferResult {
        val cleanedComment = comment.trim()

        require(sender.id != recipient.id) {
            "Du kannst dir nicht selbst Coins senden."
        }

        require(amount > 0L) {
            "Bitte gib einen gültigen Betrag ein."
        }

        require(cleanedComment.isNotBlank()) {
            "Bitte gib einen Kommentar ein."
        }

        require(cleanedComment.length <= 150) {
            "Der Kommentar darf höchstens 150 Zeichen lang sein."
        }

        when (currency) {
            CurrencyType.LUNA_COIN -> {
                require(amount <= Int.MAX_VALUE.toLong()) {
                    "Der Betrag ist zu groß."
                }

                require(sender.coins.toLong() >= amount) {
                    "Du hast nicht genug Luna Coins."
                }
            }

            CurrencyType.LUNA_SILVER -> {
                require(sender.silver >= amount) {
                    "Du hast nicht genug Luna Silver."
                }
            }
        }

        val transferId = UUID.randomUUID().toString()
        val timestamp = LocalDateTime.now().toString()

        val currencyText = when (currency) {
            CurrencyType.LUNA_COIN -> {
                "Luna Coin${if (amount == 1L) "" else "s"}"
            }

            CurrencyType.LUNA_SILVER -> {
                "Luna Silver"
            }
        }

        val senderLog = LogEntry(
            id = "${transferId}_sender",
            familyId = sender.familyId,
            timestamp = timestamp,
            childId = sender.id,
            type = LogType.SYSTEM,
            text = "Du hast ${recipient.name} $amount $currencyText gesendet. " +
                    "Kommentar: „$cleanedComment“",
            coinChange = when (currency) {
                CurrencyType.LUNA_COIN -> -amount.toInt()
                CurrencyType.LUNA_SILVER -> 0
            },
            silverChange = when (currency) {
                CurrencyType.LUNA_COIN -> 0L
                CurrencyType.LUNA_SILVER -> -amount
            }
        )

        val recipientLog = LogEntry(
            id = "${transferId}_recipient",
            familyId = recipient.familyId.ifBlank {
                sender.familyId
            },
            timestamp = timestamp,
            childId = recipient.id,
            type = LogType.SYSTEM,
            text = "${sender.name} hat dir $amount $currencyText gesendet. " +
                    "Kommentar: „$cleanedComment“",
            coinChange = when (currency) {
                CurrencyType.LUNA_COIN -> amount.toInt()
                CurrencyType.LUNA_SILVER -> 0
            },
            silverChange = when (currency) {
                CurrencyType.LUNA_COIN -> 0L
                CurrencyType.LUNA_SILVER -> amount
            }
        )

        val (newSenderBalance, newRecipientBalance) = repository.transferCurrency(
            senderId = sender.id,
            recipientId = recipient.id,
            amount = amount,
            currency = currency,
            senderLog = senderLog,
            recipientLog = recipientLog
        )

        val newSenderCoins = when (currency) {
            CurrencyType.LUNA_COIN -> newSenderBalance.toInt()
            CurrencyType.LUNA_SILVER -> sender.coins
        }

        val newRecipientCoins = when (currency) {
            CurrencyType.LUNA_COIN -> newRecipientBalance.toInt()
            CurrencyType.LUNA_SILVER -> recipient.coins
        }

        val newSenderSilver = when (currency) {
            CurrencyType.LUNA_COIN -> sender.silver
            CurrencyType.LUNA_SILVER -> newSenderBalance
        }

        val newRecipientSilver = when (currency) {
            CurrencyType.LUNA_COIN -> recipient.silver
            CurrencyType.LUNA_SILVER -> newRecipientBalance
        }

        return CoinTransferResult(
            currency = currency,
            newSenderCoins = newSenderCoins,
            newRecipientCoins = newRecipientCoins,
            newSenderSilver = newSenderSilver,
            newRecipientSilver = newRecipientSilver,
            senderLog = senderLog,
            recipientLog = recipientLog
        )
    }
}
