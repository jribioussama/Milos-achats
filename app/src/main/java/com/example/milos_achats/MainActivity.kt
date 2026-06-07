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

    private var installerLaunched = false

    override fun onResume() {
        super.onResume()
        if (!installerLaunched && AppUpdater.hasPendingInstall(this)) {
            installerLaunched = true
            AppUpdater.installPending(this)
        }
    }

    override fun onPause() {
        super.onPause()
        installerLaunched = false
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra(AppUpdater.EXTRA_INSTALL_UPDATE, false)) {
            AppUpdater.installPending(this)
        }
    }
}
