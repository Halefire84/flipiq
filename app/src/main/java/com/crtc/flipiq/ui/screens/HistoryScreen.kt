package com.crtc.flipiq.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crtc.flipiq.model.FlipResult
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    scans: List<FlipResult>,
    onSelect: (FlipResult) -> Unit,
    onDelete: (FlipResult) -> Unit
) {
    if (scans.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("No scans yet", style = MaterialTheme.typography.titleMedium)
                Text("Tap Calculate on the Scan tab to get started.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(scans, key = { it.id }) { scan ->
            ScanHistoryCard(scan, onClick = { onSelect(scan) }, onDelete = { onDelete(scan) })
        }
    }
}

@Composable
private fun ScanHistoryCard(result: FlipResult, onClick: () -> Unit, onDelete: () -> Unit) {
    val fmt = SimpleDateFormat("MMM d, h:mm a", Locale.US)
    val verdictColor = Color(result.verdictColor)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Verdict badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(verdictColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    result.displayVerdict.take(4),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(result.itemDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1)
                Text(
                    "Buy ${"$%.2f".format(result.buyPrice)} · " +
                    "Profit ${"$%.2f".format(result.mathResult.profit)} · " +
                    "${result.confidence.score}/100 conf",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(fmt.format(Date(result.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
