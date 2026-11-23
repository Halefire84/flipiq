package com.crtc.flipiq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.crtc.flipiq.billing.BillingManager
import com.crtc.flipiq.data.ScanDatabase
import com.crtc.flipiq.data.toEntity
import com.crtc.flipiq.data.toFlipResult
import com.crtc.flipiq.model.FlipResult
import com.crtc.flipiq.ui.screens.HistoryScreen
import com.crtc.flipiq.ui.screens.ResultScreen
import com.crtc.flipiq.ui.screens.ScanScreen
import com.crtc.flipiq.ui.screens.SettingsScreen
import com.crtc.flipiq.ui.theme.FlipIQTheme

class MainActivity : ComponentActivity() {

    private lateinit var billing: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        billing = BillingManager(this)
        billing.connect()

        setContent {
            FlipIQTheme {
                FlipIQApp(billing)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billing.disconnect()
    }
}

private enum class Tab { SCAN, HISTORY, SETTINGS }

@Composable
fun FlipIQApp(billing: BillingManager) {
    val ctx = LocalContext.current
    val db = remember { ScanDatabase.get(ctx) }
    val isPro by billing.isPro.collectAsState()

    var selectedTab by remember { mutableStateOf(Tab.SCAN) }
    var pendingResult by remember { mutableStateOf<FlipResult?>(null) }
    var scans by remember { mutableStateOf(db.scanDao().allScans().map { it.toFlipResult() }) }

    val today = remember {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.timeInMillis
    }
    val todayScans by remember(scans) {
        derivedStateOf { db.scanDao().countSince(today) }
    }
    val dailyScansLeft = if (isPro) Int.MAX_VALUE
                         else maxOf(0, BillingManager.FREE_DAILY_SCANS - todayScans)

    // Show result screen on top if we have a pending result
    pendingResult?.let { result ->
        ResultScreen(
            result = result,
            onBack = { pendingResult = null },
            onSave = { r ->
                db.scanDao().insert(r.toEntity())
                scans = db.scanDao().allScans().map { it.toFlipResult() }
                pendingResult = null
                selectedTab = Tab.HISTORY
            }
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == Tab.SCAN,
                    onClick = { selectedTab = Tab.SCAN },
                    icon = { Icon(Icons.Default.Search, "Scan") },
                    label = { Text("Scan") }
                )
                NavigationBarItem(
                    selected = selectedTab == Tab.HISTORY,
                    onClick = { selectedTab = Tab.HISTORY },
                    icon = { Icon(Icons.Default.History, "History") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = selectedTab == Tab.SETTINGS,
                    onClick = { selectedTab = Tab.SETTINGS },
                    icon = { Icon(Icons.Default.Settings, "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            Tab.SCAN -> ScanScreen(
                isPro = isPro,
                dailyScansLeft = dailyScansLeft,
                onResult = { pendingResult = it },
                modifier = Modifier.padding(padding)
            )
            Tab.HISTORY -> HistoryScreen(
                scans = scans,
                onSelect = { pendingResult = it },
                onDelete = { r ->
                    db.scanDao().delete(r.id)
                    scans = db.scanDao().allScans().map { it.toFlipResult() }
                },
                modifier = Modifier.padding(padding)
            )
            Tab.SETTINGS -> SettingsScreen(
                isPro = isPro,
                scanCount = scans.size,
                onUpgrade = { /* billing.launchPurchaseFlow(activity) — wired in SettingsScreen */ },
                modifier = Modifier.padding(padding)
            )
        }
    }
}
