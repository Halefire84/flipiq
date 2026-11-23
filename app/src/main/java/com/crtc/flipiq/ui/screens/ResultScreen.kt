package com.crtc.flipiq.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crtc.flipiq.model.FlipMath
import com.crtc.flipiq.model.FlipResult

@Composable
fun ResultScreen(result: FlipResult, onBack: () -> Unit, onSave: (FlipResult) -> Unit) {
    val verdictBg = Color(result.verdictColor)

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
            Text(result.itemDescription, style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f))
        }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Verdict card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(verdictBg)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        result.displayVerdict,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (result.mathResult.roi != null) {
                        Text(
                            "${"%.0f".format(result.mathResult.roi)}% ROI · ${"$%.2f".format(result.mathResult.profit)} profit",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Confidence card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()) {
                        Text("Market Confidence", style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold)
                        Text("${result.confidence.score}/100",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = confidenceColor(result.confidence.score))
                    }
                    LinearProgressIndicator(
                        progress = { result.confidence.score / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = confidenceColor(result.confidence.score)
                    )
                    Text(result.confidence.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (result.confidence.priceMid > 0) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()) {
                            PriceChip("Low", result.confidence.priceLow)
                            PriceChip("Mid", result.confidence.priceMid)
                            PriceChip("High", result.confidence.priceHigh)
                        }
                    }
                }
            }

            // Math breakdown card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Profit Math", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    MathRow("Buy price", result.buyPrice)
                    if (result.buyerPremiumPct > 0)
                        MathRow("+ Buyer premium (${result.buyerPremiumPct}%)",
                            result.mathResult.allIn - result.buyPrice)
                    MathRow("All-in cost", result.mathResult.allIn, bold = true)
                    Divider()
                    MathRow("Sell price", result.sellPrice)
                    MathRow("− Platform fees (${result.feesPct}%)",
                        -(result.sellPrice * result.feesPct / 100))
                    if (result.shippingCost > 0)
                        MathRow("− Shipping out", -result.shippingCost)
                    MathRow("Net proceeds", result.mathResult.net, bold = true)
                    Divider()
                    MathRow("Profit", result.mathResult.profit,
                        bold = true,
                        color = if (result.mathResult.profit >= 0) Color(0xFF2E7D32) else Color(0xFFB71C1C))
                    result.mathResult.roi?.let {
                        MathRow("ROI", it, isPct = true, bold = true,
                            color = if (it >= 0) Color(0xFF2E7D32) else Color(0xFFB71C1C))
                    }
                    if (result.mathResult.maxBuy > 0 && result.mathResult.verdict != FlipMath.Verdict.BUY) {
                        Divider()
                        MathRow("Max you should pay", result.mathResult.maxBuy,
                            color = Color(0xFFE65100))
                    }
                }
            }

            Button(onClick = { onSave(result) }, modifier = Modifier.fillMaxWidth()) {
                Text("Save to History")
            }
        }
    }
}

@Composable
private fun PriceChip(label: String, value: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text("${"$%.0f".format(value)}", style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MathRow(label: String, value: Double, bold: Boolean = false,
                    isPct: Boolean = false, color: Color = Color.Unspecified) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label,
            style = if (bold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
            color = color)
        Text(
            if (isPct) "${"%.1f".format(value)}%" else "${"$%.2f".format(value)}",
            style = if (bold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
            color = color
        )
    }
}

private fun confidenceColor(score: Int): Color = when {
    score >= 70 -> Color(0xFF2E7D32)
    score >= 40 -> Color(0xFFE65100)
    else -> Color(0xFFB71C1C)
}
