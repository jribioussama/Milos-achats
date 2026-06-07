package com.example.milos_achats.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.milos_achats.MilosApp
import com.example.milos_achats.data.BarProduct
import com.example.milos_achats.data.SupplierSection
import com.example.milos_achats.data.checkKey
import com.example.milos_achats.data.getWeekInfo
import com.example.milos_achats.ui.viewmodel.BarProductsViewModel
import com.example.milos_achats.ui.viewmodel.HomeViewModel
import com.example.milos_achats.ui.viewmodel.KitchenProductsViewModel
import com.example.milos_achats.ui.viewmodel.ServerProductsViewModel
import com.example.milos_achats.util.AppLogger
import com.example.milos_achats.util.EmailSender
import com.example.milos_achats.util.OrderImageGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerScreen(
    onBack: () -> Unit,
    onAdminBarClick: () -> Unit = {},
    onAdminKitchenClick: () -> Unit = {},
    onAdminServerClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val app     = context.applicationContext as MilosApp
    val vm: BarProductsViewModel      = viewModel(factory = BarProductsViewModel.Factory(app.repository, app.catalogRepository))
    val kvM: KitchenProductsViewModel = viewModel(factory = KitchenProductsViewModel.Factory(app.repository, app.catalogRepository))
    val svM: ServerProductsViewModel  = viewModel(factory = ServerProductsViewModel.Factory(app.repository, app.catalogRepository))
    val homeVm: HomeViewModel         = viewModel(factory = HomeViewModel.Factory(app.repository))
    val ordersStatus by homeVm.ordersStatus.collectAsStateWithLifecycle()
    val ordersReady  = ordersStatus.barConfirmed && ordersStatus.kitchenConfirmed && ordersStatus.serverConfirmed
    val checkStates  by vm.checkStates.collectAsStateWithLifecycle()
    val barSuppliers by vm.suppliers.collectAsStateWithLifecycle()
    val kitSuppliers by kvM.suppliers.collectAsStateWithLifecycle()
    val srvSuppliers by svM.suppliers.collectAsStateWithLifecycle()
    val weekInfo = remember { getWeekInfo() }

    val editableIndex = weekInfo.days.indexOfFirst { it.isEditable }
    val editableDay   = if (editableIndex >= 0) weekInfo.days[editableIndex] else null
    val monthName     = weekInfo.monthHeader.split(" ").first()
    val formattedDate = editableDay?.let { "${it.fullName} ${it.dayNumber} $monthName" } ?: ""

    val supplierGroups: List<Pair<SupplierSection, List<BarProduct>>> = remember(checkStates, editableIndex, barSuppliers) {
        if (editableIndex < 0) emptyList()
        else barSuppliers.mapNotNull { supplier ->
            val checked = supplier.products.filter {
                checkStates[checkKey(it.id, editableIndex, weekInfo.weekId)] == true
            }
            if (checked.isNotEmpty()) supplier to checked else null
        }
    }

    val kitchenGroups: List<Pair<SupplierSection, List<BarProduct>>> = remember(checkStates, editableIndex, kitSuppliers) {
        if (editableIndex < 0) emptyList()
        else kitSuppliers.mapNotNull { supplier ->
            val checked = supplier.products.filter {
                checkStates[checkKey(it.id, editableIndex, weekInfo.weekId)] == true
            }
            if (checked.isNotEmpty()) supplier to checked else null
        }
    }

    val serverGroups: List<Pair<SupplierSection, List<BarProduct>>> = remember(checkStates, editableIndex, srvSuppliers) {
        if (editableIndex < 0) emptyList()
        else srvSuppliers.mapNotNull { supplier ->
            val checked = supplier.products.filter {
                checkStates[checkKey(it.id, editableIndex, weekInfo.weekId)] == true
            }
            if (checked.isNotEmpty()) supplier to checked else null
        }
    }

    val mergedGroups: List<Pair<SupplierSection, List<BarProduct>>> = remember(supplierGroups, kitchenGroups, serverGroups) {
        mergeOrderGroups(supplierGroups, kitchenGroups, serverGroups)
    }

    var showSummary        by remember { mutableStateOf(false) }
    var showKitchenSummary by remember { mutableStateOf(false) }
    var showServerSummary  by remember { mutableStateOf(false) }
    var showMergedSummary  by remember { mutableStateOf(false) }
    var isGenerating  by remember { mutableStateOf(false) }
    val snackbarState  = remember { SnackbarHostState() }
    val scope          = rememberCoroutineScope()

    // Permission pour Android < 10
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchGeneration(scope, context, mergedGroups, formattedDate, snackbarState) {
                isGenerating = it
            }
        } else {
            scope.launch { snackbarState.showSnackbar("Permission refusée — impossible d'enregistrer les images") }
        }
    }

    fun onGenerateClick() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(permission)
                return
            }
        }
        launchGeneration(scope, context, mergedGroups, formattedDate, snackbarState) {
            isGenerating = it
        }
    }

    if (showMergedSummary && editableIndex >= 0) {
        ManagerSummaryDialog(
            title          = "Commande complète — $formattedDate",
            supplierGroups = mergedGroups,
            onDismiss      = { showMergedSummary = false },
        )
    }

    if (showSummary && editableIndex >= 0) {
        ManagerSummaryDialog(
            title          = "Commande Bar — $formattedDate",
            supplierGroups = supplierGroups,
            onDismiss      = { showSummary = false },
        )
    }

    if (showKitchenSummary && editableIndex >= 0) {
        ManagerSummaryDialog(
            title          = "Commande Cuisine — $formattedDate",
            supplierGroups = kitchenGroups,
            onDismiss      = { showKitchenSummary = false },
        )
    }

    if (showServerSummary && editableIndex >= 0) {
        ManagerSummaryDialog(
            title          = "Commande Serveur & Ménage — $formattedDate",
            supplierGroups = serverGroups,
            onDismiss      = { showServerSummary = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Espace Gérant", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = MaterialTheme.colorScheme.primary,
                    titleContentColor          = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Zone haute : titre + vérification ────────────────
            Spacer(Modifier.height(40.dp))
            Text(text = "📋", fontSize = 56.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text       = "Espace Gérant",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            if (formattedDate.isNotEmpty()) {
                Text(
                    text  = formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Spacer(Modifier.height(32.dp))

            // ── Statut des commandes ──────────────────────────────
            if (!ordersReady) {
                Surface(
                    shape  = RoundedCornerShape(12.dp),
                    color  = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            "Commandes en attente",
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize   = 13.sp,
                        )
                        Spacer(Modifier.height(4.dp))
                        listOf(
                            "🍹 Bar"             to ordersStatus.barConfirmed,
                            "🍳 Cuisine"         to ordersStatus.kitchenConfirmed,
                            "🫧 Serveur & Ménage" to ordersStatus.serverConfirmed,
                        ).forEach { (label, done) ->
                            Text(
                                text  = if (done) "✓  $label" else "○  $label",
                                fontSize = 12.sp,
                                color    = MaterialTheme.colorScheme.onErrorContainer.copy(
                                    alpha = if (done) 0.5f else 1f
                                ),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── Commande consolidée ───────────────────────────────
            Button(
                onClick  = { AppLogger.log("GÉRANT", "Consultation commande complète"); showMergedSummary = true },
                enabled  = ordersReady && mergedGroups.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(72.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                ),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📦  Toute la commande", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (ordersReady && mergedGroups.isNotEmpty()) {
                        Text(
                            text     = "${mergedGroups.size} fournisseur(s) · ${mergedGroups.sumOf { it.second.size }} article(s)",
                            fontSize = 12.sp,
                            color    = Color.White.copy(alpha = 0.80f),
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(
                color     = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp,
            )
            Spacer(Modifier.height(16.dp))

            // ── Vérification par type ─────────────────────────────
            Button(
                onClick  = { AppLogger.log("GÉRANT", "Consultation commande Bar"); showSummary = true },
                enabled  = ordersStatus.barConfirmed,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape    = RoundedCornerShape(14.dp),
            ) {
                Text("🍹  Vérifier commande Bar", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick  = { AppLogger.log("GÉRANT", "Consultation commande Cuisine"); showKitchenSummary = true },
                enabled  = ordersStatus.kitchenConfirmed,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape    = RoundedCornerShape(14.dp),
            ) {
                Text("🍳  Vérifier commande Cuisine", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick  = { AppLogger.log("GÉRANT", "Consultation commande Serveur & Ménage"); showServerSummary = true },
                enabled  = ordersStatus.serverConfirmed,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape    = RoundedCornerShape(14.dp),
            ) {
                Text("🫧  Vérifier commande Serveur & Ménage", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }

            // ── Séparateur ────────────────────────────────────────
            Spacer(Modifier.height(40.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(20.dp))

            // ── Zone basse : génération ───────────────────────────
            Button(
                onClick  = { AppLogger.log("GÉRANT", "Bouton Générer pressé pour $formattedDate"); onGenerateClick() },
                enabled  = ordersReady && mergedGroups.isNotEmpty() && !isGenerating,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = MaterialTheme.colorScheme.secondary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                ),
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onSecondary,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Génération en cours…", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                } else {
                    Text("🧾  Générer les bons de commande", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text  = when {
                    !ordersReady             -> "En attente de validation des 3 commandes"
                    mergedGroups.isNotEmpty() -> "${mergedGroups.size} fournisseur(s) — ${mergedGroups.sumOf { it.second.size }} article(s)"
                    else                     -> "Aucun article sélectionné"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )

            // ── Gestion du catalogue ──────────────────────────────
            Spacer(Modifier.height(40.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(16.dp))
            Text(
                text       = "Gestion du catalogue",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick  = onAdminBarClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
            ) {
                Text("🍹  Gérer le catalogue Bar", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick  = onAdminKitchenClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
            ) {
                Text("🍳  Gérer le catalogue Cuisine", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick  = onAdminServerClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
            ) {
                Text("🫧  Gérer le catalogue Serveur & Ménage", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            // ── Envoi des logs ────────────────────────────────────
            Spacer(Modifier.height(40.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(16.dp))
            Text(
                text       = "Support",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(12.dp))

            var isSendingLogs by remember { mutableStateOf(false) }
            OutlinedButton(
                onClick  = {
                    AppLogger.log("SUPPORT", "Envoi logs demandé")
                    isSendingLogs = true
                    scope.launch {
                        val result = withContext(Dispatchers.IO) { EmailSender.sendLogs() }
                        isSendingLogs = false
                        snackbarState.showSnackbar(
                            if (result.isSuccess) "Logs envoyés avec succès"
                            else "Échec envoi : ${result.exceptionOrNull()?.message}"
                        )
                    }
                },
                enabled  = !isSendingLogs,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
            ) {
                if (isSendingLogs) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Envoi en cours…", fontSize = 15.sp)
                } else {
                    Text("📤  Envoyer les logs", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Fusion des commandes par fournisseur ──────────────────────────────────────

private fun mergeOrderGroups(
    vararg groupLists: List<Pair<SupplierSection, List<BarProduct>>>
): List<Pair<SupplierSection, List<BarProduct>>> {
    val supplierRef = mutableMapOf<String, SupplierSection>()
    val bySupplier  = mutableMapOf<String, MutableMap<String, BarProduct>>()

    for (list in groupLists) {
        for ((supplier, products) in list) {
            supplierRef.getOrPut(supplier.id) { supplier }
            val nameMap = bySupplier.getOrPut(supplier.id) { mutableMapOf() }
            for (p in products) {
                val key = p.nameFr.trim().lowercase()
                val cur = nameMap[key]
                // même produit dans plusieurs listes → on garde la quantité max
                if (cur == null || p.quantity > cur.quantity) nameMap[key] = p
            }
        }
    }

    return supplierRef.keys.mapNotNull { id ->
        val products = bySupplier[id]?.values?.sortedBy { it.nameFr } ?: return@mapNotNull null
        if (products.isEmpty()) null else supplierRef[id]!! to products
    }.sortedBy { it.first.name }
}

// ── Lancement de la génération en arrière-plan ────────────────────────────────

private fun launchGeneration(
    scope: kotlinx.coroutines.CoroutineScope,
    context: android.content.Context,
    supplierGroups: List<Pair<SupplierSection, List<BarProduct>>>,
    formattedDate: String,
    snackbarState: SnackbarHostState,
    setLoading: (Boolean) -> Unit,
) {
    AppLogger.log("GÉRANT", "Génération démarrée — $formattedDate — ${supplierGroups.size} fournisseur(s), ${supplierGroups.sumOf { it.second.size }} article(s)")
    scope.launch {
        setLoading(true)
        var genSuccess = 0
        var genErrors  = 0
        val attachments = mutableListOf<Pair<String, ByteArray>>()

        var emailError: String? = null

        withContext(Dispatchers.IO) {
            OrderImageGenerator.clearFolder(context)
            supplierGroups.forEach { (supplier, products) ->
                OrderImageGenerator.generate(context, supplier, products, formattedDate)
                    .onSuccess { pair -> genSuccess++; attachments += pair }
                    .onFailure { e -> genErrors++; AppLogger.log("GÉRANT", "Erreur ${supplier.name}: ${e.message}") }
            }

            AppLogger.log("GÉRANT", "Génération terminée — $genSuccess bon(s) OK, $genErrors erreur(s)")

            if (attachments.isNotEmpty()) {
                val logName = "journal_${java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.FRENCH).format(java.util.Date())}.txt"
                attachments.add(logName to AppLogger.export())
                AppLogger.clear()
                val summary = supplierGroups.map { (s, p) -> s.name to p.size }
                EmailSender.send(
                    attachments     = attachments,
                    orderDate       = formattedDate,
                    supplierSummary = summary,
                ).onFailure { e ->
                    emailError = e.message ?: e.javaClass.simpleName
                    AppLogger.log("GÉRANT", "Échec envoi mail: $emailError")
                }
            }
        }

        setLoading(false)
        val genMsg = buildString {
            append(if (genErrors == 0) "$genSuccess bon(s) enregistré(s)" else "$genSuccess réussi(s), $genErrors erreur(s)")
            if (emailError != null) append(" — Mail non envoyé: $emailError")
        }
        snackbarState.showSnackbar(genMsg)
    }
}

// ── Dialog récapitulatif (read-only) ─────────────────────────────────────────

@Composable
private fun ManagerSummaryDialog(
    title: String,
    supplierGroups: List<Pair<SupplierSection, List<BarProduct>>>,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape          = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "${supplierGroups.sumOf { it.second.size }} article(s) commandé(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 380.dp)) {
                    supplierGroups.forEach { (supplier, products) ->
                        item(key = "mgr_${supplier.id}") {
                            Text(
                                text       = supplier.name,
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.primary,
                                modifier   = Modifier.padding(top = 8.dp, bottom = 2.dp),
                            )
                        }
                        items(items = products, key = { "mgr_${it.id}" }) { product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text     = product.nameFr,
                                        style    = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text  = product.nameAr,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    )
                                }
                                Text(
                                    text       = "× ${product.quantity}",
                                    style      = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = MaterialTheme.colorScheme.primary,
                                    modifier   = Modifier.padding(start = 8.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(onClick = onDismiss) { Text("Fermer") }
                }
            }
        }
    }
}
