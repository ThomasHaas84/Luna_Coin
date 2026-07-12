package de.meson_labs.luna_coin.manager

import de.meson_labs.luna_coin.data.repository.DataRepository
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.LogEntry
import de.meson_labs.luna_coin.models.LogType
import java.time.LocalDateTime
import java.util.UUID

data class CoinTransferResult(
    val newSenderCoins: Int,
    val newRecipientCoins: Int,
    val senderLog: LogEntry,
    val recipientLog: LogEntry
)

class CoinTransferManager(
    private val repository: DataRepository
) {
    suspend fun transfer(
        sender: Child,
        recipient: Child,
        amount: Int,
        comment: String
    ): CoinTransferResult {
        val cleanedComment = comment.trim()
        require(sender.id != recipient.id) { "Du kannst dir nicht selbst Coins senden." }
        require(amount > 0) { "Bitte gib einen gültigen Betrag ein." }
        require(cleanedComment.isNotBlank()) { "Bitte gib einen Kommentar ein." }
        require(cleanedComment.length <= 150) { "Der Kommentar darf höchstens 150 Zeichen lang sein." }
        require(sender.coins >= amount) { "Du hast nicht genug Luna Coins." }

        val transferId = UUID.randomUUID().toString()
        val timestamp = LocalDateTime.now().toString()
        val senderLog = LogEntry(
            id = "${transferId}_sender",
            familyId = sender.familyId,
            timestamp = timestamp,
            childId = sender.id,
            type = LogType.SYSTEM,
            text = "Du hast ${recipient.name} $amount Luna Coin${if (amount == 1) "" else "s"} gesendet. Kommentar: „$cleanedComment“",
            coinChange = -amount
        )
        val recipientLog = LogEntry(
            id = "${transferId}_recipient",
            familyId = recipient.familyId.ifBlank { sender.familyId },
            timestamp = timestamp,
            childId = recipient.id,
            type = LogType.SYSTEM,
            text = "${sender.name} hat dir $amount Luna Coin${if (amount == 1) "" else "s"} gesendet. Kommentar: „$cleanedComment“",
            coinChange = amount
        )

        val (newSenderCoins, newRecipientCoins) = repository.transferCoins(
            senderId = sender.id,
            recipientId = recipient.id,
            amount = amount,
            senderLog = senderLog,
            recipientLog = recipientLog
        )

        return CoinTransferResult(
            newSenderCoins = newSenderCoins,
            newRecipientCoins = newRecipientCoins,
            senderLog = senderLog,
            recipientLog = recipientLog
        )
    }
}
