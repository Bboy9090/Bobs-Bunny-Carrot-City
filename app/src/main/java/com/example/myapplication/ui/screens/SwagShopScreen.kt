package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.PlayerProfileState
import com.example.myapplication.model.GameCatalog
import com.example.myapplication.model.SwagCategory

@Composable
fun SwagShopScreen(
    profile: PlayerProfileState,
    onBuyOrEquip: (itemId: String, categoryName: String, cost: Int) -> Boolean,
    onBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(SwagCategory.HEADGEAR) }
    var feedbackMessage by remember {
        mutableStateOf("100% Fair Play: Cosmetics are pure Drip! Wonder Badges give 1 tactical stage modifier.")
    }

    val filteredItems = remember(selectedCategory) {
        GameCatalog.swagItems.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141124))
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "👟 NEW CARROT CITY SWAG SHOP",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
                Text(
                    text = "Carrot Stash: 🥕 ${profile.totalCarrots}   •   Golden Shards: 🌟 ${profile.goldenShards}",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            OutlinedButton(onClick = onBack) {
                Text("Back to Hub", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = feedbackMessage,
            color = Color(0xFFFFCC80),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SwagCategory.entries.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = {
                        Text(
                            text = cat.label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFF6D00),
                        selectedLabelColor = Color.Black,
                        labelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Item Cards
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredItems, key = { it.id }) { item ->
                val isOwned = profile.ownedItemIds.contains(item.id)
                val isEquipped = when (item.category) {
                    SwagCategory.HEADGEAR -> profile.equippedHeadgearId == item.id
                    SwagCategory.HOODIE -> profile.equippedHoodieId == item.id
                    SwagCategory.KICKS -> profile.equippedKicksId == item.id
                    SwagCategory.TRAIL -> profile.equippedTrailId == item.id
                    SwagCategory.BADGE -> profile.equippedBadgeId == item.id
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEquipped) Color(0xFF28224A) else Color(0xFF1E1936)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Color swatch preview
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(item.primaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(item.secondaryColor)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = item.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = item.description,
                                    color = Color(0xFFB0BEC5),
                                    fontSize = 12.sp
                                )
                                if (item.badgePerkDescription.isNotEmpty()) {
                                    Text(
                                        text = "Perk: ${item.badgePerkDescription}",
                                        color = Color(0xFF00E676),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                val ok = onBuyOrEquip(item.id, item.category.name, item.costCarrots)
                                feedbackMessage = if (ok) {
                                    "Equipped ${item.name}! Looking fresh, Bobs! 🔥"
                                } else {
                                    "Need ${item.costCarrots} 🥕 Carrots to unlock ${item.name}!"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    isEquipped -> Color(0xFF00E676)
                                    isOwned -> Color(0xFF00E5FF)
                                    else -> Color(0xFFFF9100)
                                }
                            )
                        ) {
                            Text(
                                text = when {
                                    isEquipped -> "EQUIPPED"
                                    isOwned -> "EQUIP"
                                    else -> "BUY 🥕 ${item.costCarrots}"
                                },
                                color = Color.Black,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
