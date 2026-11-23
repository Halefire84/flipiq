package com.crtc.flipiq.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.crtc.flipiq.model.ConfidenceEngine
import com.crtc.flipiq.model.FlipMath
import com.crtc.flipiq.model.FlipResult

@Composable
fun ScanScreen(
    isPro: Boolean,
    dailyScansLeft: Int,
    onResult: (FlipResult) -> Unit
) {
    var item by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var sellLow by remember { mutableStateOf("") }
    var sellHigh by remember { mutableStateOf("") }
    var feesPct by remember { mutableStateOf("12.9") }  // typical platform fee, edit as needed
    var shipping by remember { mutableStateOf("") }
    var premium by remember { mutableStateOf("0") }
    var compsCount by remember { mutableStateOf("") }
    var showUpgrade by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("FlipIQ", style = MaterialTheme.typography.headlineMedium)

        if (!isPro) {
            Text(
                "$dailyScansLeft free scans left today",
                style = MaterialTheme.typography.labelSmall,
                color = if (dailyScansLeft <= 1) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.secondary
            )
        }

        OutlinedTextField(
            value = item,
            onValueChange = { item = it },
            label = { Text("Item description") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = buyPrice,
                onValueChange = { buyPrice = it },
                label = { Text("Buy price $") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            OutlinedTextField(
                value = feesPct,
                onValueChange = { feesPct = it },
                label = { Text("Fee %") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = sellLow,
                onValueChange = { sellLow = it },
                label = { Text("Sell low $") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            OutlinedTextField(
                value = sellHigh,
                onValueChange = { sellHigh = it },
                label = { Text("Sell high $") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = shipping,
                onValueChange = { shipping = it },
                label = { Text("Shipping $") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            OutlinedTextField(
                value = compsCount,
                onValueChange = { compsCount = it },
                label = { Text("# comps seen") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = premium,
            onValueChange = { premium = it },
            label = { Text("Buyer premium % (auction, 0 if none)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = {
                if (!isPro && dailyScansLeft <= 0) {
                    showUpgrade = true
                    return@Button
                }
                val result = runScan(item, buyPrice, sellLow, sellHigh,
                    feesPct, shipping, premium, compsCount)
                if (result == null) {
                    error = "Check your numbers — all prices must be valid."
                } else {
                    error = null
                    onResult(result)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate")
        }
    }

    if (showUpgrade) {
        AlertDialog(
            onDismissRequest = { showUpgrade = false },
            title = { Text("Free limit reached") },
            text = { Text("You've used your 3 free scans today. Upgrade to FlipIQ Pro for unlimited scans — \$14.99/month.") },
            confirmButton = {
                TextButton(onClick = { showUpgrade = false }) { Text("Upgrade") }
            },
            dismissButton = {
                TextButton(onClick = { showUpgrade = false }) { Text("Later") }
            }
        )
    }
}

private fun runScan(
    item: String, buyStr: String, lowStr: String, highStr: String,
    feesStr: String, shipStr: String, premStr: String, compsStr: String
): FlipResult? {
    val buy = buyStr.toDoubleOrNull() ?: return null
    val low = lowStr.toDoubleOrNull() ?: return null
    val high = highStr.toDoubleOrNull() ?: return null
    val fees = feesStr.toDoubleOrNull() ?: return null
    val ship = shipStr.toDoubleOrNull() ?: 0.0
    val prem = premStr.toDoubleOrNull() ?: 0.0
    val comps = compsStr.toIntOrNull() ?: 5

    if (low > high || buy <= 0 || low <= 0) return null

    val sellMid = (low + high) / 2.0
    val math = FlipMath.calculate(buy, sellMid, fees, ship, prem)
    val confidence = ConfidenceEngine.scoreFromRange(low, high, comps)

    return FlipResult(
        itemDescription = item.ifBlank { "Unnamed item" },
        buyPrice = buy,
        sellPrice = sellMid,
        feesPct = fees,
        shippingCost = ship,
        buyerPremiumPct = prem,
        mathResult = math,
        confidence = confidence
    )
}
