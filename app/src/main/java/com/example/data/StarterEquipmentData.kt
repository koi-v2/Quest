package com.example.data

import com.example.model.EquipmentItem
import com.example.model.EquipmentType
import com.example.model.HeroClass
import com.example.model.Rarity
import java.util.UUID

object StarterEquipmentData {

    fun generateStarterInventoryFor(heroClass: HeroClass): List<EquipmentItem> {
        return when (heroClass) {
            HeroClass.WARRIOR -> listOf(
                // Equipped starter gear
                EquipmentItem(
                    id = "warrior_wpn_1",
                    name = "Broadsword Tempa Besi",
                    type = EquipmentType.WEAPON,
                    requiredClass = HeroClass.WARRIOR,
                    rarity = Rarity.COMMON,
                    isEquipped = true,
                    atkBonus = 35,
                    defBonus = 15,
                    hpBonus = 80,
                    description = "Pedang baja tempa khas prajurit garis depan. Berat dan berbobot.",
                    iconKey = "sword"
                ),
                EquipmentItem(
                    id = "warrior_arm_1",
                    name = "Plat Zirah Ksatria Sentinel",
                    type = EquipmentType.ARMOR,
                    requiredClass = HeroClass.WARRIOR,
                    rarity = Rarity.RARE,
                    isEquipped = true,
                    defBonus = 65,
                    hpBonus = 220,
                    description = "Lapisan baja tebal yang mampu menangkis tebasan pedang musuh.",
                    iconKey = "shield"
                ),
                EquipmentItem(
                    id = "warrior_hlm_1",
                    name = "Helm Bertanduk Besi Hitam",
                    type = EquipmentType.HELMET,
                    requiredClass = HeroClass.WARRIOR,
                    rarity = Rarity.COMMON,
                    isEquipped = true,
                    defBonus = 25,
                    hpBonus = 90,
                    description = "Pelindung kepala kokoh dengan visor baja tempa.",
                    iconKey = "helmet"
                ),
                EquipmentItem(
                    id = "warrior_acc_1",
                    name = "Perisai Baja Pelindung Aegis",
                    type = EquipmentType.ACCESSORY,
                    requiredClass = HeroClass.WARRIOR,
                    rarity = Rarity.RARE,
                    isEquipped = true,
                    defBonus = 50,
                    hpBonus = 180,
                    critBonus = 3,
                    description = "Perisai menara yang menyerap benturan keras dari musuh.",
                    iconKey = "accessory"
                ),
                EquipmentItem(
                    id = "warrior_bts_1",
                    name = "Sepatu Bot Tempur Greaves",
                    type = EquipmentType.BOOTS,
                    requiredClass = HeroClass.WARRIOR,
                    rarity = Rarity.COMMON,
                    isEquipped = true,
                    defBonus = 20,
                    speedBonus = 12,
                    hpBonus = 60,
                    description = "Sepatu beralas logam berat untuk kuda-kuda kokoh tak tergoyahkan.",
                    iconKey = "boots"
                ),
                // Extra unequipped items in inventory for Warrior
                EquipmentItem(
                    id = "warrior_wpn_2",
                    name = "Pedang Api Naga (Dragonblade)",
                    type = EquipmentType.WEAPON,
                    requiredClass = HeroClass.WARRIOR,
                    rarity = Rarity.EPIC,
                    isEquipped = false,
                    atkBonus = 85,
                    defBonus = 30,
                    hpBonus = 180,
                    critBonus = 8,
                    description = "Bilah pedang merah berukir rune naga yang membara saat diayunkan.",
                    iconKey = "sword"
                ),
                EquipmentItem(
                    id = "warrior_acc_2",
                    name = "Kalung Berkat Titan",
                    type = EquipmentType.ACCESSORY,
                    requiredClass = null, // Universal
                    rarity = Rarity.LEGENDARY,
                    isEquipped = false,
                    hpBonus = 400,
                    defBonus = 70,
                    mpBonus = 100,
                    description = "Jimat kuno dari para raksasa yang melipatgandakan daya tahan tubuh.",
                    iconKey = "ring"
                ),
                EquipmentItem(
                    id = "warrior_arm_2",
                    name = "Zirah Obsidian Dreadnought",
                    type = EquipmentType.ARMOR,
                    requiredClass = HeroClass.WARRIOR,
                    rarity = Rarity.LEGENDARY,
                    isEquipped = false,
                    defBonus = 120,
                    hpBonus = 500,
                    atkBonus = 20,
                    description = "Armor legendaris dari batu vulkanik hitam yang tak tertembus sihir sekalipun.",
                    iconKey = "shield"
                )
            )

            HeroClass.MAGE -> listOf(
                // Equipped starter gear
                EquipmentItem(
                    id = "mage_wpn_1",
                    name = "Tongkat Kristal Arcane",
                    type = EquipmentType.WEAPON,
                    requiredClass = HeroClass.MAGE,
                    rarity = Rarity.COMMON,
                    isEquipped = true,
                    matkBonus = 55,
                    mpBonus = 180,
                    description = "Tongkat sihir berujung batu safir pengalir energi kosmik.",
                    iconKey = "wand"
                ),
                EquipmentItem(
                    id = "mage_arm_1",
                    name = "Jubah Sutra Astromancer",
                    type = EquipmentType.ARMOR,
                    requiredClass = HeroClass.MAGE,
                    rarity = Rarity.RARE,
                    isEquipped = true,
                    matkBonus = 35,
                    defBonus = 25,
                    mpBonus = 260,
                    description = "Jubah bermotif bintang yang mempercepat sirkulasi mana pengguna.",
                    iconKey = "shield"
                ),
                EquipmentItem(
                    id = "mage_hlm_1",
                    name = "Tudung Pengembara Mistis",
                    type = EquipmentType.HELMET,
                    requiredClass = HeroClass.MAGE,
                    rarity = Rarity.COMMON,
                    isEquipped = true,
                    matkBonus = 20,
                    defBonus = 15,
                    mpBonus = 140,
                    description = "Tudung berbahan serat bayangan pemfokus konsentrasi mantra.",
                    iconKey = "helmet"
                ),
                EquipmentItem(
                    id = "mage_acc_1",
                    name = "Orb Bola Kristal Void",
                    type = EquipmentType.ACCESSORY,
                    requiredClass = HeroClass.MAGE,
                    rarity = Rarity.RARE,
                    isEquipped = true,
                    matkBonus = 45,
                    mpBonus = 200,
                    critBonus = 5,
                    description = "Bola kristal melayang yang berdenyut dengan getaran magis.",
                    iconKey = "accessory"
                ),
                EquipmentItem(
                    id = "mage_bts_1",
                    name = "Sandal Berkah Angin",
                    type = EquipmentType.BOOTS,
                    requiredClass = HeroClass.MAGE,
                    rarity = Rarity.COMMON,
                    isEquipped = true,
                    defBonus = 12,
                    speedBonus = 18,
                    mpBonus = 90,
                    description = "Alas kaki ringan yang membuat langkah pemakainya seringan hembusan angin.",
                    iconKey = "boots"
                ),
                // Extra unequipped items in inventory for Mage
                EquipmentItem(
                    id = "mage_wpn_2",
                    name = "Staf Petir Archmage Chronos",
                    type = EquipmentType.WEAPON,
                    requiredClass = HeroClass.MAGE,
                    rarity = Rarity.EPIC,
                    isEquipped = false,
                    matkBonus = 110,
                    mpBonus = 380,
                    critBonus = 8,
                    description = "Tongkat legendaris tempat roh petir bersemayam. Menghancurkan musuh dalam sekejap.",
                    iconKey = "wand"
                ),
                EquipmentItem(
                    id = "mage_acc_2",
                    name = "Cincin Batu Filsuf (Philosopher)",
                    type = EquipmentType.ACCESSORY,
                    requiredClass = null,
                    rarity = Rarity.LEGENDARY,
                    isEquipped = false,
                    matkBonus = 75,
                    mpBonus = 500,
                    hpBonus = 150,
                    critBonus = 10,
                    description = "Artefak pusaka yang menyuplai energi mana tanpa batas.",
                    iconKey = "ring"
                ),
                EquipmentItem(
                    id = "mage_arm_2",
                    name = "Jubah Ratu Kegelapan (Void Empress)",
                    type = EquipmentType.ARMOR,
                    requiredClass = HeroClass.MAGE,
                    rarity = Rarity.LEGENDARY,
                    isEquipped = false,
                    matkBonus = 95,
                    defBonus = 45,
                    mpBonus = 420,
                    critBonus = 12,
                    description = "Kain mistis dari dimensi antariksa, memancarkan aura ungu memikat.",
                    iconKey = "shield"
                )
            )

            HeroClass.ARCHER -> listOf(
                // Equipped starter gear
                EquipmentItem(
                    id = "archer_wpn_1",
                    name = "Busur Komposit Kayu Yew",
                    type = EquipmentType.WEAPON,
                    requiredClass = HeroClass.ARCHER,
                    rarity = Rarity.COMMON,
                    isEquipped = true,
                    atkBonus = 50,
                    speedBonus = 18,
                    critBonus = 6,
                    description = "Busur lengkung fleksibel dengan tarikan senar yang tajam dan presisi.",
                    iconKey = "bow"
                ),
                EquipmentItem(
                    id = "archer_arm_1",
                    name = "Rompi Kulit Siluman Rusa",
                    type = EquipmentType.ARMOR,
                    requiredClass = HeroClass.ARCHER,
                    rarity = Rarity.COMMON,
                    isEquipped = true,
                    defBonus = 35,
                    hpBonus = 140,
                    speedBonus = 12,
                    description = "Zirah kulit lentur berpenyamaran dedaunan hutan belantara.",
                    iconKey = "shield"
                ),
                EquipmentItem(
                    id = "archer_hlm_1",
                    name = "Topi Pemburu Mata Rajawali",
                    type = EquipmentType.HELMET,
                    requiredClass = HeroClass.ARCHER,
                    rarity = Rarity.RARE,
                    isEquipped = true,
                    atkBonus = 25,
                    defBonus = 20,
                    critBonus = 7,
                    description = "Topi berbulu elang yang mempertajam pandangan menembak sasaran.",
                    iconKey = "helmet"
                ),
                EquipmentItem(
                    id = "archer_acc_1",
                    name = "Quiver Tabung Panah Berbisa",
                    type = EquipmentType.ACCESSORY,
                    requiredClass = HeroClass.ARCHER,
                    rarity = Rarity.RARE,
                    isEquipped = true,
                    atkBonus = 35,
                    critBonus = 8,
                    speedBonus = 15,
                    description = "Tempat anak panah dengan racun tanaman hutan yang mematikan.",
                    iconKey = "accessory"
                ),
                EquipmentItem(
                    id = "archer_bts_1",
                    name = "Sepatu Langkah Hutan (Strider)",
                    type = EquipmentType.BOOTS,
                    requiredClass = HeroClass.ARCHER,
                    rarity = Rarity.COMMON,
                    isEquipped = true,
                    defBonus = 15,
                    speedBonus = 28,
                    description = "Sepatu bersol karet getah yang melangkah tanpa meninggalkan jejak suara.",
                    iconKey = "boots"
                ),
                // Extra unequipped items in inventory for Archer
                EquipmentItem(
                    id = "archer_wpn_2",
                    name = "Busur Badai Halilintar (Storm Bow)",
                    type = EquipmentType.WEAPON,
                    requiredClass = HeroClass.ARCHER,
                    rarity = Rarity.EPIC,
                    isEquipped = false,
                    atkBonus = 105,
                    speedBonus = 30,
                    critBonus = 14,
                    description = "Busur suci yang meluncurkan anak panah secepat sambaran petir badai.",
                    iconKey = "bow"
                ),
                EquipmentItem(
                    id = "archer_acc_2",
                    name = "Teropong Bintang Penembak Jitu",
                    type = EquipmentType.ACCESSORY,
                    requiredClass = null,
                    rarity = Rarity.LEGENDARY,
                    isEquipped = false,
                    atkBonus = 65,
                    critBonus = 16,
                    speedBonus = 25,
                    hpBonus = 180,
                    description = "Lensa magis yang mengunci titik kelemahan musuh dari ratusan meter.",
                    iconKey = "ring"
                ),
                EquipmentItem(
                    id = "archer_arm_2",
                    name = "Zirah Sisik Naga Angin",
                    type = EquipmentType.ARMOR,
                    requiredClass = HeroClass.ARCHER,
                    rarity = Rarity.LEGENDARY,
                    isEquipped = false,
                    defBonus = 70,
                    atkBonus = 45,
                    speedBonus = 35,
                    critBonus = 10,
                    hpBonus = 320,
                    description = "Ditempa dari sisik naga badai, sangat ringan dan memotong hambatan udara.",
                    iconKey = "shield"
                )
            )

            HeroClass.ASSASSIN -> listOf(
                // Equipped starter gear
                EquipmentItem(
                    id = "assassin_wpn_1",
                    name = "Belati Kembar Obsidian Hitam",
                    type = EquipmentType.WEAPON,
                    requiredClass = HeroClass.ASSASSIN,
                    rarity = Rarity.COMMON,
                    isEquipped = true,
                    atkBonus = 42,
                    critBonus = 14,
                    speedBonus = 25,
                    description = "Sepasang belati tajam bermata ganda yang haus akan titik fatal lawan.",
                    iconKey = "dagger"
                ),
                EquipmentItem(
                    id = "assassin_arm_1",
                    name = "Jubah Malam Penyamaran",
                    type = EquipmentType.ARMOR,
                    requiredClass = HeroClass.ASSASSIN,
                    rarity = Rarity.RARE,
                    isEquipped = true,
                    defBonus = 28,
                    critBonus = 10,
                    speedBonus = 20,
                    hpBonus = 120,
                    description = "Kain hitam pekat penyerap cahaya yang menyatu sempurna dalam bayangan.",
                    iconKey = "shield"
                ),
                EquipmentItem(
                    id = "assassin_hlm_1",
                    name = "Masker Kain Siluman Bayangan",
                    type = EquipmentType.HELMET,
                    requiredClass = HeroClass.ASSASSIN,
                    rarity = Rarity.COMMON,
                    isEquipped = true,
                    defBonus = 16,
                    critBonus = 8,
                    speedBonus = 15,
                    description = "Menutup identitas dan menajamkan insting berburu di kegelapan.",
                    iconKey = "helmet"
                ),
                EquipmentItem(
                    id = "assassin_acc_1",
                    name = "Gelang Cakar Racun Viper",
                    type = EquipmentType.ACCESSORY,
                    requiredClass = HeroClass.ASSASSIN,
                    rarity = Rarity.RARE,
                    isEquipped = true,
                    atkBonus = 30,
                    critBonus = 15,
                    speedBonus = 18,
                    description = "Cakar tersembunyi yang dilapisi bisa racun ular berdaya kejut tinggi.",
                    iconKey = "accessory"
                ),
                EquipmentItem(
                    id = "assassin_bts_1",
                    name = "Sepatu Langkah Hening Gaib",
                    type = EquipmentType.BOOTS,
                    requiredClass = HeroClass.ASSASSIN,
                    rarity = Rarity.COMMON,
                    isEquipped = true,
                    defBonus = 14,
                    speedBonus = 35,
                    critBonus = 5,
                    description = "Sepatu khusus yang meredam suara detak langkah menjadi nol desibel.",
                    iconKey = "boots"
                ),
                // Extra unequipped items in inventory for Assassin
                EquipmentItem(
                    id = "assassin_wpn_2",
                    name = "Belati Bulan Berdarah (Blood Moon)",
                    type = EquipmentType.WEAPON,
                    requiredClass = HeroClass.ASSASSIN,
                    rarity = Rarity.LEGENDARY,
                    isEquipped = false,
                    atkBonus = 95,
                    critBonus = 28, // High critical rate
                    speedBonus = 45,
                    hpBonus = 150,
                    description = "Senjata pembunuh legendaris yang bersinar merah darah saat mencium luka musuh.",
                    iconKey = "dagger"
                ),
                EquipmentItem(
                    id = "assassin_acc_2",
                    name = "Cincin Hati Pembunuh Gelap",
                    type = EquipmentType.ACCESSORY,
                    requiredClass = null,
                    rarity = Rarity.EPIC,
                    isEquipped = false,
                    atkBonus = 45,
                    critBonus = 18,
                    speedBonus = 22,
                    description = "Cincin berukir tengkorak yang memperbesar daya ledak serangan fatal.",
                    iconKey = "ring"
                ),
                EquipmentItem(
                    id = "assassin_arm_2",
                    name = "Kostum Siluman Phantom Reaver",
                    type = EquipmentType.ARMOR,
                    requiredClass = HeroClass.ASSASSIN,
                    rarity = Rarity.LEGENDARY,
                    isEquipped = false,
                    defBonus = 55,
                    atkBonus = 40,
                    critBonus = 20,
                    speedBonus = 40,
                    hpBonus = 260,
                    description = "Ditenun dari benang ketiadaan, membuat pemakainya sulit dideteksi mata biasa.",
                    iconKey = "shield"
                )
            )
        }
    }

    // Generator for mystery loot chest drop based on class
    fun generateRandomLootFor(heroClass: HeroClass): EquipmentItem {
        val randomId = "loot_" + UUID.randomUUID().toString().take(8)
        val rarities = listOf(Rarity.RARE, Rarity.EPIC, Rarity.LEGENDARY)
        val chosenRarity = rarities.random()

        return when (heroClass) {
            HeroClass.WARRIOR -> {
                when ((1..3).random()) {
                    1 -> EquipmentItem(
                        id = randomId,
                        name = "Kapak Perang Jagal Raksasa",
                        type = EquipmentType.WEAPON,
                        requiredClass = HeroClass.WARRIOR,
                        rarity = chosenRarity,
                        atkBonus = if (chosenRarity == Rarity.LEGENDARY) 110 else 75,
                        defBonus = 35,
                        hpBonus = 200,
                        description = "Kapak bergagang panjang dengan hantaman yang mampu meremukkan perisai lawan.",
                        iconKey = "sword"
                    )
                    2 -> EquipmentItem(
                        id = randomId,
                        name = "Perisai Cermin Paladin Emas",
                        type = EquipmentType.ACCESSORY,
                        requiredClass = HeroClass.WARRIOR,
                        rarity = chosenRarity,
                        defBonus = if (chosenRarity == Rarity.LEGENDARY) 95 else 60,
                        hpBonus = 350,
                        critBonus = 5,
                        description = "Perisai bercahaya ilahi yang memantulkan serangan proyektil.",
                        iconKey = "shield"
                    )
                    else -> EquipmentItem(
                        id = randomId,
                        name = "Zirah Besi Naga Merah",
                        type = EquipmentType.ARMOR,
                        requiredClass = HeroClass.WARRIOR,
                        rarity = chosenRarity,
                        defBonus = if (chosenRarity == Rarity.LEGENDARY) 100 else 70,
                        hpBonus = 420,
                        description = "Armor ksatria bertatahkan permata rubi menyala.",
                        iconKey = "shield"
                    )
                }
            }
            HeroClass.MAGE -> {
                when ((1..3).random()) {
                    1 -> EquipmentItem(
                        id = randomId,
                        name = "Staf Nebula Kosmik",
                        type = EquipmentType.WEAPON,
                        requiredClass = HeroClass.MAGE,
                        rarity = chosenRarity,
                        matkBonus = if (chosenRarity == Rarity.LEGENDARY) 130 else 85,
                        mpBonus = 450,
                        critBonus = 8,
                        description = "Staf berkepala galaksi mini yang menyalurkan energi lubang hitam.",
                        iconKey = "wand"
                    )
                    2 -> EquipmentItem(
                        id = randomId,
                        name = "Amulet Roh Phoenix",
                        type = EquipmentType.ACCESSORY,
                        requiredClass = HeroClass.MAGE,
                        rarity = chosenRarity,
                        matkBonus = if (chosenRarity == Rarity.LEGENDARY) 70 else 45,
                        mpBonus = 320,
                        hpBonus = 200,
                        description = "Amulet yang menghangatkan jiwa dan melipatgandakan kobaran api sihir.",
                        iconKey = "accessory"
                    )
                    else -> EquipmentItem(
                        id = randomId,
                        name = "Jubah Penyihir Bintang Utara",
                        type = EquipmentType.ARMOR,
                        requiredClass = HeroClass.MAGE,
                        rarity = chosenRarity,
                        matkBonus = 60,
                        defBonus = 40,
                        mpBonus = 380,
                        description = "Jubah biru es yang memancarkan serbuk bintang di setiap kibasannya.",
                        iconKey = "shield"
                    )
                }
            }
            HeroClass.ARCHER -> {
                when ((1..3).random()) {
                    1 -> EquipmentItem(
                        id = randomId,
                        name = "Busur Sayap Garuda",
                        type = EquipmentType.WEAPON,
                        requiredClass = HeroClass.ARCHER,
                        rarity = chosenRarity,
                        atkBonus = if (chosenRarity == Rarity.LEGENDARY) 120 else 80,
                        speedBonus = 25,
                        critBonus = 12,
                        description = "Busur berbahan tulang burung mitologi yang menembak dengan kecepatan suara.",
                        iconKey = "bow"
                    )
                    2 -> EquipmentItem(
                        id = randomId,
                        name = "Cincin Angin Topan",
                        type = EquipmentType.ACCESSORY,
                        requiredClass = HeroClass.ARCHER,
                        rarity = chosenRarity,
                        atkBonus = 45,
                        speedBonus = 30,
                        critBonus = 10,
                        description = "Cincin berpusar angin yang meringankan bidikan panah.",
                        iconKey = "ring"
                    )
                    else -> EquipmentItem(
                        id = randomId,
                        name = "Topi Ranger Hutan Keramat",
                        type = EquipmentType.HELMET,
                        requiredClass = HeroClass.ARCHER,
                        rarity = chosenRarity,
                        atkBonus = 35,
                        defBonus = 30,
                        critBonus = 8,
                        description = "Pelindung kepala hijau tua yang menyatu dengan dedaunan kanopi.",
                        iconKey = "helmet"
                    )
                }
            }
            HeroClass.ASSASSIN -> {
                when ((1..3).random()) {
                    1 -> EquipmentItem(
                        id = randomId,
                        name = "Belati Cakar Harimau Kumbang",
                        type = EquipmentType.WEAPON,
                        requiredClass = HeroClass.ASSASSIN,
                        rarity = chosenRarity,
                        atkBonus = if (chosenRarity == Rarity.LEGENDARY) 100 else 70,
                        critBonus = if (chosenRarity == Rarity.LEGENDARY) 30 else 20,
                        speedBonus = 40,
                        description = "Bilah taring hewan purba yang merobek armor dalam satu tebasan kilat.",
                        iconKey = "dagger"
                    )
                    2 -> EquipmentItem(
                        id = randomId,
                        name = "Jimat Bayangan Hantu (Wraith)",
                        type = EquipmentType.ACCESSORY,
                        requiredClass = HeroClass.ASSASSIN,
                        rarity = chosenRarity,
                        atkBonus = 40,
                        critBonus = 18,
                        speedBonus = 28,
                        description = "Jimat pemanggil kabut yang meningkatkan frekuensi serangan kritikal.",
                        iconKey = "accessory"
                    )
                    else -> EquipmentItem(
                        id = randomId,
                        name = "Sepatu Langkah Bayangan Kosmik",
                        type = EquipmentType.BOOTS,
                        requiredClass = HeroClass.ASSASSIN,
                        rarity = chosenRarity,
                        defBonus = 25,
                        speedBonus = 45,
                        critBonus = 8,
                        description = "Sepatu hitam pekat yang membuat pemakainya melompat sekedipan mata.",
                        iconKey = "boots"
                    )
                }
            }
        }
    }

    fun generateDefaultDailyMissions(): List<com.example.model.DailyMission> {
        return listOf(
            com.example.model.DailyMission(
                id = "mission_login",
                title = "Absensi Petualang (Daily Check-in)",
                description = "Masuk ke dalam game untuk memulai petualangan hari ini.",
                targetCount = 1,
                currentCount = 1,
                goldReward = 150,
                expReward = 60,
                isClaimed = false,
                category = com.example.model.MissionCategory.LOGIN
            ),
            com.example.model.DailyMission(
                id = "mission_train",
                title = "Latihan Rutin Karakter",
                description = "Lakukan latihan stat karakter minimal 2 kali.",
                targetCount = 2,
                currentCount = 0,
                goldReward = 200,
                expReward = 90,
                isClaimed = false,
                category = com.example.model.MissionCategory.TRAIN
            ),
            com.example.model.DailyMission(
                id = "mission_dungeon",
                title = "Penakluk Ruang Bawah Tanah",
                description = "Tantang dan taklukkan monster di Ruang Bawah Tanah Kuno 1 kali.",
                targetCount = 1,
                currentCount = 0,
                goldReward = 280,
                expReward = 120,
                isClaimed = false,
                category = com.example.model.MissionCategory.DUNGEON
            ),
            com.example.model.DailyMission(
                id = "mission_equip",
                title = "Ahli Perlengkapan Tempur",
                description = "Pasang atau ganti salah satu equipment senjata/zirah di tas inventaris.",
                targetCount = 1,
                currentCount = 0,
                goldReward = 140,
                expReward = 60,
                isClaimed = false,
                category = com.example.model.MissionCategory.EQUIP
            ),
            com.example.model.DailyMission(
                id = "mission_chest",
                title = "Pemburu Harta Karun",
                description = "Buka 1 Peti Misteri di inventaris untuk mencari perlengkapan langka.",
                targetCount = 1,
                currentCount = 0,
                goldReward = 350,
                expReward = 150,
                diamondReward = 5,
                isClaimed = false,
                category = com.example.model.MissionCategory.CHEST
            )
        )
    }
}

