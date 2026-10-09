package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameRepository
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class GameScreen {
    AUTH,
    CLASS_SELECTION,
    MAIN_DASHBOARD
}

data class BattleLogEntry(
    val text: String,
    val isCritical: Boolean = false,
    val isPlayerAction: Boolean = true,
    val colorHex: String? = null
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application.applicationContext)

    // Current Screen
    private val _currentScreen = MutableStateFlow<GameScreen>(GameScreen.AUTH)
    val currentScreen: StateFlow<GameScreen> = _currentScreen.asStateFlow()

    // Auth & User Flow
    val currentUser = repository.currentUser
    val activeCharacter = repository.activeCharacter
    val characterInventory = repository.characterInventory

    // Class selection state
    private val _selectedClass = MutableStateFlow(HeroClass.WARRIOR)
    val selectedClass: StateFlow<HeroClass> = _selectedClass.asStateFlow()

    // Nickname dialog state
    private val _showNicknameDialog = MutableStateFlow(false)
    val showNicknameDialog: StateFlow<Boolean> = _showNicknameDialog.asStateFlow()

    // UI Feedback
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // Detail modal for inspecting inventory item
    private val _inspectedItem = MutableStateFlow<EquipmentItem?>(null)
    val inspectedItem: StateFlow<EquipmentItem?> = _inspectedItem.asStateFlow()

    // Dungeon battle simulator state
    private val _battleLogs = MutableStateFlow<List<BattleLogEntry>>(emptyList())
    val battleLogs: StateFlow<List<BattleLogEntry>> = _battleLogs.asStateFlow()

    private val _isBattling = MutableStateFlow(false)
    val isBattling: StateFlow<Boolean> = _isBattling.asStateFlow()

    private val _lastBattleWon = MutableStateFlow<Boolean?>(null)
    val lastBattleWon: StateFlow<Boolean?> = _lastBattleWon.asStateFlow()

    init {
        // Evaluate initial screen based on persisted session
        val user = repository.currentUser.value
        val char = repository.activeCharacter.value
        if (user != null) {
            if (char != null) {
                _currentScreen.value = GameScreen.MAIN_DASHBOARD
            } else {
                _currentScreen.value = GameScreen.CLASS_SELECTION
            }
        } else {
            _currentScreen.value = GameScreen.AUTH
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun login(user: String, pass: String) {
        _authError.value = null
        val result = repository.login(user, pass)
        result.fold(
            onSuccess = {
                val char = repository.activeCharacter.value
                if (char != null) {
                    _currentScreen.value = GameScreen.MAIN_DASHBOARD
                    _toastMessage.value = "Selamat datang kembali, ${char.nickname}!"
                } else {
                    _currentScreen.value = GameScreen.CLASS_SELECTION
                }
            },
            onFailure = { error ->
                _authError.value = error.message ?: "Gagal masuk."
            }
        )
    }

    fun register(user: String, pass: String) {
        _authError.value = null
        val result = repository.register(user, pass)
        result.fold(
            onSuccess = {
                _currentScreen.value = GameScreen.CLASS_SELECTION
                _toastMessage.value = "Akun berhasil dibuat! Silakan pilih class karaktermu."
            },
            onFailure = { error ->
                _authError.value = error.message ?: "Gagal mendaftar."
            }
        )
    }

    fun selectClass(heroClass: HeroClass) {
        _selectedClass.value = heroClass
    }

    fun openNicknameDialog() {
        _showNicknameDialog.value = true
    }

    fun closeNicknameDialog() {
        _showNicknameDialog.value = false
    }

    fun confirmNicknameAndCreate(nickname: String) {
        val result = repository.createCharacter(nickname, _selectedClass.value)
        result.fold(
            onSuccess = { char ->
                _showNicknameDialog.value = false
                _currentScreen.value = GameScreen.MAIN_DASHBOARD
                _toastMessage.value = "Karakter ${char.nickname} (${char.heroClass.displayName}) berhasil diciptakan!"
            },
            onFailure = { err ->
                _toastMessage.value = err.message ?: "Gagal membuat karakter."
            }
        )
    }

    fun startNewCharacterFlow() {
        _currentScreen.value = GameScreen.CLASS_SELECTION
    }

    fun switchCharacter(character: HeroCharacter) {
        repository.setActiveCharacter(character)
        _currentScreen.value = GameScreen.MAIN_DASHBOARD
        _toastMessage.value = "Beralih ke karakter ${character.nickname}!"
    }

    fun getUserCharacters(): List<HeroCharacter> {
        val user = repository.currentUser.value ?: return emptyList()
        return repository.getCharactersForUser(user.username)
    }

    fun inspectItem(item: EquipmentItem?) {
        _inspectedItem.value = item
    }

    fun equipItem(item: EquipmentItem) {
        val char = activeCharacter.value ?: return
        if (item.requiredClass != null && item.requiredClass != char.heroClass) {
            _toastMessage.value = "Equipment ini hanya untuk ${item.requiredClass.displayName}!"
            return
        }
        repository.equipItem(item)
        _inspectedItem.value = item.copy(isEquipped = true)
        _toastMessage.value = "${item.name} berhasil dipasang!"
    }

    fun unequipItem(item: EquipmentItem) {
        repository.unequipItem(item)
        _inspectedItem.value = item.copy(isEquipped = false)
        _toastMessage.value = "${item.name} berhasil dilepas!"
    }

    fun sellItem(item: EquipmentItem) {
        val goldEarned = repository.sellItem(item)
        if (goldEarned > 0) {
            _inspectedItem.value = null
            _toastMessage.value = "Berhasil menjual ${item.name} seharga +$goldEarned Gold!"
        } else {
            _toastMessage.value = "Lepas equipment terlebih dahulu sebelum menjual!"
        }
    }

    fun openLootChest() {
        val result = repository.openLootChest(costGold = 250)
        result.fold(
            onSuccess = { item ->
                _inspectedItem.value = item
                _toastMessage.value = "Mendapatkan: [${item.rarity.label}] ${item.name}!"
            },
            onFailure = { err ->
                _toastMessage.value = err.message ?: "Gagal membuka peti."
            }
        )
    }

    fun trainCharacter() {
        val char = activeCharacter.value ?: return
        val expGain = 45
        val goldGain = 80
        val prevLevel = char.level

        repository.addExpAndGold(expGain, goldGain)
        val newChar = repository.activeCharacter.value
        if (newChar != null && newChar.level > prevLevel) {
            _toastMessage.value = "LEVEL UP! Sekarang Level ${newChar.level}! Stat bertambah kuat!"
        } else {
            _toastMessage.value = "Latihan selesai! +$expGain EXP, +$goldGain Gold."
        }
    }

    fun startDungeonBattle() {
        val char = activeCharacter.value ?: return
        val stats = char.calculateStats(characterInventory.value.filter { it.isEquipped })

        if (_isBattling.value) return
        _isBattling.value = true
        _lastBattleWon.value = null
        _battleLogs.value = listOf(
            BattleLogEntry("⚔️ Menjelajahi Ruang Bawah Tanah Kuno...", isPlayerAction = true)
        )

        viewModelScope.launch {
            delay(500)
            val monsterName = listOf("Gorgon Batu", "Siluman Serigala Gua", "Ksatria Kegelapan Tengkorak", "Naga Api Kerdil").random()
            var monsterHp = (stats.hp * 0.95).toInt()
            var playerHp = stats.hp
            val monsterAtk = (stats.def * 1.35).toInt().coerceAtLeast(35)

            val logs = mutableListOf<BattleLogEntry>()
            logs.add(BattleLogEntry("⚠️ Monster liar muncul: $monsterName (HP $monsterHp)!"))
            _battleLogs.value = logs.toList()

            // Turn 1: Player attack based on class specialities
            delay(700)
            val isCrit = (1..100).random() <= stats.critRate
            val critMultiplier = if (isCrit) 2.2 else 1.0

            val playerDmg = when (char.heroClass) {
                HeroClass.WARRIOR -> {
                    // Solid damage + high def
                    ((stats.physicalAtk * 1.25) * critMultiplier).toInt()
                }
                HeroClass.MAGE -> {
                    // Huge magic burst
                    ((stats.magicAtk * 1.55) * critMultiplier).toInt()
                }
                HeroClass.ARCHER -> {
                    // Highest piercing single hit
                    ((stats.physicalAtk * 1.65) * critMultiplier).toInt()
                }
                HeroClass.ASSASSIN -> {
                    // Huge critical burst
                    ((stats.physicalAtk * 1.4) * (if (isCrit) 2.5 else 1.0)).toInt()
                }
            }

            monsterHp -= playerDmg
            val attackDesc = when (char.heroClass) {
                HeroClass.WARRIOR -> "menebaskan Pedang Baja Perkasa"
                HeroClass.MAGE -> "merapalkan Mantra Badai Arcane"
                HeroClass.ARCHER -> "menembakkan Panah Menembus Zirah"
                HeroClass.ASSASSIN -> "menghujamkan Belati Mematikan dari Bayangan"
            }

            val critTag = if (isCrit) "💥 [CRITICAL HIT!]" else "⚔️"
            logs.add(BattleLogEntry("$critTag ${char.nickname} $attackDesc! Musuh terkena $playerDmg DMG! (Sisa HP: ${monsterHp.coerceAtLeast(0)})", isCritical = isCrit))
            _battleLogs.value = logs.toList()

            // Turn 2: Monster counter-attacks
            delay(700)
            if (monsterHp > 0) {
                // Warrior passive absorbs 25% damage
                val damageReduction = if (char.heroClass == HeroClass.WARRIOR) 0.75 else 1.0
                val rawMonsterDmg = (monsterAtk - (stats.def * 0.35)).toInt().coerceAtLeast(18)
                val monsterDmg = (rawMonsterDmg * damageReduction).toInt()

                playerHp -= monsterDmg
                val defenseNote = if (char.heroClass == HeroClass.WARRIOR) " (Pasif Iron Wall menyerap 25% damage!)" else ""
                logs.add(BattleLogEntry("🛡️ $monsterName membalas serangan! ${char.nickname} menerima $monsterDmg DMG$defenseNote. (Sisa HP: ${playerHp.coerceAtLeast(0)})", isPlayerAction = false))
                _battleLogs.value = logs.toList()
            }

            // Turn 3: Final Strike
            delay(700)
            val finishDmg = (playerDmg * 1.1).toInt()
            logs.add(BattleLogEntry("✨ Serangan pamungkas! $monsterName roboh tak berdaya! (Damage: $finishDmg)", isCritical = true))
            _battleLogs.value = logs.toList()

            // Rewards
            delay(500)
            val goldReward = (120..250).random()
            val expReward = (60..100).random()
            repository.addExpAndGold(expReward, goldReward)

            logs.add(BattleLogEntry("🏆 Kemenangan! Hadiah: +$goldReward Gold & +$expReward EXP!"))
            _battleLogs.value = logs.toList()
            _lastBattleWon.value = true
            _isBattling.value = false

            val updatedChar = repository.activeCharacter.value
            _toastMessage.value = "Dungeon Selesai! +$goldReward Gold & +$expReward EXP."
        }
    }

    fun logout() {
        repository.logout()
        _currentScreen.value = GameScreen.AUTH
        _toastMessage.value = "Berhasil keluar dari game."
    }
}
