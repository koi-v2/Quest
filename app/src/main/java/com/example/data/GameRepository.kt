package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class GameRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("valiant_rpg_prefs", Context.MODE_PRIVATE)

    // Current State Flows
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val _activeCharacter = MutableStateFlow<HeroCharacter?>(null)
    val activeCharacter: StateFlow<HeroCharacter?> = _activeCharacter.asStateFlow()

    private val _characterInventory = MutableStateFlow<List<EquipmentItem>>(emptyList())
    val characterInventory: StateFlow<List<EquipmentItem>> = _characterInventory.asStateFlow()

    private val _dailyMissions = MutableStateFlow<List<DailyMission>>(emptyList())
    val dailyMissions: StateFlow<List<DailyMission>> = _dailyMissions.asStateFlow()

    private val _dailyChestClaimed = MutableStateFlow(false)
    val dailyChestClaimed: StateFlow<Boolean> = _dailyChestClaimed.asStateFlow()

    init {
        // Load initial session if exists
        loadSavedSession()
    }

    private fun loadSavedSession() {
        val lastUserJson = prefs.getString("current_user", null)
        if (lastUserJson != null) {
            try {
                val obj = JSONObject(lastUserJson)
                val account = UserAccount(
                    username = obj.getString("username"),
                    passwordHash = obj.getString("password"),
                    activeCharacterId = obj.optString("activeCharacterId", null)
                )
                _currentUser.value = account

                account.activeCharacterId?.let { charId ->
                    loadCharacterById(charId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun register(username: String, password: String):Result<UserAccount> {
        val cleanName = username.trim()
        if (cleanName.length < 3) {
            return Result.failure(IllegalArgumentException("Username minimal 3 karakter."))
        }
        if (password.length < 4) {
            return Result.failure(IllegalArgumentException("Password minimal 4 karakter."))
        }

        val allUsers = getAllUsers()
        if (allUsers.any { it.username.equals(cleanName, ignoreCase = true) }) {
            return Result.failure(IllegalArgumentException("Username sudah terdaftar! Silakan login."))
        }

        val newAccount = UserAccount(username = cleanName, passwordHash = password)
        val updatedList = allUsers + newAccount
        saveAllUsers(updatedList)

        // Auto login
        _currentUser.value = newAccount
        _activeCharacter.value = null
        _characterInventory.value = emptyList()
        persistCurrentSession(newAccount)

        return Result.success(newAccount)
    }

    fun login(username: String, password: String): Result<UserAccount> {
        val cleanName = username.trim()
        val allUsers = getAllUsers()
        val found = allUsers.firstOrNull {
            it.username.equals(cleanName, ignoreCase = true) && it.passwordHash == password
        }

        return if (found != null) {
            _currentUser.value = found
            persistCurrentSession(found)

            // Try to load user's character if exists
            val userChars = getCharactersForUser(found.username)
            if (userChars.isNotEmpty()) {
                val charToLoad = userChars.firstOrNull { it.id == found.activeCharacterId } ?: userChars.first()
                setActiveCharacter(charToLoad)
            } else {
                _activeCharacter.value = null
                _characterInventory.value = emptyList()
            }

            Result.success(found)
        } else {
            Result.failure(IllegalArgumentException("Username atau Password salah."))
        }
    }

    fun logout() {
        _currentUser.value = null
        _activeCharacter.value = null
        _characterInventory.value = emptyList()
        prefs.edit().remove("current_user").apply()
    }

    fun createCharacter(nickname: String, heroClass: HeroClass): Result<HeroCharacter> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("Pemain belum login."))
        val cleanNick = nickname.trim()

        if (cleanNick.length < 3 || cleanNick.length > 16) {
            return Result.failure(IllegalArgumentException("Nickname harus terdiri dari 3 hingga 16 karakter."))
        }

        val newCharId = "char_" + UUID.randomUUID().toString().take(8)
        val newChar = HeroCharacter(
            id = newCharId,
            userId = user.username,
            nickname = cleanNick,
            heroClass = heroClass,
            level = 1,
            currentExp = 0,
            maxExp = 100,
            gold = 1500,
            diamonds = 50
        )

        // Save character
        val allChars = getAllCharacters()
        saveAllCharacters(allChars + newChar)

        // Generate class-specific starter inventory
        val starterGear = StarterEquipmentData.generateStarterInventoryFor(heroClass)
        saveInventoryForCharacter(newCharId, starterGear)

        // Generate daily missions
        val initialMissions = StarterEquipmentData.generateDefaultDailyMissions()
        saveDailyMissionsForCharacter(newCharId, initialMissions)
        setDailyChestClaimed(newCharId, false)
        _dailyMissions.value = initialMissions
        _dailyChestClaimed.value = false

        // Update active user's character link
        val updatedUser = user.copy(activeCharacterId = newCharId)
        _currentUser.value = updatedUser
        updateUser(updatedUser)
        persistCurrentSession(updatedUser)

        _activeCharacter.value = newChar
        _characterInventory.value = starterGear

        return Result.success(newChar)
    }

    fun setActiveCharacter(character: HeroCharacter) {
        _activeCharacter.value = character
        val items = getInventoryForCharacter(character.id)
        _characterInventory.value = items

        _dailyMissions.value = getDailyMissionsForCharacter(character.id)
        _dailyChestClaimed.value = isDailyChestClaimed(character.id)

        val user = _currentUser.value
        if (user != null) {
            val updatedUser = user.copy(activeCharacterId = character.id)
            _currentUser.value = updatedUser
            updateUser(updatedUser)
            persistCurrentSession(updatedUser)
        }
    }

    fun equipItem(itemToEquip: EquipmentItem) {
        val currentChar = _activeCharacter.value ?: return
        val currentItems = _characterInventory.value.toMutableList()

        // Verify class requirement
        if (itemToEquip.requiredClass != null && itemToEquip.requiredClass != currentChar.heroClass) {
            return
        }

        // Unequip currently equipped item in this type slot
        val updated = currentItems.map { item ->
            when {
                item.id == itemToEquip.id -> item.copy(isEquipped = true)
                item.type == itemToEquip.type && item.isEquipped -> item.copy(isEquipped = false)
                else -> item
            }
        }

        _characterInventory.value = updated
        saveInventoryForCharacter(currentChar.id, updated)
        updateMissionProgress(MissionCategory.EQUIP, 1)
    }

    fun unequipItem(itemToUnequip: EquipmentItem) {
        val currentChar = _activeCharacter.value ?: return
        val currentItems = _characterInventory.value.map { item ->
            if (item.id == itemToUnequip.id) item.copy(isEquipped = false) else item
        }

        _characterInventory.value = currentItems
        saveInventoryForCharacter(currentChar.id, currentItems)
    }

    fun sellItem(itemToSell: EquipmentItem): Int {
        val currentChar = _activeCharacter.value ?: return 0
        if (itemToSell.isEquipped) {
            // Must unequip first or auto-unequip
            return 0
        }

        val goldValue = when (itemToSell.rarity) {
            Rarity.COMMON -> 150
            Rarity.RARE -> 450
            Rarity.EPIC -> 1200
            Rarity.LEGENDARY -> 3000
        }

        val currentItems = _characterInventory.value.filter { it.id != itemToSell.id }
        _characterInventory.value = currentItems
        saveInventoryForCharacter(currentChar.id, currentItems)

        // Add gold to character
        val updatedChar = currentChar.copy(gold = currentChar.gold + goldValue)
        updateCharacter(updatedChar)

        return goldValue
    }

    fun openLootChest(costGold: Int = 250): Result<EquipmentItem> {
        val currentChar = _activeCharacter.value ?: return Result.failure(IllegalStateException("No character"))
        if (currentChar.gold < costGold) {
            return Result.failure(IllegalStateException("Gold tidak mencukupi! Butuh $costGold Gold."))
        }

        val newItem = StarterEquipmentData.generateRandomLootFor(currentChar.heroClass)
        val updatedItems = _characterInventory.value + newItem
        _characterInventory.value = updatedItems
        saveInventoryForCharacter(currentChar.id, updatedItems)

        val updatedChar = currentChar.copy(gold = currentChar.gold - costGold)
        updateCharacter(updatedChar)

        updateMissionProgress(MissionCategory.CHEST, 1)

        return Result.success(newItem)
    }

    fun updateMissionProgress(category: MissionCategory, amount: Int = 1) {
        val currentChar = _activeCharacter.value ?: return
        val currentMissions = _dailyMissions.value
        val updated = currentMissions.map { m ->
            if (m.category == category && m.currentCount < m.targetCount) {
                m.copy(currentCount = (m.currentCount + amount).coerceAtMost(m.targetCount))
            } else {
                m
            }
        }
        _dailyMissions.value = updated
        saveDailyMissionsForCharacter(currentChar.id, updated)
    }

    fun claimDailyMission(missionId: String): Result<DailyMission> {
        val currentChar = _activeCharacter.value ?: return Result.failure(IllegalStateException("No character"))
        val currentMissions = _dailyMissions.value
        val mission = currentMissions.firstOrNull { it.id == missionId }
            ?: return Result.failure(IllegalArgumentException("Mission not found"))
        if (mission.isClaimed || mission.currentCount < mission.targetCount) {
            return Result.failure(IllegalStateException("Mission cannot be claimed"))
        }

        val updated = currentMissions.map { if (it.id == missionId) it.copy(isClaimed = true) else it }
        _dailyMissions.value = updated
        saveDailyMissionsForCharacter(currentChar.id, updated)

        addExpAndGold(mission.expReward, mission.goldReward)
        if (mission.diamondReward > 0) {
            val updatedChar = _activeCharacter.value!!.copy(diamonds = _activeCharacter.value!!.diamonds + mission.diamondReward)
            updateCharacter(updatedChar)
        }
        return Result.success(mission)
    }

    fun claimGrandDailyChest(): Result<Unit> {
        val currentChar = _activeCharacter.value ?: return Result.failure(IllegalStateException("No character"))
        if (_dailyChestClaimed.value) return Result.failure(IllegalStateException("Already claimed"))
        val allDone = _dailyMissions.value.all { it.currentCount >= it.targetCount }
        if (!allDone) return Result.failure(IllegalStateException("Complete all missions first"))

        _dailyChestClaimed.value = true
        setDailyChestClaimed(currentChar.id, true)

        addExpAndGold(250, 600)
        val updatedChar = _activeCharacter.value!!.copy(diamonds = _activeCharacter.value!!.diamonds + 15)
        updateCharacter(updatedChar)
        return Result.success(Unit)
    }

    fun resetDailyMissions() {
        val currentChar = _activeCharacter.value ?: return
        val defaultMissions = StarterEquipmentData.generateDefaultDailyMissions()
        _dailyMissions.value = defaultMissions
        saveDailyMissionsForCharacter(currentChar.id, defaultMissions)
        _dailyChestClaimed.value = false
        setDailyChestClaimed(currentChar.id, false)
    }

    fun addExpAndGold(expGained: Int, goldGained: Int) {
        val currentChar = _activeCharacter.value ?: return
        var newExp = currentChar.currentExp + expGained
        var newLevel = currentChar.level
        var newMaxExp = currentChar.maxExp

        while (newExp >= newMaxExp) {
            newExp -= newMaxExp
            newLevel += 1
            newMaxExp = (newMaxExp * 1.35).toInt()
        }

        val updatedChar = currentChar.copy(
            level = newLevel,
            currentExp = newExp,
            maxExp = newMaxExp,
            gold = currentChar.gold + goldGained
        )
        updateCharacter(updatedChar)
    }

    private fun updateCharacter(character: HeroCharacter) {
        _activeCharacter.value = character
        val allChars = getAllCharacters().map {
            if (it.id == character.id) character else it
        }
        saveAllCharacters(allChars)
    }

    private fun loadCharacterById(charId: String) {
        val allChars = getAllCharacters()
        val found = allChars.firstOrNull { it.id == charId }
        if (found != null) {
            _activeCharacter.value = found
            _characterInventory.value = getInventoryForCharacter(charId)
        }
    }

    fun getCharactersForUser(userId: String): List<HeroCharacter> {
        return getAllCharacters().filter { it.userId == userId }
    }

    // JSON Persistence Helpers
    private fun getAllUsers(): List<UserAccount> {
        val jsonStr = prefs.getString("all_users", null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<UserAccount>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    UserAccount(
                        username = obj.getString("username"),
                        passwordHash = obj.getString("password"),
                        activeCharacterId = obj.optString("activeCharacterId", null)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveAllUsers(users: List<UserAccount>) {
        val array = JSONArray()
        for (u in users) {
            val obj = JSONObject()
            obj.put("username", u.username)
            obj.put("password", u.passwordHash)
            obj.put("activeCharacterId", u.activeCharacterId)
            array.put(obj)
        }
        prefs.edit().putString("all_users", array.toString()).apply()
    }

    private fun updateUser(user: UserAccount) {
        val users = getAllUsers().map {
            if (it.username.equals(user.username, ignoreCase = true)) user else it
        }
        saveAllUsers(users)
    }

    private fun persistCurrentSession(user: UserAccount) {
        val obj = JSONObject()
        obj.put("username", user.username)
        obj.put("password", user.passwordHash)
        obj.put("activeCharacterId", user.activeCharacterId)
        prefs.edit().putString("current_user", obj.toString()).apply()
    }

    private fun getAllCharacters(): List<HeroCharacter> {
        val jsonStr = prefs.getString("all_characters", null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<HeroCharacter>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    HeroCharacter(
                        id = obj.getString("id"),
                        userId = obj.getString("userId"),
                        nickname = obj.getString("nickname"),
                        heroClass = HeroClass.valueOf(obj.getString("heroClass")),
                        level = obj.optInt("level", 1),
                        currentExp = obj.optInt("currentExp", 0),
                        maxExp = obj.optInt("maxExp", 100),
                        gold = obj.optInt("gold", 1500),
                        diamonds = obj.optInt("diamonds", 50),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveAllCharacters(characters: List<HeroCharacter>) {
        val array = JSONArray()
        for (c in characters) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("userId", c.userId)
            obj.put("nickname", c.nickname)
            obj.put("heroClass", c.heroClass.name)
            obj.put("level", c.level)
            obj.put("currentExp", c.currentExp)
            obj.put("maxExp", c.maxExp)
            obj.put("gold", c.gold)
            obj.put("diamonds", c.diamonds)
            obj.put("createdAt", c.createdAt)
            array.put(obj)
        }
        prefs.edit().putString("all_characters", array.toString()).apply()
    }

    private fun getInventoryForCharacter(charId: String): List<EquipmentItem> {
        val jsonStr = prefs.getString("inventory_$charId", null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<EquipmentItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val reqClassStr = obj.optString("requiredClass", "")
                val reqClass = if (reqClassStr.isNotEmpty()) HeroClass.valueOf(reqClassStr) else null
                list.add(
                    EquipmentItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        type = EquipmentType.valueOf(obj.getString("type")),
                        requiredClass = reqClass,
                        rarity = Rarity.valueOf(obj.optString("rarity", Rarity.COMMON.name)),
                        isEquipped = obj.optBoolean("isEquipped", false),
                        hpBonus = obj.optInt("hpBonus", 0),
                        mpBonus = obj.optInt("mpBonus", 0),
                        atkBonus = obj.optInt("atkBonus", 0),
                        matkBonus = obj.optInt("matkBonus", 0),
                        defBonus = obj.optInt("defBonus", 0),
                        critBonus = obj.optInt("critBonus", 0),
                        speedBonus = obj.optInt("speedBonus", 0),
                        description = obj.optString("description", ""),
                        iconKey = obj.optString("iconKey", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveInventoryForCharacter(charId: String, items: List<EquipmentItem>) {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("type", item.type.name)
            obj.put("requiredClass", item.requiredClass?.name ?: "")
            obj.put("rarity", item.rarity.name)
            obj.put("isEquipped", item.isEquipped)
            obj.put("hpBonus", item.hpBonus)
            obj.put("mpBonus", item.mpBonus)
            obj.put("atkBonus", item.atkBonus)
            obj.put("matkBonus", item.matkBonus)
            obj.put("defBonus", item.defBonus)
            obj.put("critBonus", item.critBonus)
            obj.put("speedBonus", item.speedBonus)
            obj.put("description", item.description)
            obj.put("iconKey", item.iconKey)
            array.put(obj)
        }
        prefs.edit().putString("inventory_$charId", array.toString()).apply()
    }
}
