package com.example.milos_achats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.milos_achats.MilosApp
import com.example.milos_achats.ui.viewmodel.HomeViewModel

private val GreenConfirmed = Color(0xFF388E3C)
private val GreenBgLight   = Color(0xFF4CAF50)
private val AmberColor     = Color(0xFFE65100)
private val AmberBgLight   = Color(0xFFFF9800)

@Composable
fun MainScreen(onBarClick: () -> Unit) {
    val app         = LocalContext.current.applicationContext as MilosApp
    val vm          = viewModel<HomeViewModel>(factory = HomeViewModel.Factory(app.repository))
    val isConfirmed by vm.isOrderConfirmed.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "☕", fontSize = 64.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text       = "Milos Achats",
            style      = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
        )
        Text(
            text  = "Gestion des commandes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(40.dp))

        // Carte statut commande (visible seulement si on a une date éditable)
        if (vm.formattedDate.isNotEmpty()) {
            OrderStatusCard(
                formattedDate = vm.formattedDate,
                isConfirmed   = isConfirmed,
            )
            Spacer(Modifier.height(28.dp))
        }

        // CTA principal
        Button(
            onClick   = onBarClick,
            modifier  = Modifier.fillMaxWidth().height(80.dp),
            shape     = RoundedCornerShape(16.dp),
        ) {
            Text(
                text       = "🍹  Produits Bar",
                fontSize   = 22.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun OrderStatusCard(formattedDate: String, isConfirmed: Boolean) {
    val bgColor   = if (isConfirmed) GreenBgLight.copy(alpha = 0.12f)
                    else AmberBgLight.copy(alpha = 0.12f)
    val iconColor = if (isConfirmed) GreenConfirmed else AmberColor
    val textColor = if (isConfirmed) GreenConfirmed else AmberColor
    val icon      = if (isConfirmed) Icons.Default.CheckCircle else Icons.Default.Warning
    val message   = if (isConfirmed)
                        "Commande du $formattedDate validée"
                    else
                        "Commande du $formattedDate\nnon encore validée"

    Surface(
        shape  = RoundedCornerShape(14.dp),
        color  = bgColor,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint     = iconColor,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text       = message,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = textColor,
            )
        }
    }
}
