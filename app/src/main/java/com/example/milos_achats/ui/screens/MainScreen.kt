package com.example.milos_achats.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.milos_achats.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.milos_achats.MilosApp
import com.example.milos_achats.ui.viewmodel.HomeViewModel
import com.example.milos_achats.util.AppLogger

private val StatusGreen = Color(0xFF81C784)  // vert pastel légèrement foncé sur fond bleu
private val StatusRed   = Color(0xFFE57373)  // rouge-corail légèrement foncé sur fond bleu

@Composable
fun MainScreen(onBarClick: () -> Unit, onKitchenClick: () -> Unit, onServerClick: () -> Unit, onManagerClick: () -> Unit) {
    val app         = LocalContext.current.applicationContext as MilosApp
    val vm            = viewModel<HomeViewModel>(factory = HomeViewModel.Factory(app.repository))
    val ordersStatus  by vm.ordersStatus.collectAsStateWithLifecycle()
    val formattedDate by vm.formattedDate.collectAsStateWithLifecycle()
    val managerEnabled = ordersStatus.barConfirmed && ordersStatus.kitchenConfirmed && ordersStatus.serverConfirmed

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
                        AppLogger.log("GÉRANT", "Accès Gérant autorisé")
                        showPinDialog = false
                        pinInput = ""
                        pinError = false
                        onManagerClick()
                    } else {
                        AppLogger.log("GÉRANT", "Code PIN incorrect")
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
            .verticalScroll(rememberScrollState())
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
        OrderCta(
            emoji         = "🍹",
            label         = "Produits Bar",
            formattedDate = formattedDate,
            isConfirmed   = ordersStatus.barConfirmed,
            onClick       = { AppLogger.log("NAVIGATION", "Ouverture Produits Bar"); onBarClick() },
        )
        Spacer(Modifier.height(12.dp))
        OrderCta(
            emoji         = "🍳",
            label         = "Produits Cuisine",
            formattedDate = formattedDate,
            isConfirmed   = ordersStatus.kitchenConfirmed,
            onClick       = { AppLogger.log("NAVIGATION", "Ouverture Produits Cuisine"); onKitchenClick() },
        )
        Spacer(Modifier.height(12.dp))
        OrderCta(
            emoji         = "🫧",
            label         = "Serveur & Ménage",
            formattedDate = formattedDate,
            isConfirmed   = ordersStatus.serverConfirmed,
            onClick       = { AppLogger.log("NAVIGATION", "Ouverture Serveur & Ménage"); onServerClick() },
        )

        // ── Séparateur ────────────────────────────────────────────
        Spacer(Modifier.height(40.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(20.dp))

        // ── Zone basse : gérant ───────────────────────────────────
        Button(
            onClick  = { AppLogger.log("NAVIGATION", "Tentative accès Gérant"); showPinDialog = true },
            enabled  = managerEnabled,
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
            text  = if (managerEnabled) "Toutes les commandes validées — accès disponible"
                    else "Disponible après validation des 3 commandes (Bar, Cuisine, Serveur)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun OrderCta(
    emoji: String,
    label: String,
    formattedDate: String,
    isConfirmed: Boolean,
    onClick: () -> Unit,
) {
    val statusColor = if (isConfirmed) StatusGreen else StatusRed
    val statusText  = when {
        formattedDate.isEmpty() -> null
        isConfirmed  -> "✓  Commande du $formattedDate — validée"
        else         -> "●  Commande du $formattedDate — non encore validée"
    }
    Button(
        onClick  = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (statusText != null) 90.dp else 72.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        ) {
            Text(
                text       = "$emoji  $label",
                fontSize   = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White,
            )
            if (statusText != null) {
                Spacer(Modifier.height(6.dp))
                HorizontalDivider(
                    color     = Color.White.copy(alpha = 0.20f),
                    thickness = 1.dp,
                )
                Spacer(Modifier.height(6.dp))
                AutoSizeText(
                    text     = statusText,
                    color    = statusColor,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun AutoSizeText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    maxFontSize: Float = 14f,
    minFontSize: Float = 8f,
) {
    var fontSize    by remember(text) { mutableFloatStateOf(maxFontSize) }
    var readyToDraw by remember(text) { mutableStateOf(false) }
    Text(
        text       = text,
        color      = color,
        fontSize   = fontSize.sp,
        fontWeight = FontWeight.Normal,
        textAlign  = TextAlign.Center,
        maxLines   = 1,
        softWrap   = false,
        overflow   = TextOverflow.Clip,
        modifier   = modifier.drawWithContent { if (readyToDraw) drawContent() },
        onTextLayout = { result ->
            if (result.didOverflowWidth && fontSize > minFontSize) {
                fontSize -= 1f
                readyToDraw = false
            } else {
                readyToDraw = true
            }
        },
    )
}
