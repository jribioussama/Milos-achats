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
import com.example.milos_achats.data.BAR_SUPPLIERS
import com.example.milos_achats.data.BarProduct
import com.example.milos_achats.data.KITCHEN_SUPPLIERS
import com.example.milos_achats.data.SERVER_SUPPLIERS
import com.example.milos_achats.data.SupplierSection
import com.example.milos_achats.data.checkKey
import com.example.milos_achats.data.getWeekInfo
import com.example.milos_achats.ui.viewmodel.BarProductsViewModel
import com.example.milos_achats.ui.viewmodel.KitchenProductsViewModel
import com.example.milos_achats.ui.viewmodel.ServerProductsViewModel
import com.example.milos_achats.util.OrderImageGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app     = context.applicationContext as MilosApp
    val vm: BarProductsViewModel      = viewModel(factory = BarProductsViewModel.Factory(app.repository))
    val kvM: KitchenProductsViewModel = viewModel(factory = KitchenProductsViewModel.Factory(app.repository))
    val svM: ServerProductsViewModel  = viewModel(factory = ServerProductsViewModel.Factory(app.repository))
    val checkStates by vm.checkStates.collectAsStateWithLifecycle()
    val weekInfo = remember { getWeekInfo() }

    val editableIndex = weekInfo.days.indexOfFirst { it.isEditable }
    val editableDay   = if (editableIndex >= 0) weekInfo.days[editableIndex] else null
    val monthName     = weekInfo.monthHeader.split(" ").first()
    val formattedDate = editableDay?.let { "${it.fullName} ${it.dayNumber} $monthName" } ?: ""

    val supplierGroups: List<Pair<SupplierSection, List<BarProduct>>> = remember(checkStates, editableIndex) {
        if (editableIndex < 0) emptyList()
        else BAR_SUPPLIERS.mapNotNull { supplier ->
            val checked = supplier.products.filter {
                checkStates[checkKey(it.id, editableIndex)] == true
            }
            if (checked.isNotEmpty()) supplier to checked else null
        }
    }

    val kitchenGroups: List<Pair<SupplierSection, List<BarProduct>>> = remember(checkStates, editableIndex) {
        if (editableIndex < 0) emptyList()
        else KITCHEN_SUPPLIERS.mapNotNull { supplier ->
            val checked = supplier.products.filter {
                checkStates[checkKey(it.id, editableIndex)] == true
            }
            if (checked.isNotEmpty()) supplier to checked else null
        }
    }

    val serverGroups: List<Pair<SupplierSection, List<BarProduct>>> = remember(checkStates, editableIndex) {
        if (editableIndex < 0) emptyList()
        else SERVER_SUPPLIERS.mapNotNull { supplier ->
            val checked = supplier.products.filter {
                checkStates[checkKey(it.id, editableIndex)] == true
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

            // ── Commande consolidée ───────────────────────────────
            Button(
                onClick  = { showMergedSummary = true },
                enabled  = mergedGroups.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(72.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                ),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📦  Toute la commande", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (mergedGroups.isNotEmpty()) {
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
                onClick  = { showSummary = true },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape    = RoundedCornerShape(14.dp),
            ) {
                Text("🍹  Vérifier commande Bar", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick  = { showKitchenSummary = true },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape    = RoundedCornerShape(14.dp),
            ) {
                Text("🍳  Vérifier commande Cuisine", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick  = { showServerSummary = true },
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
                onClick  = { onGenerateClick() },
                enabled  = mergedGroups.isNotEmpty() && !isGenerating,
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
                text  = if (mergedGroups.isNotEmpty())
                            "${mergedGroups.size} fournisseur(s) — ${mergedGroups.sumOf { it.second.size }} article(s)"
                        else
                            "Aucune commande validée",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
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
    scope.launch {
        setLoading(true)
        var success = 0
        var errors  = 0
        withContext(Dispatchers.IO) {
            supplierGroups.forEach { (supplier, products) ->
                OrderImageGenerator.generate(context, supplier, products, formattedDate)
                    .onSuccess { success++ }
                    .onFailure { errors++ }
            }
        }
        setLoading(false)
        val message = if (errors == 0)
            "$success bon(s) enregistré(s) dans Photos/Milos Achats"
        else
            "$success réussi(s), $errors erreur(s)"
        snackbarState.showSnackbar(message)
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
