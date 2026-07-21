package de.meson_labs.luna_coin.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import de.meson_labs.luna_coin.models.Child
import de.meson_labs.luna_coin.models.DogPlanData
import de.meson_labs.luna_coin.models.DogPlanShift
import de.meson_labs.luna_coin.models.DogPlanTaskCompletion
import de.meson_labs.luna_coin.models.DogPlanTaskTemplate
import de.meson_labs.luna_coin.models.DogScheduleItem
import de.meson_labs.luna_coin.models.GameDailyReward
import de.meson_labs.luna_coin.models.GameHighscore
import de.meson_labs.luna_coin.models.GameSettings
import de.meson_labs.luna_coin.models.LogEntry
import de.meson_labs.luna_coin.models.LuckyWheelUsage
import de.meson_labs.luna_coin.models.LunaCoinData
import de.meson_labs.luna_coin.models.LunaGameType
import de.meson_labs.luna_coin.models.LunaInventoryItem
import de.meson_labs.luna_coin.models.ShopItem
import de.meson_labs.luna_coin.models.TaskItem
import de.meson_labs.luna_coin.models.UserRole
import de.meson_labs.luna_coin.manager.ProgressManager
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
    private val dogPlanTemplatesRef = familyRef.collection("dogPlanTemplates")
    private val dogPlanCompletionsRef = familyRef.collection("dogPlanCompletions")
    private val dogPlanShiftsRef = familyRef.collection("dogPlanShifts")
    private val logsRef = familyRef.collection("logs")
    private val luckyWheelUsageRef = familyRef.collection("luckyWheelUsage")
    private val gameHighscoresRef = familyRef.collection("gameHighscores")
    private val gameDailyRewardsRef = familyRef.collection("gameDailyRewards")
    private val gameSettingsRef = familyRef.collection("settings").document("games")

    private val backupDocumentRef = familyRef.collection("backups").document("current")
    private val backupChildrenRef = backupDocumentRef.collection("children")
    private val backupTasksRef = backupDocumentRef.collection("tasks")
    private val backupShopItemsRef = backupDocumentRef.collection("shopItems")
    private val backupDogScheduleRef = backupDocumentRef.collection("dogSchedule")
    private val backupDogPlanTemplatesRef = backupDocumentRef.collection("dogPlanTemplates")
    private val backupDogPlanCompletionsRef = backupDocumentRef.collection("dogPlanCompletions")
    private val backupDogPlanShiftsRef = backupDocumentRef.collection("dogPlanShifts")
    private val backupLogsRef = backupDocumentRef.collection("logs")
    private val backupLuckyWheelUsageRef = backupDocumentRef.collection("luckyWheelUsage")
    private val backupGameHighscoresRef = backupDocumentRef.collection("gameHighscores")
    private val backupGameDailyRewardsRef = backupDocumentRef.collection("gameDailyRewards")
    private val backupGameSettingsRef = backupDocumentRef.collection("settings").document("games")

    private val listenerRegistrations = mutableListOf<ListenerRegistration>()

    private var realtimeChildren: List<Child>? = null
    private var realtimeTasks: List<TaskItem>? = null
    private var realtimeShopItems: List<ShopItem>? = null
    private var realtimeDogSchedule: List<DogScheduleItem>? = null
    private var realtimeDogPlanTemplates: List<DogPlanTaskTemplate>? = null
    private var realtimeDogPlanCompletions: List<DogPlanTaskCompletion>? = null
    private var realtimeDogPlanShifts: List<DogPlanShift>? = null
    private var realtimeLogs: List<LogEntry>? = null
    private var realtimeLuckyWheelUsage: List<LuckyWheelUsage>? = null
    private var realtimeGameHighscores: List<GameHighscore>? = null
    private var realtimeGameDailyRewards: List<GameDailyReward>? = null
    private var realtimeGameSettings: GameSettings? = null

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
                dogPlan = loadDogPlan(),
                logs = loadLogs(),
                luckyWheelUsage = loadLuckyWheelUsage(),
                gameHighscores = loadGameHighscores(),
                gameDailyRewards = loadGameDailyRewards(),
                gameSettings = loadGameSettings()
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
            replaceCollection(dogPlanTemplatesRef, data.dogPlan.templates.map { prepareForSave(it) })
            replaceCollection(dogPlanCompletionsRef, data.dogPlan.completions.map { prepareForSave(it) })
            replaceCollection(dogPlanShiftsRef, data.dogPlan.shifts.map { prepareForSave(it) })
            replaceCollection(logsRef, data.logs.map { prepareForSave(it) })
            replaceCollection(luckyWheelUsageRef, data.luckyWheelUsage.map { prepareForSave(it) })
            replaceCollection(gameHighscoresRef, data.gameHighscores.map { prepareForSave(it) })
            replaceCollection(gameDailyRewardsRef, data.gameDailyRewards.map { prepareForSave(it) })
            saveGameSettings(data.gameSettings)

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
                    "dogPlanTemplateCount" to data.dogPlan.templates.size,
                    "dogPlanCompletionCount" to data.dogPlan.completions.size,
                    "dogPlanShiftCount" to data.dogPlan.shifts.size,
                    "logsCount" to data.logs.size,
                    "luckyWheelUsageCount" to data.luckyWheelUsage.size,
                    "gameHighscoresCount" to data.gameHighscores.size,
                    "gameDailyRewardsCount" to data.gameDailyRewards.size,
                    "autoWeeklyHighscoreResetEnabled" to data.gameSettings.autoWeeklyHighscoreResetEnabled,
                    "lastAutomaticHighscoreResetDate" to data.gameSettings.lastAutomaticHighscoreResetDate
                ),
                SetOptions.merge()
            ).await()

            replaceCollection(backupChildrenRef, data.children.map { prepareForSave(it) })
            replaceCollection(backupTasksRef, data.tasks.map { prepareForSave(it) })
            replaceCollection(backupShopItemsRef, data.shopItems.map { prepareForSave(it) })
            replaceCollection(backupDogScheduleRef, data.dogSchedule.map { prepareForSave(it) })
            replaceCollection(backupDogPlanTemplatesRef, data.dogPlan.templates.map { prepareForSave(it) })
            replaceCollection(backupDogPlanCompletionsRef, data.dogPlan.completions.map { prepareForSave(it) })
            replaceCollection(backupDogPlanShiftsRef, data.dogPlan.shifts.map { prepareForSave(it) })
            replaceCollection(backupLogsRef, data.logs.map { prepareForSave(it) })
            replaceCollection(backupLuckyWheelUsageRef, data.luckyWheelUsage.map { prepareForSave(it) })
            replaceCollection(backupGameHighscoresRef, data.gameHighscores.map { prepareForSave(it) })
            replaceCollection(backupGameDailyRewardsRef, data.gameDailyRewards.map { prepareForSave(it) })
            backupGameSettingsRef.set(data.gameSettings).await()

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
                dogPlan = DogPlanData(
                    templates = backupDogPlanTemplatesRef
                        .orderBy("sortOrder", Query.Direction.ASCENDING)
                        .get()
                        .await()
                        .toObjects(DogPlanTaskTemplate::class.java),
                    completions = backupDogPlanCompletionsRef.get().await().toObjects(DogPlanTaskCompletion::class.java),
                    shifts = backupDogPlanShiftsRef.get().await().toObjects(DogPlanShift::class.java)
                ),
                logs = backupLogsRef
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(MAX_ACTIVE_LOGS)
                    .get()
                    .await()
                    .toObjects(LogEntry::class.java),
                luckyWheelUsage = backupLuckyWheelUsageRef.get().await().toObjects(LuckyWheelUsage::class.java),
                gameHighscores = backupGameHighscoresRef.get().await().toObjects(GameHighscore::class.java),
                gameDailyRewards = backupGameDailyRewardsRef.get().await().toObjects(GameDailyReward::class.java),
                gameSettings = backupGameSettingsRef.get().await().toObject(GameSettings::class.java)
                    ?: GameSettings()
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
        realtimeDogPlanTemplates = null
        realtimeDogPlanCompletions = null
        realtimeDogPlanShifts = null
        realtimeLogs = null
        realtimeLuckyWheelUsage = null
        realtimeGameHighscores = null
        realtimeGameDailyRewards = null
        realtimeGameSettings = null

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

        listenerRegistrations += dogPlanTemplatesRef
            .orderBy("sortOrder", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("❌ Realtime dogPlanTemplates Fehler: ${error.message}")
                    onError(error)
                    return@addSnapshotListener
                }

                realtimeDogPlanTemplates = snapshot?.toObjects(DogPlanTaskTemplate::class.java).orEmpty()
                emitRealtimeDataIfReady(onDataChanged)
            }

        listenerRegistrations += dogPlanCompletionsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("❌ Realtime dogPlanCompletions Fehler: ${error.message}")
                onError(error)
                return@addSnapshotListener
            }

            realtimeDogPlanCompletions = snapshot?.toObjects(DogPlanTaskCompletion::class.java).orEmpty()
            emitRealtimeDataIfReady(onDataChanged)
        }

        listenerRegistrations += dogPlanShiftsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("❌ Realtime dogPlanShifts Fehler: ${error.message}")
                onError(error)
                return@addSnapshotListener
            }

            realtimeDogPlanShifts = snapshot?.toObjects(DogPlanShift::class.java).orEmpty()
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

        listenerRegistrations += gameDailyRewardsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("❌ Realtime gameDailyRewards Fehler: ${error.message}")
                onError(error)
                return@addSnapshotListener
            }

            realtimeGameDailyRewards = snapshot?.toObjects(GameDailyReward::class.java).orEmpty()
            emitRealtimeDataIfReady(onDataChanged)
        }

        listenerRegistrations += gameSettingsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("❌ Realtime gameSettings Fehler: ${error.message}")
                onError(error)
                return@addSnapshotListener
            }

            realtimeGameSettings = snapshot?.toObject(GameSettings::class.java) ?: GameSettings()
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
        val dogPlanTemplates = realtimeDogPlanTemplates ?: return
        val dogPlanCompletions = realtimeDogPlanCompletions ?: return
        val dogPlanShifts = realtimeDogPlanShifts ?: return
        val logs = realtimeLogs ?: return
        val luckyWheelUsage = realtimeLuckyWheelUsage ?: return
        val gameHighscores = realtimeGameHighscores ?: return
        val gameDailyRewards = realtimeGameDailyRewards ?: return
        val gameSettings = realtimeGameSettings ?: return

        val realtimeData = LunaCoinData(
            children = ensureBuiltInAdmin(children),
            tasks = tasks,
            shopItems = shopItems,
            dogSchedule = dogSchedule,
            dogPlan = DogPlanData(
                templates = dogPlanTemplates,
                completions = dogPlanCompletions,
                shifts = dogPlanShifts
            ),
            logs = logs,
            luckyWheelUsage = luckyWheelUsage,
            gameHighscores = gameHighscores,
            gameDailyRewards = gameDailyRewards,
            gameSettings = gameSettings
        )

        onDataChanged(realtimeData)
    }

    override suspend fun loadChildren(): List<Child> {
        val children = childrenRef
            .orderBy("age", Query.Direction.ASCENDING)
            .get()
            .await()
            .toObjects(Child::class.java)

        return ensureBuiltInAdmin(children)
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

    override suspend fun loadDogPlan(): DogPlanData {
        return DogPlanData(
            templates = loadDogPlanTemplates(),
            completions = loadDogPlanCompletions(),
            shifts = loadDogPlanShifts()
        )
    }

    override suspend fun loadDogPlanTemplates(): List<DogPlanTaskTemplate> {
        return dogPlanTemplatesRef
            .orderBy("sortOrder", Query.Direction.ASCENDING)
            .get()
            .await()
            .toObjects(DogPlanTaskTemplate::class.java)
    }

    override suspend fun loadDogPlanCompletions(): List<DogPlanTaskCompletion> {
        return dogPlanCompletionsRef
            .get()
            .await()
            .toObjects(DogPlanTaskCompletion::class.java)
    }

    override suspend fun loadDogPlanShifts(): List<DogPlanShift> {
        return dogPlanShiftsRef
            .get()
            .await()
            .toObjects(DogPlanShift::class.java)
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

    override suspend fun loadGameDailyRewards(): List<GameDailyReward> {
        return gameDailyRewardsRef.get().await().toObjects(GameDailyReward::class.java)
    }

    override suspend fun loadGameSettings(): GameSettings {
        return gameSettingsRef
            .get()
            .await()
            .toObject(GameSettings::class.java)
            ?: GameSettings()
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

    override suspend fun saveDogPlan(data: DogPlanData) {
        replaceCollection(dogPlanTemplatesRef, data.templates.map { prepareForSave(it) })
        replaceCollection(dogPlanCompletionsRef, data.completions.map { prepareForSave(it) })
        replaceCollection(dogPlanShiftsRef, data.shifts.map { prepareForSave(it) })
        updateFamilyTimestamp()
    }

    override suspend fun saveDogPlanTemplate(template: DogPlanTaskTemplate) {
        val prepared = prepareForSave(template)
        dogPlanTemplatesRef.document(prepared.id).set(prepared).await()
        updateFamilyTimestamp()
    }

    override suspend fun saveDogPlanCompletion(completion: DogPlanTaskCompletion) {
        val prepared = prepareForSave(completion)
        dogPlanCompletionsRef.document(prepared.id).set(prepared).await()
        updateFamilyTimestamp()
    }

    override suspend fun saveDogPlanShift(shift: DogPlanShift) {
        val prepared = prepareForSave(shift)
        dogPlanShiftsRef.document(prepared.id).set(prepared).await()
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

    override suspend fun saveGameDailyReward(reward: GameDailyReward) {
        val item = prepareForSave(reward)
        gameDailyRewardsRef.document(item.id).set(item).await()
        updateFamilyTimestamp()
    }

    override suspend fun saveGameSettings(settings: GameSettings) {
        gameSettingsRef.set(settings, SetOptions.merge()).await()
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
        age: Int,
        passwordRequired: Boolean,
        allowRememberLogin: Boolean,
        isBuiltInAdmin: Boolean
    ) {
        childrenRef.document(childId)
            .set(
                mapOf(
                    "name" to name,
                    "role" to role.name,
                    "password" to password,
                    "age" to age,
                    "passwordRequired" to passwordRequired,
                    "allowRememberLogin" to allowRememberLogin,
                    "isBuiltInAdmin" to isBuiltInAdmin,
                    "updatedAt" to Timestamp.now()
                ),
                SetOptions.merge()
            )
            .await()

        updateFamilyTimestamp()
    }



    override suspend fun updateChildProgress(
        childId: String,
        level: Int,
        experience: Int,
        availableSkillPoints: Int,
        intelligence: Int,
        strength: Int,
        agility: Int,
        endurance: Int,
        perception: Int,
        charisma: Int,
        luck: Int
    ) {
        childrenRef.document(childId)
            .set(
                mapOf(
                    "level" to level,
                    "experience" to experience,
                    "availableSkillPoints" to availableSkillPoints,
                    "intelligence" to intelligence,
                    "strength" to strength,
                    "agility" to agility,
                    "endurance" to endurance,
                    "perception" to perception,
                    "charisma" to charisma,
                    "luck" to luck,
                    "updatedAt" to Timestamp.now()
                ),
                SetOptions.merge()
            )
            .await()

        updateFamilyTimestamp()
    }


    override suspend fun changeChildCoinsAndExperience(
        childId: String,
        coinDelta: Int,
        experienceDelta: Int
    ): Child {
        val documentRef = childrenRef.document(childId)

        val updatedChild = db.runTransaction { transaction ->
            val snapshot = transaction.get(documentRef)

            val currentCoins = snapshot.getLong("coins")?.toInt() ?: 0
            val updatedCoins = currentCoins + coinDelta

            if (updatedCoins < 0) {
                throw IllegalStateException("Nicht genug Luna Coins")
            }

            val currentChild = Child(
                id = childId,
                familyId = snapshot.getString("familyId") ?: familyId,
                name = snapshot.getString("name") ?: "",
                coins = updatedCoins,
                level = snapshot.getLong("level")?.toInt() ?: 1,
                experience = snapshot.getLong("experience")?.toInt() ?: 0,
                availableSkillPoints = snapshot.getLong("availableSkillPoints")?.toInt() ?: 0,
                intelligence = snapshot.getLong("intelligence")?.toInt() ?: 1,
                strength = snapshot.getLong("strength")?.toInt() ?: 1,
                agility = snapshot.getLong("agility")?.toInt() ?: 1
            )

            val childWithProgress = ProgressManager.addExperience(
                child = currentChild,
                experienceDelta = experienceDelta
            )

            transaction.update(
                documentRef,
                mapOf(
                    "coins" to childWithProgress.coins,
                    "level" to childWithProgress.level,
                    "experience" to childWithProgress.experience,
                    "availableSkillPoints" to childWithProgress.availableSkillPoints,
                    "intelligence" to childWithProgress.intelligence,
                    "strength" to childWithProgress.strength,
                    "agility" to childWithProgress.agility,
                    "updatedAt" to Timestamp.now()
                )
            )

            childWithProgress
        }.await()

        updateFamilyTimestamp()

        return updatedChild
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

    override suspend fun deleteDogPlanTemplate(templateId: String) {
        dogPlanTemplatesRef.document(templateId).delete().await()
        updateFamilyTimestamp()
    }

    override suspend fun deleteDogPlanCompletion(completionId: String) {
        dogPlanCompletionsRef.document(completionId).delete().await()
        updateFamilyTimestamp()
    }

    override suspend fun deleteDogPlanShift(shiftId: String) {
        dogPlanShiftsRef.document(shiftId).delete().await()
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

    override suspend fun deleteGameDailyReward(rewardId: String) {
        gameDailyRewardsRef.document(rewardId).delete().await()
        updateFamilyTimestamp()
    }

    override suspend fun deleteGameHighscoresByGame(game: LunaGameType) {
        val snapshot = gameHighscoresRef
            .whereEqualTo("game", game.name)
            .get()
            .await()

        if (snapshot.isEmpty) {
            return
        }

        snapshot.documents
            .chunked(FIRESTORE_BATCH_LIMIT)
            .forEach { documents ->
                val batch = db.batch()
                documents.forEach { document ->
                    batch.delete(document.reference)
                }
                batch.commit().await()
            }

        updateFamilyTimestamp()
    }

    override suspend fun deleteAllGameHighscores() {
        val snapshot = gameHighscoresRef.get().await()

        if (snapshot.isEmpty) {
            return
        }

        snapshot.documents
            .chunked(FIRESTORE_BATCH_LIMIT)
            .forEach { documents ->
                val batch = db.batch()
                documents.forEach { document ->
                    batch.delete(document.reference)
                }
                batch.commit().await()
            }

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


    override suspend fun transferCoins(
        senderId: String,
        recipientId: String,
        amount: Int,
        senderLog: LogEntry,
        recipientLog: LogEntry
    ): Pair<Int, Int> {
        require(senderId != recipientId) { "Sender und Empfänger dürfen nicht identisch sein" }
        require(amount > 0) { "Der Betrag muss größer als 0 sein" }

        val senderRef = childrenRef.document(senderId)
        val recipientRef = childrenRef.document(recipientId)
        val senderLogItem = prepareForSave(senderLog)
        val recipientLogItem = prepareForSave(recipientLog)

        val result = db.runTransaction { transaction ->
            val senderSnapshot = transaction.get(senderRef)
            val recipientSnapshot = transaction.get(recipientRef)

            if (!senderSnapshot.exists()) {
                throw IllegalStateException("Sender wurde nicht gefunden")
            }
            if (!recipientSnapshot.exists()) {
                throw IllegalStateException("Empfänger wurde nicht gefunden")
            }

            val senderCoins = senderSnapshot.getLong("coins")?.toInt() ?: 0
            val recipientCoins = recipientSnapshot.getLong("coins")?.toInt() ?: 0
            if (senderCoins < amount) {
                throw IllegalStateException("Nicht genug Luna Coins")
            }

            val newSenderCoins = senderCoins - amount
            val newRecipientCoins = recipientCoins + amount
            val now = Timestamp.now()

            transaction.update(senderRef, mapOf("coins" to newSenderCoins, "updatedAt" to now))
            transaction.update(recipientRef, mapOf("coins" to newRecipientCoins, "updatedAt" to now))
            transaction.set(logsRef.document(senderLogItem.id), senderLogItem)
            transaction.set(logsRef.document(recipientLogItem.id), recipientLogItem)

            newSenderCoins to newRecipientCoins
        }.await()

        updateFamilyTimestamp()
        return result
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
        val fixedId = child.id.ifBlank {
            if (child.isBuiltInAdmin) BUILT_IN_ADMIN_ID else childrenRef.document().id
        }

        return child.copy(
            id = fixedId,
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

    private fun prepareForSave(template: DogPlanTaskTemplate): DogPlanTaskTemplate {
        val now = Date()
        return template.copy(
            id = template.id.ifBlank { dogPlanTemplatesRef.document().id },
            familyId = familyId,
            createdAt = template.createdAt ?: now,
            updatedAt = now
        )
    }

    private fun prepareForSave(completion: DogPlanTaskCompletion): DogPlanTaskCompletion {
        val now = Date()
        val completionId = completion.id.ifBlank {
            "${completion.date}_${completion.templateId}"
                .replace(":", "")
                .replace(".", "")
                .replace("/", "_")
                .replace(" ", "_")
        }

        return completion.copy(
            id = completionId,
            familyId = familyId,
            createdAt = completion.createdAt ?: now,
            updatedAt = now
        )
    }

    private fun prepareForSave(shift: DogPlanShift): DogPlanShift {
        val now = Date()
        val shiftId = shift.id.ifBlank {
            "dog_shift_${shift.date}"
                .replace(".", "")
                .replace("/", "_")
                .replace(" ", "_")
        }

        return shift.copy(
            id = shiftId,
            familyId = familyId,
            createdAt = shift.createdAt ?: now,
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

    private fun prepareForSave(reward: GameDailyReward): GameDailyReward {
        val now = Date()
        val rewardId = reward.id.ifBlank {
            "${reward.game}_${reward.childId}_${reward.date}"
        }

        return reward.copy(
            id = rewardId,
            familyId = familyId,
            createdAt = reward.createdAt ?: now,
            updatedAt = now
        )
    }

    private fun getDocumentId(item: Any): String {
        return when (item) {
            is Child -> item.id
            is TaskItem -> item.id
            is ShopItem -> item.id
            is DogScheduleItem -> item.id
            is DogPlanTaskTemplate -> item.id
            is DogPlanTaskCompletion -> item.id.ifBlank {
                "${item.date}_${item.templateId}"
                    .replace(":", "")
                    .replace(".", "")
                    .replace("/", "_")
                    .replace(" ", "_")
            }
            is DogPlanShift -> item.id.ifBlank {
                "dog_shift_${item.date}"
                    .replace(".", "")
                    .replace("/", "_")
                    .replace(" ", "_")
            }
            is LogEntry -> item.id
            is LuckyWheelUsage -> item.id.ifBlank { "${item.childId}_${item.date}" }
            is GameHighscore -> item.id.ifBlank { "${item.game}_${item.childId}_${item.level}_${item.scoreType}" }
            is GameDailyReward -> item.id.ifBlank { "${item.game}_${item.childId}_${item.date}" }
            else -> throw IllegalArgumentException("Unbekannter Firestore-Typ: ${item::class.java.simpleName}")
        }
    }

    private fun ensureBuiltInAdmin(children: List<Child>): List<Child> {
        if (children.any { it.isBuiltInAdmin || it.id == BUILT_IN_ADMIN_ID }) {
            return children.map { child ->
                if (child.isBuiltInAdmin || child.id == BUILT_IN_ADMIN_ID) {
                    child.copy(
                        id = BUILT_IN_ADMIN_ID,
                        role = UserRole.ADMIN,
                        passwordRequired = true,
                        allowRememberLogin = true,
                        isBuiltInAdmin = true
                    )
                } else {
                    child
                }
            }
        }

        if (children.any { it.role == UserRole.ADMIN }) {
            return children
        }

        val builtInAdmin = Child(
            id = BUILT_IN_ADMIN_ID,
            name = "Thomas",
            coins = 0,
            role = UserRole.ADMIN,
            password = "",
            age = 99,
            passwordRequired = true,
            allowRememberLogin = true,
            isBuiltInAdmin = true
        )

        return (children + builtInAdmin).sortedBy { it.age }
    }

    companion object {
        private const val MAX_ACTIVE_LOGS = 2000L
        private const val FIRESTORE_BATCH_LIMIT = 450
        const val BUILT_IN_ADMIN_ID = "built_in_admin"
    }
}