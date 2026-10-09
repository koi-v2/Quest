package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StarterEquipmentData
import com.example.model.HeroClass
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel

@Composable
fun ClassSelectionScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val selectedClass by viewModel.selectedClass.collectAsState()
    val showNicknameDialog by viewModel.showNicknameDialog.collectAsState()
    val scrollState = rememberScrollState()

    val starterItems = remember(selectedClass) {
        StarterEquipmentData.generateStarterInventoryFor(selectedClass).filter { it.isEquipped }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PILIH TAKDIR KELAS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = GoldPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tentukan gaya bermainmu. Setiap class memiliki kelebihan, pasif, dan status awal tersendiri.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // Class Selection Carousel / Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(HeroClass.values()) { heroClass ->
                    val isSelected = heroClass == selectedClass
                    ClassSelectorCard(
                        heroClass = heroClass,
                        isSelected = isSelected,
                        onClick = { viewModel.selectClass(heroClass) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detailed Class Profile Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.5.dp, selectedClass.glowColor.copy(alpha = 0.8f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Top Hero Banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(selectedClass.themeColor.copy(alpha = 0.25f))
                                .border(2.dp, selectedClass.glowColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            HeroClassIcon(
                                heroClass = selectedClass,
                                tint = selectedClass.glowColor,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedClass.displayName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                ClassBadge(heroClass = selectedClass)
                            }
                            Text(
                                text = selectedClass.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextGold,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = selectedClass.role,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Highlighted Main Advantage Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(selectedClass.themeColor.copy(alpha = 0.15f))
                            .border(1.dp, selectedClass.glowColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Star",
                                tint = selectedClass.glowColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "KELEBIHAN UTAMA",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = selectedClass.mainAdvantage,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = selectedClass.glowColor,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Passive Skill Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.FlashOn,
                                    contentDescription = "Passive",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pasif: ${selectedClass.passiveName}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = selectedClass.passiveDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description of playstyle
                    Text(
                        text = selectedClass.playstyleDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Initial Base Stats Display
                    Text(
                        text = "STATUS AWAL KARAKTER",
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp),
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatProgressBar(
                            label = "Health Points (HP)",
                            value = selectedClass.baseHp,
                            maxValue = 1500,
                            color = StatHp
                        )
                        StatProgressBar(
                            label = "Mana Points (MP)",
                            value = selectedClass.baseMp,
                            maxValue = 1300,
                            color = StatMp
                        )
                        StatProgressBar(
                            label = "Serangan Fisik (Physical ATK)",
                            value = selectedClass.basePhysicalAtk,
                            maxValue = 180,
                            color = StatPatk
                        )
                        StatProgressBar(
                            label = "Serangan Sihir (Magic ATK)",
                            value = selectedClass.baseMagicAtk,
                            maxValue = 180,
                            color = StatMatk
                        )
                        StatProgressBar(
                            label = "Pertahanan (Defense)",
                            value = selectedClass.baseDef,
                            maxValue = 150,
                            color = StatDef
                        )
                        StatProgressBar(
                            label = "Peluang Kritis (Crit Rate)",
                            value = selectedClass.baseCritRate,
                            maxValue = 50,
                            color = StatCrit,
                            suffix = "%"
                        )
                        StatProgressBar(
                            label = "Kecepatan Gerak (Speed / AGI)",
                            value = selectedClass.baseSpeed,
                            maxValue = 120,
                            color = StatSpeed
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Starter Gear Preview
                    Text(
                        text = "PERALATAN AWAL DI TAS INVENTARIS",
                        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp),
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        starterItems.take(3).forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, item.rarity.color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                EquipmentTypeIcon(
                                    type = item.type,
                                    tint = item.rarity.color,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = item.type.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                                RarityBadge(rarity = item.rarity)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Primary Action Button
                    Button(
                        onClick = { viewModel.openNicknameDialog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = selectedClass.glowColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("choose_class_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Pilih",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PILIH ${selectedClass.displayName.uppercase()} & BERI NAMA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // Nickname Dialog Prompt
        if (showNicknameDialog) {
            NicknameDialog(
                selectedClass = selectedClass,
                onDismiss = { viewModel.closeNicknameDialog() },
                onConfirm = { nickname ->
                    viewModel.confirmNicknameAndCreate(nickname)
                }
            )
        }
    }
}

@Composable
fun ClassSelectorCard(
    heroClass: HeroClass,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) heroClass.glowColor else DarkCardBorder
    val bgColor = if (isSelected) heroClass.themeColor.copy(alpha = 0.25f) else DarkSurface

    Card(
        modifier = Modifier
            .width(150.dp)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("class_card_${heroClass.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(heroClass.themeColor.copy(alpha = 0.3f))
                    .border(1.dp, heroClass.glowColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                HeroClassIcon(
                    heroClass = heroClass,
                    tint = heroClass.glowColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = heroClass.displayName,
                style = MaterialTheme.typography.titleSmall,
                color = if (isSelected) heroClass.glowColor else TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = heroClass.mainAdvantage,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (isSelected) TextGold else TextSecondary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}
