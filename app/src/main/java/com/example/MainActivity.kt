package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.UltraEngineViewModel
import com.example.ui.screens.ArchitectureDocsScreen
import com.example.ui.screens.BenchmarksScreen
import com.example.ui.screens.MarketDataScreen
import com.example.ui.screens.TelemetryScreen
import com.example.ui.screens.VerificationScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PolishBorder
import com.example.ui.theme.PolishCanvas
import com.example.ui.theme.PolishPurple
import com.example.ui.theme.PolishPurpleContainer
import com.example.ui.theme.PolishSurfaceHeader
import com.example.ui.theme.PolishTextSecondary

enum class NavigationTab(val title: String, val icon: ImageVector) {
    TELEMETRY("Engine", Icons.Default.Speed),
    BENCHMARKS("Benchmarks", Icons.Default.Tune),
    DOMAINS("Domains", Icons.Default.ShowChart),
    VERIFICATION("Verify", Icons.Default.Verified),
    BLUEPRINT("Blueprint", Icons.Default.Code)
}

class MainActivity : ComponentActivity() {
    private val viewModel: UltraEngineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: UltraEngineViewModel) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = NavigationTab.values()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = PolishSurfaceHeader,
                contentColor = PolishTextSecondary,
                tonalElevation = 0.dp,
                modifier = Modifier.border(width = 1.dp, color = PolishBorder)
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PolishPurple,
                            selectedTextColor = PolishPurple,
                            indicatorColor = PolishPurpleContainer,
                            unselectedIconColor = PolishTextSecondary,
                            unselectedTextColor = PolishTextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PolishCanvas)
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> TelemetryScreen(viewModel = viewModel)
                1 -> BenchmarksScreen(viewModel = viewModel)
                2 -> MarketDataScreen(viewModel = viewModel)
                3 -> VerificationScreen(viewModel = viewModel)
                4 -> ArchitectureDocsScreen()
            }
        }
    }
}

