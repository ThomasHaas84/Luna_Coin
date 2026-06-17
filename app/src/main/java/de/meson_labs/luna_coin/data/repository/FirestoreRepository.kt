package de.meson_labs.luna_coin.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DogScheduleItem
import de.meson_labs.luna_coin.models.GameHighscore
import de.meson_labs.luna_coin.models.LogEntry
import de.meson_labs.luna_coin.models.LuckyWheelUsage
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.LunaInventoryItem
import de.meson_labs.luna_coin.models.ShopItem
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.models.UserRole
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirestoreRepository : DataRepository {

    private val db = FirebaseFirestore.getInstance()

    private val familyId = "haas_family_demo"

    private val familyRef = db.collection("families").document(familyId)

    private val childrenRef = familyRef.collection("children")
    private val tasksRef = familyRef.collection("tasks")
    private val shopItemsRef = familyRef.collection("shopItems")
    private val dogScheduleRef = familyRef.collection("dogSchedule")
    private val logsRef = familyRef.collection("logs")
    private val luckyWheelUsageRef = familyRef.collection("luckyWheelUsage")
    private val gameHighscoresRef = familyRef.collection("gameHighscores")

    private val backupDocumentRef = familyRef.collection("backups").document("current")
    private val backupChildrenRef = backupDocumentRef.collection("children")
    private val backupTasksRef = backupDocumentRef.collection("tasks")
    private val backupShopItemsRef = backupDocumentRef.collection("shopItems")
    private val backupDogScheduleRef = backupDocumentRef.collection("dogSchedule")
    private val backupLogsRef = backupDocumentRef.collection("logs")
    private val backupLuckyWheelUsageRef = backupDocumentRef.collection("luckyWheelUsage")
    private val backupGameHighscoresRef = backupDocumentRef.collection("gameHighscores")

    private val listenerRegistrations = mutableListOf<ListenerRegistration>()

    private var realtimeChildren: List<Child>? = null
    private var realtimeTasks: List<TaskItem>? = null
    private var realtimeShopItems: List<ShopItem>? = null
    private var realtimeDogSchedule: List<DogScheduleItem>? = null
    private var realtimeLogs: List<LogEntry>? = null
    private var realtimeLuckyWheelUsage: List<LuckyWheelUsage>? = null
    private var realtimeGameHighscores: List<GameHighscore>? = null

    override suspend fun loadData(): LunaCoinData? {
        return try {
            val children = loadChildren()

            if (children.isEmpty()) {
                println("⚠️ Keine Kinder in Firestore gefunden")
                return null
            }

            LunaCoinData(
                children = children,
                tasks = loadTasks(),
                shopItems = loadShopItems(),
                dogSchedule = loadDogSchedule(),
                logs = loadLogs(),
                luckyWheelUsage = loadLuckyWheelUsage(),
                gameHighscores = loadGameHighscores()
            )
        } catch (e: Exception) {
            println("❌ Fehler beim Laden aus Firestore: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    override suspend fun saveData(data: LunaCoinData) {
        try {
            updateFamilyTimestamp()

            replaceCollection(childrenRef, data.children.map { prepareForSave(it) })
            replaceCollection(tasksRef, data.tasks.map { prepareForSave(it) })
            replaceCollection(shopItemsRef, data.shopItems.map { prepareForSave(it) })
            replaceCollection(dogScheduleRef, data.dogSchedule.map { prepareForSave(it) })
            replaceCollection(logsRef, data.logs.map { prepareForSave(it) })
            replaceCollection(luckyWheelUsageRef, data.luckyWheelUsage.map { prepareForSave(it) })
            replaceCollection(gameHighscoresRef, data.gameHighscores.map { prepareForSave(it) })

            println("✅ Firestore Collections komplett gespeichert (Coins: ${data.children.sumOf { it.coins }})")
        } catch (e: Exception) {
            println("❌ Fehler beim Speichern in Firestore: ${e.message}")
            e.printStackTrace()
        }
    }

    override suspend fun createCloudBackup(data: LunaCoinData) {
        try {
            backupDocumentRef.set(
                mapOf(
                    "familyId" to familyId,
                    "createdAt" to Timestamp.now(),
                    "updatedAt" to Timestamp.now(),
                    "childrenCount" to data.children.size,
                    "tasksCount" to data.tasks.size,
                    "shopItemsCount" to data.shopItems.size,
                    "dogScheduleCount" to data.dogSchedule.size,
                    "logsCount" to data.logs.size,
                    "luckyWheelUsageCount" to data.luckyWheelUsage.size,
                    "gameHighscoresCount" to data.gameHighscores.size
                ),
                SetOptions.merge()
            ).await()

            replaceCollection(backupChildrenRef, data.children.map { prepareForSave(it) })
            replaceCollection(backupTasksRef, data.tasks.map { prepareForSave(it) })
            replaceCollection(backupShopItemsRef, data.shopItems.map { prepareForSave(it) })
            replaceCollection(backupDogScheduleRef, data.dogSchedule.map { prepareForSave(it) })
            replaceCollection(backupLogsRef, data.logs.map { prepareForSave(it) })
            replaceCollection(backupLuckyWheelUsageRef, data.luckyWheelUsage.map { prepareForSave(it) })
            replaceCollection(backupGameHighscoresRef, data.gameHighscores.map { prepareForSave(it) })

            println("✅ Cloud-Backup erfolgreich erstellt")
        } catch (e: Exception) {
            println("❌ Fehler beim Erstellen des Cloud-Backups: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun loadCloudBackup(): LunaCoinData? {
        return try {
            val backupSnapshot = backupDocumentRef.get().await()

            if (!backupSnapshot.exists()) {
                println("⚠️ Kein Cloud-Backup vorhanden")
                return null
            }

            val children = backupChildrenRef
                .orderBy("age", Query.Direction.ASCENDING)
                .get()
                .await()
                .toObjects(Child::class.java)

            if (children.isEmpty()) {
                println("⚠️ Cloud-Backup enthält keine Kinder")
                return null
            }

            val backupData = LunaCoinData(
                children = children,
                tasks = backupTasksRef.get().await().toObjects(TaskItem::class.java),
                shopItems = backupShopItemsRef.get().await().toObjects(ShopItem::class.java),
                dogSchedule = backupDogScheduleRef.get().await().toObjects(DogScheduleItem::class.java),
                logs = backupLogsRef
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(MAX_ACTIVE_LOGS)
                    .get()
                    .await()
                    .toObjects(LogEntry::class.java),
                luckyWheelUsage = backupLuckyWheelUsageRef.get().await().toObjects(LuckyWheelUsage::class.java),
                gameHighscores = backupGameHighscoresRef.get().await().toObjects(GameHighscore::class.java)
            )

            println("✅ Cloud-Backup erfolgreich geladen")
            backupData
        } catch (e: Exception) {
            println("❌ Fehler beim Laden des Cloud-Backups: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    override fun startRealtimeSync(
        onDataChanged: (LunaCoinData) -> Unit,
        onError: (Exception) -> Unit
    ) {
        stopRealtimeSync()

        realtimeChildren = null
        realtimeTasks = null
        realtimeShopItems = null
        realtimeDogSchedule = null
        realtimeLogs = null
        realtimeLuckyWheelUsage = null
        realtimeGameHighscores = null

        listenerRegistrations += childrenRef
            .orderBy("age", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("❌ Realtime children Fehler: ${error.message}")
                    onError(error)
                    return@addSnapshotListener
                }

                realtimeChildren = snapshot?.toObjects(Child::class.java).orEmpty()
                emitRealtimeDataIfReady(onDataChanged)
            }

        listenerRegistrations += tasksRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("❌ Realtime tasks Fehler: ${error.message}")
                onError(error)
                return@addSnapshotListener
            }

            realtimeTasks = snapshot?.toObjects(TaskItem::class.java).orEmpty()
            emitRealtimeDataIfReady(onDataChanged)
        }

        listenerRegistrations += shopItemsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("❌ Realtime shopItems Fehler: ${error.message}")
                onError(error)
                return@addSnapshotListener
            }

            realtimeShopItems = snapshot?.toObjects(ShopItem::class.java).orEmpty()
            emitRealtimeDataIfReady(onDataChanged)
        }

        listenerRegistrations += dogScheduleRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("❌ Realtime dogSchedule Fehler: ${error.message}")
                onError(error)
                return@addSnapshotListener
            }

            realtimeDogSchedule = snapshot?.toObjects(DogScheduleItem::class.java).orEmpty()
            emitRealtimeDataIfReady(onDataChanged)
        }

        listenerRegistrations += logsRef
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(MAX_ACTIVE_LOGS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("❌ Realtime logs Fehler: ${error.message}")
                    onError(error)
                    return@addSnapshotListener
                }

                realtimeLogs = snapshot?.toObjects(LogEntry::class.java).orEmpty()
                emitRealtimeDataIfReady(onDataChanged)
            }

        listenerRegistrations += luckyWheelUsageRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("❌ Realtime luckyWheelUsage Fehler: ${error.message}")
                onError(error)
                return@addSnapshotListener
            }

            realtimeLuckyWheelUsage = snapshot?.toObjects(LuckyWheelUsage::class.java).orEmpty()
            emitRealtimeDataIfReady(onDataChanged)
        }

        listenerRegistrations += gameHighscoresRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("❌ Realtime gameHighscores Fehler: ${error.message}")
                onError(error)
                return@addSnapshotListener
            }

            realtimeGameHighscores = snapshot?.toObjects(GameHighscore::class.java).orEmpty()
            emitRealtimeDataIfReady(onDataChanged)
        }

        println("✅ Firestore Realtime-Synchronisation gestartet")
    }

    override fun stopRealtimeSync() {
        listenerRegistrations.forEach { registration ->
            registration.remove()
        }

        listenerRegistrations.clear()

        println("🛑 Firestore Realtime-Synchronisation gestoppt")
    }

    private fun emitRealtimeDataIfReady(
        onDataChanged: (LunaCoinData) -> Unit
    ) {
        val children = realtimeChildren ?: return
        val tasks = realtimeTasks ?: return
        val shopItems = realtimeShopItems ?: return
        val dogSchedule = realtimeDogSchedule ?: return
        val logs = realtimeLogs ?: return
        val luckyWheelUsage = realtimeLuckyWheelUsage ?: return
        val gameHighscores = realtimeGameHighscores ?: return

        val realtimeData = LunaCoinData(
            children = children,
            tasks = tasks,
            shopItems = shopItems,
            dogSchedule = dogSchedule,
            logs = logs,
            luckyWheelUsage = luckyWheelUsage,
            gameHighscores = gameHighscores
        )

        onDataChanged(realtimeData)
    }

    override suspend fun loadChildren(): List<Child> {
        return childrenRef
            .orderBy("age", Query.Direction.ASCENDING)
            .get()
            .await()
            .toObjects(Child::class.java)
    }

    override suspend fun loadTasks(): List<TaskItem> {
        return tasksRef.get().await().toObjects(TaskItem::class.java)
    }

    override suspend fun loadShopItems(): List<ShopItem> {
        return shopItemsRef.get().await().toObjects(ShopItem::class.java)
    }

    override suspend fun loadDogSchedule(): List<DogScheduleItem> {
        return dogScheduleRef.get().await().toObjects(DogScheduleItem::class.java)
    }

    override suspend fun loadLogs(limit: Long): List<LogEntry> {
        return logsRef
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
            .toObjects(LogEntry::class.java)
    }

    override suspend fun loadLuckyWheelUsage(): List<LuckyWheelUsage> {
        return luckyWheelUsageRef.get().await().toObjects(LuckyWheelUsage::class.java)
    }

    override suspend fun loadGameHighscores(): List<GameHighscore> {
        return gameHighscoresRef.get().await().toObjects(GameHighscore::class.java)
    }

    override suspend fun saveChild(child: Child) {
        val item = prepareForSave(child)
        childrenRef.document(item.id).set(item).await()
        updateFamilyTimestamp()
    }

    override suspend fun saveTask(task: TaskItem) {
        val item = prepareForSave(task)
        tasksRef.document(item.id).set(item).await()
        updateFamilyTimestamp()
    }

    override suspend fun saveShopItem(shopItem: ShopItem) {
        val item = prepareForSave(shopItem)
        shopItemsRef.document(item.id).set(item).await()
        updateFamilyTimestamp()
    }

    override suspend fun saveDogScheduleItem(item: DogScheduleItem) {
        val prepared = prepareForSave(item)
        dogScheduleRef.document(prepared.id).set(prepared).await()
        updateFamilyTimestamp()
    }

    override suspend fun saveLog(log: LogEntry) {
        val item = prepareForSave(log)
        logsRef.document(item.id).set(item).await()
        updateFamilyTimestamp()
    }

    override suspend fun saveLuckyWheelUsage(usage: LuckyWheelUsage) {
        val item = prepareForSave(usage)
        luckyWheelUsageRef.document(item.id).set(item).await()
        updateFamilyTimestamp()
    }

    override suspend fun saveGameHighscore(highscore: GameHighscore) {
        val item = prepareForSave(highscore)
        gameHighscoresRef.document(item.id).set(item).await()
        updateFamilyTimestamp()
    }

    override suspend fun updateChildInventory(
        childId: String,
        inventory: List<LunaInventoryItem>,
        equippedItem: LunaInventoryItem?,
        profileImageItem: LunaInventoryItem?,
        hasProfileImage: Boolean
    ) {
        childrenRef.document(childId)
            .set(
                mapOf(
                    "inventory" to inventory.map { it.name },
                    "equippedItem" to equippedItem?.name,
                    "profileImageItem" to profileImageItem?.name,
                    "hasProfileImage" to hasProfileImage,
                    "updatedAt" to Timestamp.now()
                ),
                SetOptions.merge()
            )
            .await()

        updateFamilyTimestamp()
    }

    override suspend fun updateChildProfile(
        childId: String,
        name: String,
        role: UserRole,
        password: String,
        age: Int
    ) {
        childrenRef.document(childId)
            .set(
                mapOf(
                    "name" to name,
                    "role" to role.name,
                    "password" to password,
                    "age" to age,
                    "updatedAt" to Timestamp.now()
                ),
                SetOptions.merge()
            )
            .await()

        updateFamilyTimestamp()
    }

    override suspend fun deleteChild(childId: String) {
        childrenRef.document(childId).delete().await()
        updateFamilyTimestamp()
    }

    override suspend fun deleteTask(taskId: String) {
        tasksRef.document(taskId).delete().await()
        updateFamilyTimestamp()
    }

    override suspend fun deleteShopItem(shopItemId: String) {
        shopItemsRef.document(shopItemId).delete().await()
        updateFamilyTimestamp()
    }

    override suspend fun deleteDogScheduleItem(itemId: String) {
        dogScheduleRef.document(itemId).delete().await()
        updateFamilyTimestamp()
    }

    override suspend fun deleteLog(logId: String) {
        logsRef.document(logId).delete().await()
        updateFamilyTimestamp()
    }

    override suspend fun deleteLuckyWheelUsage(usageId: String) {
        luckyWheelUsageRef.document(usageId).delete().await()
        updateFamilyTimestamp()
    }

    override suspend fun deleteGameHighscore(highscoreId: String) {
        gameHighscoresRef.document(highscoreId).delete().await()
        updateFamilyTimestamp()
    }

    override suspend fun changeChildCoins(
        childId: String,
        coinDelta: Int
    ): Int {
        val documentRef = childrenRef.document(childId)

        val newCoinValue = db.runTransaction { transaction ->
            val snapshot = transaction.get(documentRef)

            val currentCoins = snapshot.getLong("coins")?.toInt() ?: 0
            val updatedCoins = currentCoins + coinDelta

            println(
                "DEBUG COINS: " +
                        "childId=$childId, " +
                        "currentCoins=$currentCoins, " +
                        "coinDelta=$coinDelta, " +
                        "updatedCoins=$updatedCoins"
            )

            if (updatedCoins < 0) {
                throw IllegalStateException("Nicht genug Luna Coins")
            }

            transaction.update(
                documentRef,
                mapOf(
                    "coins" to updatedCoins,
                    "updatedAt" to Timestamp.now()
                )
            )

            updatedCoins
        }.await()

        println(
            "DEBUG COINS: " +
                    "childId=$childId erfolgreich gespeichert. " +
                    "Neuer Wert=$newCoinValue"
        )

        updateFamilyTimestamp()

        return newCoinValue
    }

    override suspend fun setChildCoins(
        childId: String,
        coins: Int
    ): Int {
        val documentRef = childrenRef.document(childId)

        val newCoinValue = db.runTransaction { transaction ->

            println(
                "DEBUG SET COINS: " +
                        "childId=$childId, " +
                        "newCoins=$coins"
            )

            transaction.update(
                documentRef,
                mapOf(
                    "coins" to coins,
                    "updatedAt" to Timestamp.now()
                )
            )

            coins
        }.await()

        println(
            "DEBUG SET COINS: " +
                    "childId=$childId erfolgreich gespeichert. " +
                    "Neuer Wert=$newCoinValue"
        )

        updateFamilyTimestamp()

        return newCoinValue
    }

    private suspend fun updateFamilyTimestamp() {
        familyRef.set(
            mapOf(
                "familyId" to familyId,
                "updatedAt" to Timestamp.now()
            ),
            SetOptions.merge()
        ).await()
    }

    private suspend fun <T : Any> replaceCollection(
        collectionRef: CollectionReference,
        items: List<T>
    ) {
        val existingDocuments = collectionRef.get().await().documents

        if (existingDocuments.isNotEmpty()) {
            val deleteBatch = db.batch()
            existingDocuments.forEach { document ->
                deleteBatch.delete(document.reference)
            }
            deleteBatch.commit().await()
        }

        if (items.isEmpty()) return

        val writeBatch = db.batch()

        items.forEach { item ->
            val documentId = getDocumentId(item)
            writeBatch.set(collectionRef.document(documentId), item)
        }

        writeBatch.commit().await()
    }

    private fun prepareForSave(child: Child): Child {
        val now = Date()
        return child.copy(
            id = child.id.ifBlank { childrenRef.document().id },
            familyId = familyId,
            createdAt = child.createdAt ?: now,
            updatedAt = now
        )
    }

    private fun prepareForSave(task: TaskItem): TaskItem {
        val now = Date()
        return task.copy(
            id = task.id.ifBlank { tasksRef.document().id },
            familyId = familyId,
            createdAt = task.createdAt ?: now,
            updatedAt = now
        )
    }

    private fun prepareForSave(shopItem: ShopItem): ShopItem {
        val now = Date()
        return shopItem.copy(
            id = shopItem.id.ifBlank { shopItemsRef.document().id },
            familyId = familyId,
            createdAt = shopItem.createdAt ?: now,
            updatedAt = now
        )
    }

    private fun prepareForSave(item: DogScheduleItem): DogScheduleItem {
        val now = Date()
        return item.copy(
            id = item.id.ifBlank { dogScheduleRef.document().id },
            familyId = familyId,
            createdAt = item.createdAt ?: now,
            updatedAt = now
        )
    }

    private fun prepareForSave(log: LogEntry): LogEntry {
        val now = Date()
        return log.copy(
            id = log.id.ifBlank { logsRef.document().id },
            familyId = familyId,
            createdAt = log.createdAt ?: now,
            updatedAt = now
        )
    }

    private fun prepareForSave(usage: LuckyWheelUsage): LuckyWheelUsage {
        val now = Date()
        val usageId = usage.id.ifBlank {
            "${usage.childId}_${usage.date}"
        }

        return usage.copy(
            id = usageId,
            familyId = familyId,
            createdAt = usage.createdAt ?: now,
            updatedAt = now
        )
    }

    private fun prepareForSave(highscore: GameHighscore): GameHighscore {
        val now = Date()
        val highscoreId = highscore.id.ifBlank {
            "${highscore.game}_${highscore.childId}_${highscore.level}_${highscore.scoreType}"
        }

        return highscore.copy(
            id = highscoreId,
            familyId = familyId,
            createdAt = highscore.createdAt ?: now,
            updatedAt = now
        )
    }

    private fun getDocumentId(item: Any): String {
        return when (item) {
            is Child -> item.id
            is TaskItem -> item.id
            is ShopItem -> item.id
            is DogScheduleItem -> item.id
            is LogEntry -> item.id
            is LuckyWheelUsage -> item.id.ifBlank { "${item.childId}_${item.date}" }
            is GameHighscore -> item.id.ifBlank { "${item.game}_${item.childId}_${item.level}_${item.scoreType}" }
            else -> throw IllegalArgumentException("Unbekannter Firestore-Typ: ${item::class.java.simpleName}")
        }
    }

    companion object {
        private const val MAX_ACTIVE_LOGS = 2000L
    }
}