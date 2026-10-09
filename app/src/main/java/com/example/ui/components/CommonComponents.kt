package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EquipmentType
import com.example.model.HeroClass
import com.example.model.Rarity
import com.example.ui.theme.*

@Composable
fun StatProgressBar(
    label: String,
    value: Int,
    maxValue: Int,
    color: Color,
    modifier: Modifier = Modifier,
    suffix: String = "",
    bonusValue: Int = 0
) {
    val progress = (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "stat_anim"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$value$suffix",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                if (bonusValue > 0) {
                    Text(
                        text = " (+$bonusValue)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF81C784),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(DarkCardBorder.copy(alpha = 0.6f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(color.copy(alpha = 0.7f), color)
                        )
                    )
            )
        }
    }
}

@Composable
fun RarityBadge(rarity: Rarity, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(rarity.color.copy(alpha = 0.18f))
            .border(1.dp, rarity.color.copy(alpha = rarity.borderAlpha), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = rarity.label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = rarity.color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ClassBadge(heroClass: HeroClass, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(heroClass.themeColor.copy(alpha = 0.2f))
            .border(1.dp, heroClass.glowColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = heroClass.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = heroClass.glowColor,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun CurrencyDisplay(
    gold: Int,
    diamonds: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gold
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MonetizationOn,
                contentDescription = "Gold",
                tint = GoldPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "%,d".format(gold),
                style = MaterialTheme.typography.bodySmall,
                color = TextGold,
                fontWeight = FontWeight.Bold
            )
        }

        // Diamonds / Rubies
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, MagePurpleGlow.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Diamond,
                contentDescription = "Ruby",
                tint = MagePurpleGlow,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$diamonds",
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EquipmentTypeIcon(type: EquipmentType, tint: Color = GoldPrimary, modifier: Modifier = Modifier) {
    val icon: ImageVector = when (type) {
        EquipmentType.WEAPON -> Icons.Filled.FlashOn
        EquipmentType.ARMOR -> Icons.Filled.Shield
        EquipmentType.HELMET -> Icons.Filled.Security
        EquipmentType.BOOTS -> Icons.Filled.Speed
        EquipmentType.ACCESSORY -> Icons.Filled.Star
    }
    Icon(
        imageVector = icon,
        contentDescription = type.displayName,
        tint = tint,
        modifier = modifier
    )
}

@Composable
fun HeroClassIcon(heroClass: HeroClass, tint: Color = Color.White, modifier: Modifier = Modifier) {
    val icon: ImageVector = when (heroClass) {
        HeroClass.WARRIOR -> Icons.Filled.Shield
        HeroClass.MAGE -> Icons.Filled.AutoFixHigh
        HeroClass.ARCHER -> Icons.Filled.Adjust
        HeroClass.ASSASSIN -> Icons.Filled.Bolt
    }
    Icon(
        imageVector = icon,
        contentDescription = heroClass.displayName,
        tint = tint,
        modifier = modifier
    )
}
