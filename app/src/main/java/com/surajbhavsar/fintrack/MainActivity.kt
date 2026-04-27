package com.surajbhavsar.fintrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.surajbhavsar.fintrack.core.designsystem.FinTrackrTheme
import com.surajbhavsar.fintrack.feature.auth.AuthGate
import com.surajbhavsar.fintrack.feature.theme.ThemeMode
import com.surajbhavsar.fintrack.feature.theme.ThemeViewModel
import com.surajbhavsar.fintrack.navigation.FinTrackrNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mode by themeViewModel.themeMode.collectAsStateWithLifecycle()
            val darkTheme = when (mode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }
            FinTrackrTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AuthGate(signedInContent = { FinTrackrNavHost() })
                }
            }
        }
    }
}
