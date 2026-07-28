package com.civicsense.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.civicsense.android.ui.AlertListScreen
import com.civicsense.android.ui.DashboardScreen
import com.civicsense.android.ui.theme.CivicSenseTheme
import com.civicsense.shared.grpc.CivicSenseServiceAndroid
import com.civicsense.shared.viewmodel.CivicSenseViewModel

class MainActivity : ComponentActivity() {

    // ViewModel scoped to the activity lifecycle
    private val viewModel: CivicSenseViewModel by lazy {
        val service = CivicSenseServiceAndroid(
            host = "localhost",
            port = 50051
        )
        CivicSenseViewModel(service)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Auto-connect on launch
        viewModel.connect()

        setContent {
            CivicSenseTheme {
                CivicSenseApp(viewModel = viewModel)
            }
        }
    }
}

private data class NavTab(
    val label: String,
    val icon: ImageVector
)

private val tabs = listOf(
    NavTab("Dashboard", Icons.Default.Dashboard),
    NavTab("Alerts", Icons.Default.List),
    NavTab("Settings", Icons.Default.Settings)
)

@Composable
fun CivicSenseApp(viewModel: CivicSenseViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> DashboardScreen(
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            1 -> AlertListScreen(
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}
