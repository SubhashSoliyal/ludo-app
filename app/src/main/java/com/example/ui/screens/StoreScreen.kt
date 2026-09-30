package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CosmeticItemEntity
import com.example.ui.theme.CleanWhite
import com.example.ui.theme.DarkNavyBase
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.viewmodel.MainViewModel

@Composable
fun StoreScreen(
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val cosmetics by mainViewModel.cosmetics.collectAsState()
    val profile by mainViewModel.profile.collectAsState()
    val isStripeOpen by mainViewModel.stripeSheetVisible.collectAsState()
    val stripeStatus by mainViewModel.stripeStatus.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) }
    var stripeProductTitle by remember { mutableStateOf("Royal VIP Subscription") }
    var stripePrice by remember { mutableStateOf("$4.99/mo") }
    var isVipPurchase by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBase)
            .padding(16.dp)
            .testTag("store_screen")
    ) {
        // Header with Balances
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🛒 Royal Store & VIP",
                    color = RoyalGold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Stripe payments & cosmetic inventory",
                    color = TextSecondaryDark,
                    fontSize = 12.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "💰 ${profile?.coins ?: 0}", color = RoyalGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "💎 ${profile?.diamonds ?: 0}", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Store Navigation Tabs
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = DarkSurfaceElevated,
            contentColor = RoyalGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = RoyalGold
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("VIP & Currency", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Dice Skins", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                text = { Text("Board Themes", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = activeTab == 3,
                onClick = { activeTab = 3 },
                text = { Text("Pawns", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (activeTab == 0) {
                // VIP Subscription Card
                item {
                    VipSubscriptionCard(
                        isVip = profile?.isVip == true,
                        onSubscribeClick = {
                            stripeProductTitle = "Royal VIP Pass Subscription"
                            stripePrice = "$4.99/mo"
                            isVipPurchase = true
                            mainViewModel.openStripeCheckout()
                        }
                    )
                }

                item {
                    Text(
                        text = "💳 Coin & Diamond Bundles (Stripe Checkout)",
                        color = RoyalGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Currency bundles
                item {
                    CurrencyBundleCard(
                        title = "Knight's Treasury",
                        coins = "25,000 Coins",
                        gems = "250 Diamonds",
                        price = "$2.99",
                        onClick = {
                            stripeProductTitle = "Knight's Treasury (25,000 Coins + 250 Diamonds)"
                            stripePrice = "$2.99"
                            isVipPurchase = false
                            mainViewModel.openStripeCheckout()
                        }
                    )
                }

                item {
                    CurrencyBundleCard(
                        title = "Emperor's Vault",
                        coins = "100,000 Coins",
                        gems = "1,000 Diamonds",
                        price = "$9.99",
                        isPopular = true,
                        onClick = {
                            stripeProductTitle = "Emperor's Vault (100,000 Coins + 1,000 Diamonds)"
                            stripePrice = "$9.99"
                            isVipPurchase = false
                            mainViewModel.openStripeCheckout()
                        }
                    )
                }

                item {
                    CurrencyBundleCard(
                        title = "Sultan's Hoard",
                        coins = "500,000 Coins",
                        gems = "5,000 Diamonds",
                        price = "$24.99",
                        onClick = {
                            stripeProductTitle = "Sultan's Hoard (500,000 Coins + 5,000 Diamonds)"
                            stripePrice = "$24.99"
                            isVipPurchase = false
                            mainViewModel.openStripeCheckout()
                        }
                    )
                }
            } else {
                // Cosmetics Tab (Dice / Board / Pawn)
                val filteredType = when (activeTab) {
                    1 -> "DICE"
                    2 -> "BOARD"
                    else -> "PAWN"
                }
                val itemsForType = cosmetics.filter { it.itemType == filteredType }

                items(itemsForType) { item ->
                    CosmeticItemCard(
                        item = item,
                        canAfford = (profile?.coins ?: 0) >= item.priceCoins && (profile?.diamonds ?: 0) >= item.priceGems,
                        onUnlock = { mainViewModel.unlockCosmetic(item) },
                        onEquip = { mainViewModel.equipCosmetic(item) }
                    )
                }
            }
        }
    }

    // Stripe Checkout Sheet / Dialog
    if (isStripeOpen) {
        StripeCheckoutDialog(
            productTitle = stripeProductTitle,
            price = stripePrice,
            status = stripeStatus,
            onDismiss = { mainViewModel.closeStripeCheckout() },
            onPay = { cardNumber ->
                mainViewModel.processStripePayment(cardNumber, isVipPurchase)
            }
        )
    }
}

@Composable
fun VipSubscriptionCard(
    isVip: Boolean,
    onSubscribeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF78350F), Color(0xFFD97706), Color(0xFF1E1B4B))
                    )
                )
                .border(2.dp, RoyalGold, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "👑 ROYAL VIP CLUB",
                            color = RoyalGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (isVip) "Status: Active Subscriber" else "Automated Monthly Subscription",
                            color = CleanWhite,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x33000000))
                            .border(1.dp, RoyalGold, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isVip) "ACTIVE" else "$4.99/mo",
                            color = RoyalGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val perks = listOf(
                    "✨ 100% Ad-Free Experience",
                    "💰 2x Coins on Every Win",
                    "🎲 Unlocks Royal 24k Gold Dice",
                    "🏰 Exclusive Royal Palace 3D Board",
                    "🛡️ VIP Golden Border & Chat Badge"
                )

                perks.forEach { perk ->
                    Text(
                        text = perk,
                        color = Color(0xFFFEF3C7),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!isVip) {
                    Button(
                        onClick = onSubscribeClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalGold,
                            contentColor = DarkNavyBase
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subscribe_vip_button")
                    ) {
                        Icon(imageVector = Icons.Default.CreditCard, contentDescription = "Stripe", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Subscribe via Stripe ($4.99/mo)", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF065F46))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👑 VIP Benefits Currently Active",
                            color = CleanWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyBundleCard(
    title: String,
    coins: String,
    gems: String,
    price: String,
    isPopular: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("bundle_$price"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isPopular) 1.5.dp else 1.dp,
                    color = if (isPopular) RoyalGold else Color(0x22FFFFFF),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isPopular) RoyalGold.copy(alpha = 0.2f) else DarkSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (isPopular) "💎" else "💰", fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = title, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (isPopular) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RoyalGold)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("BEST VALUE", color = DarkNavyBase, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    Text(text = "💰 $coins • 💎 $gems", color = TextSecondaryDark, fontSize = 11.sp)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(RoyalGold)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = price, color = DarkNavyBase, fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun CosmeticItemCard(
    item: CosmeticItemEntity,
    canAfford: Boolean,
    onUnlock: () -> Unit,
    onEquip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (item.isEquipped) 1.5.dp else 1.dp,
                    color = if (item.isEquipped) RoyalGold else Color(0x1AFFFFFF),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (item.itemType) {
                            "DICE" -> "🎲"
                            "BOARD" -> "🏰"
                            else -> "♟️"
                        },
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = item.name, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (item.isEquipped) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF10B981))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("EQUIPPED", color = CleanWhite, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(text = item.description, color = TextSecondaryDark, fontSize = 11.sp)
                    if (!item.isUnlocked) {
                        Text(
                            text = if (item.priceGems > 0) "💎 ${item.priceGems} Gems" else "💰 ${item.priceCoins} Coins",
                            color = RoyalGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (item.isEquipped) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Active", tint = RoyalGold)
            } else if (item.isUnlocked) {
                Button(
                    onClick = onEquip,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = TextPrimaryDark),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Equip", fontSize = 11.sp)
                }
            } else {
                Button(
                    onClick = onUnlock,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = DarkNavyBase),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Unlock", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun StripeCheckoutDialog(
    productTitle: String,
    price: String,
    status: String?,
    onDismiss: () -> Unit,
    onPay: (String) -> Unit
) {
    var cardNumber by remember { mutableStateOf("4242 4242 4242 4242") }
    var expiry by remember { mutableStateOf("12/28") }
    var cvc by remember { mutableStateOf("888") }
    var postalCode by remember { mutableStateOf("94016") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stripe Secure Payment",
                    color = CleanWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = "SSL", tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "256-bit SSL", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Product Summary
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceElevated)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = productTitle, color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Text(text = price, color = RoyalGold, fontSize = 15.sp, fontWeight = FontWeight.Black)
                    }
                }

                // Card Number Field
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    label = { Text("Card Number", color = TextSecondaryDark) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stripe_card_number_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedBorderColor = RoyalGold,
                        unfocusedBorderColor = Color(0x44FFFFFF)
                    )
                )

                // Expiry & CVC
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = expiry,
                        onValueChange = { expiry = it },
                        label = { Text("MM/YY", color = TextSecondaryDark) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = RoyalGold,
                            unfocusedBorderColor = Color(0x44FFFFFF)
                        )
                    )
                    OutlinedTextField(
                        value = cvc,
                        onValueChange = { cvc = it },
                        label = { Text("CVC", color = TextSecondaryDark) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = RoyalGold,
                            unfocusedBorderColor = Color(0x44FFFFFF)
                        )
                    )
                }

                if (!status.isNullOrEmpty()) {
                    Text(
                        text = status,
                        color = if (status.contains("Successful")) Color(0xFF34D399) else Color(0xFFF87171),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onPay(cardNumber.replace(" ", "")) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1), contentColor = CleanWhite),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("stripe_pay_button")
            ) {
                Text("Pay $price with Stripe", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        },
        containerColor = DarkSurfaceCard,
        shape = RoundedCornerShape(20.dp)
    )
}
