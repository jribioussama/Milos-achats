package com.example.milos_achats

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.milos_achats.navigation.AppNavigation
import com.example.milos_achats.ui.theme.MilosachatsTheme
import com.example.milos_achats.util.AppUpdater

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MilosachatsTheme {
                AppNavigation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppUpdater.checkPendingInstall(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }
}
