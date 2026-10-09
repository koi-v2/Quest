package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class HeroClass(
    val displayName: String,
    val title: String,
    val role: String,
    val mainAdvantage: String,
    val passiveName: String,
    val passiveDescription: String,
    val playstyleDescription: String,
    val baseHp: Int,
    val baseMp: Int,
    val basePhysicalAtk: Int,
    val baseMagicAtk: Int,
    val baseDef: Int,
    val baseCritRate: Int, // in percentage, e.g. 6 = 6%
    val baseSpeed: Int,
    val themeColor: Color,
    val glowColor: Color
) {
    WARRIOR(
        displayName = "Warrior",
        title = "Ksatria Pelindung",
        role = "Tanker & Heavy Defender",
        mainAdvantage = "DEF Lebih Kuat",
        passiveName = "Iron Wall & Bastion",
        passiveDescription = "Mengurangi semua damage yang diterima sebesar 25%, meningkatkan pertahanan alami, dan memantulkan 10% serangan musuh.",
        playstyleDescription = "Fokus pada ketahanan tinggi dan pertahanan tak tertembus. Sangat tangguh menahan serangan besar di garis depan.",
        baseHp = 1350,
        baseMp = 300,
        basePhysicalAtk = 85,
        baseMagicAtk = 20,
        baseDef = 130,
        baseCritRate = 6,
        baseSpeed = 50,
        themeColor = WarriorRed,
        glowColor = WarriorRedGlow
    ),
    MAGE(
        displayName = "Mage",
        title = "Penyihir Elemen Arcane",
        role = "Burst Magic Caster",
        mainAdvantage = "MAG Lebih Kuat",
        passiveName = "Arcane Surge & Mana Well",
        passiveDescription = "Meningkatkan Magic Damage sebesar +35%, cadangan mana masif, serta meregenerasi 5% MP setiap detik dalam pertarungan.",
        playstyleDescription = "Pengendali elemen dengan daya rusak sihir luar biasa dari jarak jauh. Mengubah energi sihir murni menjadi kehancuran fatal.",
        baseHp = 750,
        baseMp = 1200,
        basePhysicalAtk = 30,
        baseMagicAtk = 165,
        baseDef = 45,
        baseCritRate = 8,
        baseSpeed = 60,
        themeColor = MagePurple,
        glowColor = MagePurpleGlow
    ),
    ARCHER(
        displayName = "Archer",
        title = "Pemanah Elit Pemburu",
        role = "High Physical DPS",
        mainAdvantage = "ATK Lebih Kuat",
        passiveName = "Eagle Eye & Piercing Arrow",
        passiveDescription = "Meningkatkan Physical Attack sebesar +30%, menembak dengan akurasi 100%, dan tembakan menembus 25% Armor lawan.",
        playstyleDescription = "Penembak jitu mematikan dengan Physical Damage konstan tertinggi. Menghabisi target dari kejauhan tanpa ampun.",
        baseHp = 880,
        baseMp = 450,
        basePhysicalAtk = 155,
        baseMagicAtk = 25,
        baseDef = 65,
        baseCritRate = 16,
        baseSpeed = 85,
        themeColor = ArcherGreen,
        glowColor = ArcherGreenGlow
    ),
    ASSASSIN(
        displayName = "Assassin",
        title = "Bayangan Maut Siluman",
        role = "Lethal Burst Striker",
        mainAdvantage = "Critical Lebih Sering",
        passiveName = "Shadow Step & Lethal Precision",
        passiveDescription = "Critical Rate bawaan tinggi (+25%), melipatgandakan Critical Damage hingga 220%, serta kelincahan gerak tercepat di medan tempur.",
        playstyleDescription = "Ahli eksekusi cepat dari kegelapan. Mengandalkan serangan critical mematikan beruntun dan kecepatan tinggi untuk merobek musuh.",
        baseHp = 820,
        baseMp = 480,
        basePhysicalAtk = 120,
        baseMagicAtk = 25,
        baseDef = 55,
        baseCritRate = 42,
        baseSpeed = 110,
        themeColor = AssassinViolet,
        glowColor = AssassinCritCrimson
    )
}

enum class EquipmentType(val displayName: String, val slotName: String) {
    WEAPON("Senjata Utama", "Weapon"),
    ARMOR("Zirah Pelindung", "Armor"),
    HELMET("Helm / Tudung", "Helmet"),
    BOOTS("Sepatu Tempur", "Boots"),
    ACCESSORY("Aksesoris / Tameng", "Accessory")
}

enum class Rarity(val label: String, val color: Color, val borderAlpha: Float) {
    COMMON("Common", RarityCommon, 0.4f),
    RARE("Rare", RarityRare, 0.7f),
    EPIC("Epic", RarityEpic, 0.85f),
    LEGENDARY("Legendary", RarityLegendary, 1.0f)
}

data class EquipmentItem(
    val id: String,
    val name: String,
    val type: EquipmentType,
    val requiredClass: HeroClass? = null, // null means usable by all classes
    val rarity: Rarity = Rarity.COMMON,
    val isEquipped: Boolean = false,
    val hpBonus: Int = 0,
    val mpBonus: Int = 0,
    val atkBonus: Int = 0,
    val matkBonus: Int = 0,
    val defBonus: Int = 0,
    val critBonus: Int = 0, // e.g. 5 = +5%
    val speedBonus: Int = 0,
    val description: String = "",
    val iconKey: String = ""
)

data class HeroCharacter(
    val id: String,
    val userId: String,
    val nickname: String,
    val heroClass: HeroClass,
    val level: Int = 1,
    val currentExp: Int = 0,
    val maxExp: Int = 100,
    val gold: Int = 1500,
    val diamonds: Int = 50,
    val createdAt: Long = System.currentTimeMillis()
) {
    // Stat Calculation considering level scaling and equipped items
    fun calculateStats(equippedItems: List<EquipmentItem>): CalculatedStats {
        val levelMultiplier = 1.0 + (level - 1) * 0.12 // 12% stat increase per level

        var bonusHp = 0
        var bonusMp = 0
        var bonusAtk = 0
        var bonusMatk = 0
        var bonusDef = 0
        var bonusCrit = 0
        var bonusSpeed = 0

        for (item in equippedItems) {
            bonusHp += item.hpBonus
            bonusMp += item.mpBonus
            bonusAtk += item.atkBonus
            bonusMatk += item.matkBonus
            bonusDef += item.defBonus
            bonusCrit += item.critBonus
            bonusSpeed += item.speedBonus
        }

        val totalHp = ((heroClass.baseHp * levelMultiplier) + bonusHp).toInt()
        val totalMp = ((heroClass.baseMp * levelMultiplier) + bonusMp).toInt()
        val totalAtk = ((heroClass.basePhysicalAtk * levelMultiplier) + bonusAtk).toInt()
        val totalMatk = ((heroClass.baseMagicAtk * levelMultiplier) + bonusMatk).toInt()
        val totalDef = ((heroClass.baseDef * levelMultiplier) + bonusDef).toInt()
        val totalCrit = (heroClass.baseCritRate + bonusCrit).coerceAtMost(100)
        val totalSpeed = ((heroClass.baseSpeed * (1.0 + (level - 1) * 0.05)) + bonusSpeed).toInt()

        // Formula Combat Power (CP)
        val combatPower = (totalHp * 0.25 + totalMp * 0.15 + totalAtk * 1.8 + totalMatk * 1.8 + totalDef * 1.4 + totalCrit * 12 + totalSpeed * 1.1).toInt()

        return CalculatedStats(
            hp = totalHp,
            mp = totalMp,
            physicalAtk = totalAtk,
            magicAtk = totalMatk,
            def = totalDef,
            critRate = totalCrit,
            speed = totalSpeed,
            combatPower = combatPower,
            bonusHp = bonusHp,
            bonusMp = bonusMp,
            bonusAtk = bonusAtk,
            bonusMatk = bonusMatk,
            bonusDef = bonusDef,
            bonusCrit = bonusCrit,
            bonusSpeed = bonusSpeed
        )
    }
}

data class CalculatedStats(
    val hp: Int,
    val mp: Int,
    val physicalAtk: Int,
    val magicAtk: Int,
    val def: Int,
    val critRate: Int,
    val speed: Int,
    val combatPower: Int,
    val bonusHp: Int = 0,
    val bonusMp: Int = 0,
    val bonusAtk: Int = 0,
    val bonusMatk: Int = 0,
    val bonusDef: Int = 0,
    val bonusCrit: Int = 0,
    val bonusSpeed: Int = 0
)

data class UserAccount(
    val username: String,
    val passwordHash: String,
    val activeCharacterId: String? = null
)

enum class MissionCategory {
    LOGIN,
    TRAIN,
    DUNGEON,
    EQUIP,
    CHEST
}

data class DailyMission(
    val id: String,
    val title: String,
    val description: String,
    val targetCount: Int,
    val currentCount: Int,
    val goldReward: Int,
    val expReward: Int,
    val diamondReward: Int = 0,
    val isClaimed: Boolean = false,
    val category: MissionCategory
)
