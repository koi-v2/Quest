package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.HeroClass
import com.example.ui.components.ClassBadge
import com.example.ui.components.HeroClassIcon
import com.example.ui.theme.*

@Composable
fun NicknameDialog(
    selectedClass: HeroClass,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nicknameInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coolNames = remember(selectedClass) {
        when (selectedClass) {
            HeroClass.WARRIOR -> listOf("Ares", "Valerius", "Galahad", "IronClad", "Bartholomew", "Leonidas", "TitanShield")
            HeroClass.MAGE -> listOf("Morpheus", "Arcanist", "Ignis", "Solomon", "Zephyr", "Astraea", "VoidCaller")
            HeroClass.ARCHER -> listOf("Hawkeye", "Sylvana", "Fletcher", "Robin", "WindStrider", "HunterX", "Artemis")
            HeroClass.ASSASSIN -> listOf("ShadowFang", "Nyx", "BloodRaven", "SilentBlade", "Kaelen", "Phantom", "VenomDagger")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.5.dp, selectedClass.glowColor, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Class Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(selectedClass.themeColor.copy(alpha = 0.25f))
                        .border(1.dp, selectedClass.glowColor, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    HeroClassIcon(heroClass = selectedClass, tint = selectedClass.glowColor, modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Beri Nama Karakter",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                ClassBadge(heroClass = selectedClass)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Masukkan nama panggilan (nickname) untuk memulai petualangan di dunia Eldoria.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = nicknameInput,
                    onValueChange = {
                        if (it.length <= 16) {
                            nicknameInput = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Nickname Karakter") },
                    placeholder = { Text("Contoh: ${coolNames.first()}") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Icon",
                            tint = selectedClass.glowColor
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = selectedClass.glowColor,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = selectedClass.glowColor,
                        unfocusedLabelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nickname_input_field")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = WarriorRed
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Randomizer Name Button
                OutlinedButton(
                    onClick = {
                        nicknameInput = coolNames.random()
                        errorMessage = null
                    },
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("random_name_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Casino,
                        contentDescription = "Acak Nama",
                        tint = GoldSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Acak Nama Keren",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cancel_nickname_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            val clean = nicknameInput.trim()
                            if (clean.length < 3) {
                                errorMessage = "Nickname minimal 3 karakter."
                            } else {
                                onConfirm(clean)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_create_character_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = selectedClass.glowColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Konfirmasi",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
