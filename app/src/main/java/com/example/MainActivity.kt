package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PushScrollTheme

class MainActivity : ComponentActivity() {
    companion object {
        var isResumedState = false
    }

    private var homeViewModel: HomeViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleBlockedAppIntent(intent)
        setContent {
            PushScrollTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    val viewModel: HomeViewModel = viewModel()
                    homeViewModel = viewModel
                    handleBlockedAppIntent(intent)
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleBlockedAppIntent(intent)
    }

    private fun handleBlockedAppIntent(intent: Intent?) {
        val blockedAppName = intent?.getStringExtra("BLOCKED_APP_NAME")
        if (!blockedAppName.isNullOrBlank()) {
            homeViewModel?.setBlockedAppAlert(blockedAppName)
        }
        if (intent?.action == "com.example.action.OPEN_DAILY_GOAL" ||
            intent?.getStringExtra("OPEN_TAB") == "DAILY") {
            homeViewModel?.setActiveTab(0)
        }
    }

    override fun onResume() {
        super.onResume()
        isResumedState = true
        handleBlockedAppIntent(intent)
        com.example.util.InactivityReminderManager.recordActivityAndReschedule(this)
    }

    override fun onPause() {
        super.onPause()
        isResumedState = false
    }
}
