package com.example.milos_achats.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.milos_achats.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.milos_achats.MilosApp
import com.example.milos_achats.ui.viewmodel.HomeViewModel

private val GreenConfirmed = Color(0xFF2E7D32)
private val GreenBgLight   = Color(0xFF4CAF50)
private val AmberColor     = Color(0xFFD84315)
private val AmberBgLight   = Color(0xFFFF7043)

@Composable
fun MainScreen(onBarClick: () -> Unit, onManagerClick: () -> Unit) {
    val app         = LocalContext.current.applicationContext as MilosApp
    val vm          = viewModel<HomeViewModel>(factory = HomeViewModel.Factory(app.repository))
    val isConfirmed by vm.isOrderConfirmed.collectAsStateWithLifecycle()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput      by remember { mutableStateOf("") }
    var pinError      by remember { mutableStateOf(false) }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false; pinInput = ""; pinError = false },
            title = { Text("Accès Gérant") },
            text  = {
                Column {
                    Text("Saisissez votre code d'accès")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value         = pinInput,
                        onValueChange = { if (it.length <= 4) { pinInput = it; pinError = false } },
                        label         = { Text("Code") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        isError       = pinError,
                        supportingText = if (pinError) {
                            { Text("Code incorrect", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (pinInput == "1224") {
                        showPinDialog = false
                        pinInput = ""
                        pinError = false
                        onManagerClick()
                    } else {
                        pinError = true
                    }
                }) { Text("Valider") }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false; pinInput = ""; pinError = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Zone haute : responsable bar ─────────────────────────
        Spacer(Modifier.height(56.dp))
        Image(
            painter            = painterResource(R.drawable.logo_milos),
            contentDescription = "Logo Milos",
            contentScale       = ContentScale.Crop,
            modifier           = Modifier
                .size(110.dp)
                .clip(CircleShape),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text       = "Milos Achats",
            style      = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
        )
        Text(
            text      = "Gestion des commandes",
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(36.dp))
        if (vm.formattedDate.isNotEmpty()) {
            OrderStatusCard(
                formattedDate = vm.formattedDate,
                isConfirmed   = isConfirmed,
            )
            Spacer(Modifier.height(24.dp))
        }
        Button(
            onClick  = onBarClick,
            modifier = Modifier.fillMaxWidth().height(80.dp),
            shape    = RoundedCornerShape(16.dp),
        ) {
            Text(
                text       = "🍹  Produits Bar",
                fontSize   = 22.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // ── Séparateur ────────────────────────────────────────────
        Spacer(Modifier.weight(1f))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(20.dp))

        // ── Zone basse : gérant ───────────────────────────────────
        Button(
            onClick  = { showPinDialog = true },
            enabled  = isConfirmed,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape    = RoundedCornerShape(16.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor         = MaterialTheme.colorScheme.secondary,
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            ),
        ) {
            Text(
                text       = "🔐  Accès Gérant",
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text  = if (isConfirmed) "Commande validée — accès disponible"
                    else "Disponible après validation de la commande",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
        Spacer(Modifier.height(24.dp))
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
