package com.crtc.flipiq.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    isPro: Boolean,
    scanCount: Int,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Plan", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()) {
                    Text(if (isPro) "FlipIQ Pro" else "Free (3 scans/day)")
                    if (!isPro) {
                        TextButton(onClick = onUpgrade) { Text("Upgrade $14.99/mo") }
                    } else {
                        Text("Active", color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Usage", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Text("Total scans saved: $scanCount")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("About", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Text("FlipIQ v1.0 · CRTC LLC")
                Text("crtcai.com/flipiq", color = MaterialTheme.colorScheme.primary)
            }
        }

        if (!isPro) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("FlipIQ Pro", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold)
                    Text("• Unlimited scans per day")
                    Text("• Full scan history (unlimited)")
                    Text("• Priority support")
                    Spacer(Modifier.height(4.dp))
                    Button(onClick = onUpgrade, modifier = Modifier.fillMaxWidth()) {
                        Text("Upgrade for \$14.99/month")
                    }
                }
            }
        }
    }
}
