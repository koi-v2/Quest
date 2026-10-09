package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.EquipmentItem
import com.example.model.HeroCharacter
import com.example.model.Rarity
import com.example.ui.components.ClassBadge
import com.example.ui.components.EquipmentTypeIcon
import com.example.ui.components.RarityBadge
import com.example.ui.theme.*

@Composable
fun ItemDetailDialog(
    item: EquipmentItem,
    character: HeroCharacter,
    onDismiss: () -> Unit,
    onEquip: (EquipmentItem) -> Unit,
    onUnequip: (EquipmentItem) -> Unit,
    onSell: (EquipmentItem) -> Unit
) {
    val canEquipClass = item.requiredClass == null || item.requiredClass == character.heroClass
    val sellPrice = when (item.rarity) {
        Rarity.COMMON -> 150
        Rarity.RARE -> 450
        Rarity.EPIC -> 1200
        Rarity.LEGENDARY -> 3000
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.5.dp, item.rarity.color.copy(alpha = 0.8f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(item.rarity.color.copy(alpha = 0.2f))
                            .border(1.5.dp, item.rarity.color, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        EquipmentTypeIcon(
                            type = item.type,
                            tint = item.rarity.color,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RarityBadge(rarity = item.rarity)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.type.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Tutup",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Class requirement
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Persyaratan Kelas:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    if (item.requiredClass != null) {
                        ClassBadge(heroClass = item.requiredClass)
                    } else {
                        Text(
                            text = "Semua Kelas (Universal)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Boost List
                Text(
                    text = "BONUS STATISTIK",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceElevated)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (item.hpBonus > 0) StatBoostRow(label = "Health Points (HP)", value = "+${item.hpBonus}", color = StatHp)
                    if (item.mpBonus > 0) StatBoostRow(label = "Mana Points (MP)", value = "+${item.mpBonus}", color = StatMp)
                    if (item.atkBonus > 0) StatBoostRow(label = "Serangan Fisik (P.ATK)", value = "+${item.atkBonus}", color = StatPatk)
                    if (item.matkBonus > 0) StatBoostRow(label = "Serangan Sihir (M.ATK)", value = "+${item.matkBonus}", color = StatMatk)
                    if (item.defBonus > 0) StatBoostRow(label = "Pertahanan (DEF)", value = "+${item.defBonus}", color = StatDef)
                    if (item.critBonus > 0) StatBoostRow(label = "Peluang Kritis (CRIT)", value = "+${item.critBonus}%", color = StatCrit)
                    if (item.speedBonus > 0) StatBoostRow(label = "Kecepatan (SPEED)", value = "+${item.speedBonus}", color = StatSpeed)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Description
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Equip / Unequip / Sell
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (item.isEquipped) {
                        Button(
                            onClick = { onUnequip(item) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkCardBorder,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("unequip_item_button")
                        ) {
                            Icon(imageVector = Icons.Filled.RemoveCircleOutline, contentDescription = "Lepas", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lepas")
                        }
                    } else {
                        Button(
                            onClick = { onEquip(item) },
                            enabled = canEquipClass,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = DarkBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("equip_item_button")
                        ) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = "Pasang", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (canEquipClass) "Pasang" else "Beda Kelas", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onSell(item) },
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("sell_item_button")
                        ) {
                            Icon(imageVector = Icons.Filled.MonetizationOn, contentDescription = "Jual", modifier = Modifier.size(16.dp), tint = GoldPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Jual ($sellPrice G)")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatBoostRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
