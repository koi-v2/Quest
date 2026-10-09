package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.BattleLogEntry
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainGameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val activeCharacter by viewModel.activeCharacter.collectAsState()
    val inventory by viewModel.characterInventory.collectAsState()
    val inspectedItem by viewModel.inspectedItem.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    if (activeCharacter == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = GoldPrimary)
        }
        return
    }

    val character = activeCharacter!!
    val equippedItems = remember(inventory) { inventory.filter { it.isEquipped } }
    val calculatedStats = remember(character, equippedItems) { character.calculateStats(equippedItems) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        topBar = {
            CharacterTopAppBar(
                character = character,
                stats = calculatedStats
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("main_navigation_bar"),
                containerColor = DarkSurface,
                contentColor = TextPrimary
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Karakter") },
                    label = { Text("Karakter") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBackground,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_character_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.Backpack, contentDescription = "Inventory") },
                    label = { Text("Inventory") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBackground,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_inventory_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Filled.SportsMma, contentDescription = "Dungeon") },
                    label = { Text("Dungeon") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBackground,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_dungeon_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Akun") },
                    label = { Text("Akun") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBackground,
                        selectedTextColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        indicatorColor = GoldPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_account_tab")
                )
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> CharacterOverviewTab(
                    character = character,
                    stats = calculatedStats,
                    onTrain = { viewModel.trainCharacter() }
                )
                1 -> InventoryTab(
                    character = character,
                    inventory = inventory,
                    equippedItems = equippedItems,
                    onInspectItem = { viewModel.inspectItem(it) },
                    onOpenLootChest = { viewModel.openLootChest() }
                )
                2 -> DungeonArenaTab(
                    character = character,
                    stats = calculatedStats,
                    viewModel = viewModel
                )
                3 -> AccountSettingsTab(
                    character = character,
                    viewModel = viewModel
                )
            }
        }
    }

    // Modal Item Detail
    inspectedItem?.let { item ->
        ItemDetailDialog(
            item = item,
            character = character,
            onDismiss = { viewModel.inspectItem(null) },
            onEquip = { viewModel.equipItem(it) },
            onUnequip = { viewModel.unequipItem(it) },
            onSell = { viewModel.sellItem(it) }
        )
    }
}

@Composable
fun CharacterTopAppBar(
    character: HeroCharacter,
    stats: CalculatedStats
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = DarkSurface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Character Avatar Icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(character.heroClass.themeColor.copy(alpha = 0.25f))
                        .border(2.dp, character.heroClass.glowColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    HeroClassIcon(
                        heroClass = character.heroClass,
                        tint = character.heroClass.glowColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Level
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = character.nickname,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GoldPrimary)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Lv.${character.level}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = DarkBackground,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = character.heroClass.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = character.heroClass.glowColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = " • CP: %,d".format(stats.combatPower),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Currency Display (Gold & Gems)
                CurrencyDisplay(gold = character.gold, diamonds = character.diamonds)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // EXP Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EXP",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(DarkCardBorder)
                ) {
                    val expFraction = (character.currentExp.toFloat() / character.maxExp.toFloat()).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(expFraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(GoldPrimary)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${character.currentExp}/${character.maxExp}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun CharacterOverviewTab(
    character: HeroCharacter,
    stats: CalculatedStats,
    onTrain: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Persona Showcase Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, character.heroClass.glowColor.copy(alpha = 0.7f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(character.heroClass.themeColor.copy(alpha = 0.25f))
                                .border(1.dp, character.heroClass.glowColor, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            HeroClassIcon(
                                heroClass = character.heroClass,
                                tint = character.heroClass.glowColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = character.nickname,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = character.heroClass.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                ClassBadge(heroClass = character.heroClass)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = character.heroClass.mainAdvantage,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = character.heroClass.glowColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Combat Power Emblem
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, GoldPrimary, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "COMBAT POWER",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "%,d".format(stats.combatPower),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        // STATISTIK DASAR YANG TERLIHAT JELAS (Requirement explicit)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STATISTIK DASAR LENGKAP",
                            style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 1.sp),
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Status + Equipment",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatProgressBar(
                            label = "Health Points (HP)",
                            value = stats.hp,
                            maxValue = 2500,
                            color = StatHp,
                            bonusValue = stats.bonusHp
                        )
                        StatProgressBar(
                            label = "Mana Points (MP)",
                            value = stats.mp,
                            maxValue = 2200,
                            color = StatMp,
                            bonusValue = stats.bonusMp
                        )
                        StatProgressBar(
                            label = "Serangan Fisik (Physical ATK)",
                            value = stats.physicalAtk,
                            maxValue = 350,
                            color = StatPatk,
                            bonusValue = stats.bonusAtk
                        )
                        StatProgressBar(
                            label = "Serangan Sihir (Magic ATK)",
                            value = stats.magicAtk,
                            maxValue = 350,
                            color = StatMatk,
                            bonusValue = stats.bonusMatk
                        )
                        StatProgressBar(
                            label = "Pertahanan (Defense)",
                            value = stats.def,
                            maxValue = 300,
                            color = StatDef,
                            bonusValue = stats.bonusDef
                        )
                        StatProgressBar(
                            label = "Peluang Kritis (Crit Rate)",
                            value = stats.critRate,
                            maxValue = 100,
                            color = StatCrit,
                            suffix = "%",
                            bonusValue = stats.bonusCrit
                        )
                        StatProgressBar(
                            label = "Kecepatan (Speed / Agility)",
                            value = stats.speed,
                            maxValue = 200,
                            color = StatSpeed,
                            bonusValue = stats.bonusSpeed
                        )
                    }
                }
            }
        }

        // Active Passive Skill Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, character.heroClass.glowColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.FlashOn,
                            contentDescription = "Pasif",
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pasif Kelas: ${character.heroClass.passiveName}",
                            style = MaterialTheme.typography.titleSmall,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = character.heroClass.passiveDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Train Character Action Button
        item {
            Button(
                onClick = onTrain,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("train_character_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.FitnessCenter,
                    contentDescription = "Latihan",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LATIHAN KARAKTER (+45 EXP, +80 GOLD)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun InventoryTab(
    character: HeroCharacter,
    inventory: List<EquipmentItem>,
    equippedItems: List<EquipmentItem>,
    onInspectItem: (EquipmentItem) -> Unit,
    onOpenLootChest: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf<EquipmentType?>(null) }

    val filteredInventory = remember(inventory, selectedFilter) {
        if (selectedFilter == null) inventory else inventory.filter { it.type == selectedFilter }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Equipment Slots (5 Slots)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "EQUIPMENT TERPASANG",
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp),
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        EquipmentType.values().forEach { type ->
                            val equippedInSlot = equippedItems.firstOrNull { it.type == type }
                            EquippedSlotBox(
                                type = type,
                                item = equippedInSlot,
                                onClick = { equippedInSlot?.let { onInspectItem(it) } }
                            )
                        }
                    }
                }
            }
        }

        // Action: Open Loot Box / Chest
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TAS INVENTARIS (${inventory.size} Item)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Klik item untuk melihat stat, memasang, atau menjual",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = onOpenLootChest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("open_chest_button")
                ) {
                    Icon(imageVector = Icons.Filled.CardGiftcard, contentDescription = "Peti", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Peti (250 G)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Filter chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("Semua") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = DarkBackground,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )

                FilterChip(
                    selected = selectedFilter == EquipmentType.WEAPON,
                    onClick = { selectedFilter = EquipmentType.WEAPON },
                    label = { Text("Senjata") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = DarkBackground,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )

                FilterChip(
                    selected = selectedFilter == EquipmentType.ARMOR,
                    onClick = { selectedFilter = EquipmentType.ARMOR },
                    label = { Text("Zirah") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = DarkBackground,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )

                FilterChip(
                    selected = selectedFilter == EquipmentType.ACCESSORY,
                    onClick = { selectedFilter = EquipmentType.ACCESSORY },
                    label = { Text("Aksesoris") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = DarkBackground,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        // Inventory Item List / Cards
        items(filteredInventory) { item ->
            InventoryItemRow(
                item = item,
                onClick = { onInspectItem(item) }
            )
        }
    }
}

@Composable
fun EquippedSlotBox(
    type: EquipmentType,
    item: EquipmentItem?,
    onClick: () -> Unit
) {
    val borderColor = item?.rarity?.color ?: DarkCardBorder
    val bgColor = if (item != null) item.rarity.color.copy(alpha = 0.15f) else DarkSurfaceElevated

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(58.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                .clickable(enabled = item != null) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (item != null) {
                EquipmentTypeIcon(
                    type = item.type,
                    tint = item.rarity.color,
                    modifier = Modifier.size(26.dp)
                )
            } else {
                EquipmentTypeIcon(
                    type = type,
                    tint = TextMuted.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = type.slotName,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = if (item != null) item.rarity.color else TextMuted,
            fontWeight = if (item != null) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun InventoryItemRow(
    item: EquipmentItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, item.rarity.color.copy(alpha = item.rarity.borderAlpha), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("item_row_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(item.rarity.color.copy(alpha = 0.2f))
                    .border(1.dp, item.rarity.color, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                EquipmentTypeIcon(
                    type = item.type,
                    tint = item.rarity.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.isEquipped) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GoldPrimary)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "DIPAKAI",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = DarkBackground,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RarityBadge(rarity = item.rarity)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.type.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                // Quick stats summary
                val statTexts = mutableListOf<String>()
                if (item.atkBonus > 0) statTexts.add("+${item.atkBonus} ATK")
                if (item.matkBonus > 0) statTexts.add("+${item.matkBonus} MATK")
                if (item.defBonus > 0) statTexts.add("+${item.defBonus} DEF")
                if (item.hpBonus > 0) statTexts.add("+${item.hpBonus} HP")
                if (item.critBonus > 0) statTexts.add("+${item.critBonus}% CRIT")
                if (item.speedBonus > 0) statTexts.add("+${item.speedBonus} SPD")

                if (statTexts.isNotEmpty()) {
                    Text(
                        text = statTexts.joinToString(", "),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF81C784),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Detail",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DungeonArenaTab(
    character: HeroCharacter,
    stats: CalculatedStats,
    viewModel: GameViewModel
) {
    val battleLogs by viewModel.battleLogs.collectAsState()
    val isBattling by viewModel.isBattling.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(WarriorRed.copy(alpha = 0.2f))
                            .border(1.dp, WarriorRed, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SportsMma,
                            contentDescription = "Arena",
                            tint = WarriorRedGlow,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "RUANG BAWAH TANAH KUNO",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Uji statistik & pasif ${character.heroClass.displayName} dalam pertempuran nyata!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { viewModel.startDungeonBattle() },
                    enabled = !isBattling,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("start_battle_button")
                ) {
                    if (isBattling) {
                        CircularProgressIndicator(color = DarkBackground, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sedang Bertarung...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Mulai", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TANTANG MONSTER RUANG BAWAH TANAH", fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "LOG PERTEMPURAN REAL-TIME",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = GoldPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Battle Log Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (battleLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tekan tombol di atas untuk memulai uji coba bertarung.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(battleLogs) { log ->
                        Text(
                            text = log.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (log.isCritical) AssassinCritCrimson else TextPrimary,
                            fontWeight = if (log.isCritical) FontWeight.Bold else FontWeight.Normal,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccountSettingsTab(
    character: HeroCharacter,
    viewModel: GameViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allChars = remember { viewModel.getUserCharacters() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "INFORMASI AKUN PEMAIN",
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp),
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Username: ${currentUser?.username ?: "-"}",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Karakter Aktif: ${character.nickname} (${character.heroClass.displayName})",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Switch or Create New Character
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DAFTAR KARAKTER KAMU",
                            style = MaterialTheme.typography.titleSmall,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(
                            onClick = { viewModel.startNewCharacterFlow() },
                            modifier = Modifier.testTag("create_new_character_button")
                        ) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = "Buat", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buat Baru")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    allChars.forEach { c ->
                        val isActive = c.id == character.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isActive) character.heroClass.themeColor.copy(alpha = 0.2f) else DarkSurfaceElevated)
                                .border(1.dp, if (isActive) character.heroClass.glowColor else DarkCardBorder, RoundedCornerShape(10.dp))
                                .clickable { if (!isActive) viewModel.switchCharacter(c) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HeroClassIcon(heroClass = c.heroClass, tint = c.heroClass.glowColor, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = c.nickname,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${c.heroClass.displayName} • Lv.${c.level}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            if (isActive) {
                                Text(
                                    text = "Aktif",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Logout
        item {
            Button(
                onClick = { viewModel.logout() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkCardBorder,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_button")
            ) {
                Icon(imageVector = Icons.Filled.Logout, contentDescription = "Logout", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("KELUAR DARI AKUN (LOGOUT)", fontWeight = FontWeight.Bold)
            }
        }
    }
}
