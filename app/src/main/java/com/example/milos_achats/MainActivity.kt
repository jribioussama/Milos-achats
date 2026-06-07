package com.example.milos_achats

import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.milos_achats.navigation.AppNavigation
import com.example.milos_achats.ui.theme.MilosachatsTheme
import com.example.milos_achats.util.AppLogger
import com.example.milos_achats.util.AppUpdater

class MainActivity : ComponentActivity() {

    private var installerLaunched = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MilosachatsTheme {
                AppNavigation()
            }
        }
        handleInstallStatus(intent)
    }

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
        handleInstallStatus(intent)
        if (intent.getBooleanExtra(AppUpdater.EXTRA_INSTALL_UPDATE, false)) {
            AppUpdater.installPending(this)
        }
    }

    private fun handleInstallStatus(intent: Intent?) {
        if (intent?.action != "com.example.milos_achats.INSTALL_STATUS") return
        val status  = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        AppLogger.log("UPDATE", "PackageInstaller status=$status message=$message")
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                // Android demande confirmation — on lance le dialog officiel
                val confirmIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_INTENT)
                }
                // Pas de FLAG_ACTIVITY_NEW_TASK — appelé depuis une Activity foreground
                confirmIntent?.let { startActivity(it) }
                AppLogger.log("UPDATE", "Dialog confirmation lancé")
            }
            PackageInstaller.STATUS_SUCCESS ->
                AppLogger.log("UPDATE", "Installation réussie!")
            else ->
                AppLogger.log("UPDATE", "Échec installation: status=$status")
        }
    }
}
