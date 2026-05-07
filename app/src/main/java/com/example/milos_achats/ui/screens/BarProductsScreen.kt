package com.example.milos_achats.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.milos_achats.MilosApp
import com.example.milos_achats.data.BAR_SUPPLIERS
import com.example.milos_achats.data.BarProduct
import com.example.milos_achats.data.DAY_COUNT
import com.example.milos_achats.data.DayInfo
import com.example.milos_achats.data.SupplierSection
import com.example.milos_achats.data.WeekInfo
import com.example.milos_achats.data.checkKey
import com.example.milos_achats.data.confirmedKey
import com.example.milos_achats.data.getWeekInfo
import com.example.milos_achats.ui.viewmodel.BarProductsViewModel

private val NAME_COL = 160.dp
private val QTY_COL  = 52.dp
private val DAY_COL  = 44.dp

private val GreenConfirmed = Color(0xFF388E3C)
private val GreenBg        = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarProductsScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as MilosApp
    val vm: BarProductsViewModel = viewModel(factory = BarProductsViewModel.Factory(app.repository))
    val checkStates by vm.checkStates.collectAsStateWithLifecycle()
    val weekInfo = remember { getWeekInfo() }

    // ── Dérivés utiles ─────────────────────────────────────────────────────────
    val editableIndex  = weekInfo.days.indexOfFirst { it.isEditable }
    val editableDay    = if (editableIndex >= 0) weekInfo.days[editableIndex] else null
    val isConfirmed    = editableIndex >= 0 && checkStates[confirmedKey(editableIndex)] == true
    val monthName      = weekInfo.monthHeader.split(" ").first()
    val formattedDate  = editableDay?.let { "${it.name} ${it.dayNumber} $monthName" } ?: ""

    val checkedCount = if (editableIndex >= 0)
        BAR_SUPPLIERS.sumOf { s ->
            s.products.count { checkStates[checkKey(it.id, editableIndex)] == true }
        } else 0

    // ── États UI ───────────────────────────────────────────────────────────────
    var showDevalidateDialog by remember { mutableStateOf(false) }
    var showConfirmDialog    by remember { mutableStateOf(false) }
    var showOrderSummary     by remember { mutableStateOf(false) }

    // ── Dialog dévalidation ────────────────────────────────────────────────────
    if (showDevalidateDialog && editableDay != null) {
        AlertDialog(
            onDismissRequest = { showDevalidateDialog = false },
            title = { Text("Dévalider la commande") },
            text  = {
                Text("Vous allez dévalider la commande du $formattedDate. " +
                     "La colonne redeviendra modifiable. Voulez-vous continuer ?")
            },
            confirmButton = {
                TextButton(
                    onClick = { vm.unvalidateOrder(editableIndex); showDevalidateDialog = false }
                ) { Text("Dévalider", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDevalidateDialog = false }) { Text("Annuler") }
            }
        )
    }

    // ── Dialog confirmation ────────────────────────────────────────────────────
    if (showConfirmDialog && editableIndex >= 0) {
        OrderConfirmDialog(
            formattedDate = formattedDate,
            checkStates   = checkStates,
            editableIndex = editableIndex,
            onConfirm = {
                vm.confirmOrder(editableIndex)
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    // ── Dialog récap (lecture seule, commande déjà confirmée) ──────────────────
    if (showOrderSummary && editableIndex >= 0) {
        OrderConfirmDialog(
            formattedDate = formattedDate,
            checkStates   = checkStates,
            editableIndex = editableIndex,
            isReadOnly    = true,
            onConfirm     = {},
            onDismiss     = { showOrderSummary = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Produits Bar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (isConfirmed) {
                        TextButton(
                            onClick = { showDevalidateDialog = true },
                            colors  = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                        ) { Text("Dévalider") }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.primary,
                    titleContentColor      = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                )
            )
        },
        bottomBar = {
            if (editableDay != null) {
                ConfirmCta(
                    formattedDate = formattedDate,
                    checkedCount  = checkedCount,
                    isConfirmed   = isConfirmed,
                    onClick       = { showConfirmDialog = true },
                    onViewOrder   = { showOrderSummary = true },
                )
            }
        }
    ) { padding ->
        val hScroll = rememberScrollState()

        Column(modifier = Modifier.padding(padding)) {
            TableHeader(hScroll = hScroll, weekInfo = weekInfo, isConfirmed = isConfirmed)
            HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.primary)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                BAR_SUPPLIERS.forEach { supplier ->
                    item(key = "header_${supplier.id}") {
                        SupplierHeader(supplier = supplier, hScroll = hScroll)
                    }
                    items(items = supplier.products, key = { it.id }) { product ->
                        ProductRow(
                            product       = product,
                            hScroll       = hScroll,
                            checkStates   = checkStates,
                            weekInfo      = weekInfo,
                            isConfirmed   = isConfirmed,
                            onToggle      = { dayIndex -> vm.toggle(product.id, dayIndex) }
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color     = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

// ── CTA bas de page ────────────────────────────────────────────────────────────

@Composable
private fun ConfirmCta(
    formattedDate: String,
    checkedCount: Int,
    isConfirmed: Boolean,
    onClick: () -> Unit,
    onViewOrder: () -> Unit,
) {
    Surface(shadowElevation = 8.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (isConfirmed) {
                // Bandeau vert "validée"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GreenBg.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = GreenConfirmed,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Commande du $formattedDate validée",
                        fontWeight = FontWeight.SemiBold,
                        color = GreenConfirmed,
                    )
                }
                OutlinedButton(
                    onClick  = onViewOrder,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = GreenConfirmed),
                    border   = androidx.compose.foundation.BorderStroke(1.dp, GreenConfirmed),
                ) {
                    Text("Vérifier la commande", fontWeight = FontWeight.SemiBold)
                }
            } else {
                Button(
                    onClick  = onClick,
                    enabled  = checkedCount > 0,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = GreenConfirmed,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Confirmer la liste achat bar du $formattedDate",
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// ── Dialog liste confirmée ─────────────────────────────────────────────────────

@Composable
private fun OrderConfirmDialog(
    formattedDate: String,
    checkStates: Map<String, Boolean>,
    editableIndex: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isReadOnly: Boolean = false,
) {
    val groups = BAR_SUPPLIERS.mapNotNull { supplier ->
        val checked = supplier.products.filter {
            checkStates[checkKey(it.id, editableIndex)] == true
        }
        if (checked.isNotEmpty()) supplier to checked else null
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape         = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Titre
                Text(
                    text       = "Commande Bar — $formattedDate",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "${groups.sumOf { it.second.size }} article(s) à commander",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                // Liste groupée par fournisseur
                LazyColumn(modifier = Modifier.heightIn(max = 380.dp)) {
                    groups.forEach { (supplier, products) ->
                        // En-tête fournisseur
                        item(key = "dlg_${supplier.id}") {
                            Text(
                                text       = supplier.name,
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.primary,
                                modifier   = Modifier.padding(top = 8.dp, bottom = 2.dp),
                            )
                        }
                        items(items = products, key = { "dlg_${it.id}" }) { product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text  = product.nameFr,
                                        style = MaterialTheme.typography.bodySmall,
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

                // Boutons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    if (isReadOnly) {
                        Button(onClick = onDismiss) { Text("Fermer") }
                    } else {
                        TextButton(onClick = onDismiss) { Text("Annuler") }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = onConfirm,
                            colors  = ButtonDefaults.buttonColors(containerColor = GreenConfirmed),
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Confirmer")
                        }
                    }
                }
            }
        }
    }
}

// ── Header (ligne mois + ligne jour/date) ──────────────────────────────────────

@Composable
private fun TableHeader(hScroll: ScrollState, weekInfo: WeekInfo, isConfirmed: Boolean) {
    val divColor = MaterialTheme.colorScheme.outline
    val bgColor  = MaterialTheme.colorScheme.secondaryContainer

    // Ligne 1 : mois
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(bgColor),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(NAME_COL))
        ColDivider(divColor)
        Spacer(Modifier.width(QTY_COL))
        ColDivider(divColor)
        Row(modifier = Modifier.horizontalScroll(hScroll)) {
            Box(
                modifier = Modifier.width(DAY_COL * DAY_COUNT),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = weekInfo.monthHeader,
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }

    HorizontalDivider(thickness = 0.5.dp, color = divColor.copy(alpha = 0.4f))

    // Ligne 2 : noms des jours + dates
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(bgColor),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderCell(text = "Produit", width = NAME_COL, fontWeight = FontWeight.Bold)
        ColDivider(divColor)
        HeaderCell(text = "Qté", width = QTY_COL, fontWeight = FontWeight.Bold)
        ColDivider(divColor)
        Row(modifier = Modifier.horizontalScroll(hScroll)) {
            weekInfo.days.forEach { day ->
                DayHeaderCell(day = day, isConfirmed = isConfirmed, width = DAY_COL)
            }
        }
    }
}

// ── En-tête fournisseur ────────────────────────────────────────────────────────

@Composable
private fun SupplierHeader(supplier: SupplierSection, hScroll: ScrollState) {
    val divColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(MaterialTheme.colorScheme.primaryContainer),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .width(NAME_COL)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text       = supplier.name,
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
            Text(
                text  = supplier.deliveryInfo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            )
        }
        ColDivider(divColor)
        Spacer(Modifier.width(QTY_COL))
        ColDivider(divColor)
        Row(modifier = Modifier.horizontalScroll(hScroll)) {
            Spacer(Modifier.width(DAY_COL * DAY_COUNT))
        }
    }
}

// ── Ligne produit ──────────────────────────────────────────────────────────────

@Composable
private fun ProductRow(
    product: BarProduct,
    hScroll: ScrollState,
    checkStates: Map<String, Boolean>,
    weekInfo: WeekInfo,
    isConfirmed: Boolean,
    onToggle: (dayIndex: Int) -> Unit,
) {
    val editableIdx = weekInfo.days.indexOfFirst { it.isEditable }
    val rowChecked  = editableIdx >= 0 && checkStates[checkKey(product.id, editableIdx)] == true
    val divColor    = MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(
                when {
                    rowChecked && isConfirmed  -> GreenBg.copy(alpha = 0.07f)
                    rowChecked                 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    else                       -> Color.Transparent
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Nom produit (fixe)
        Column(
            modifier = Modifier
                .width(NAME_COL)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text     = product.nameFr,
                style    = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
            )
            Text(
                text  = product.nameAr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        ColDivider(divColor)

        // Quantité (fixe)
        Box(
            modifier = Modifier.width(QTY_COL).fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text       = product.quantity,
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.primary,
                maxLines   = 1,
            )
        }

        ColDivider(divColor)

        // Checkboxes jours (scrollable)
        Row(modifier = Modifier.horizontalScroll(hScroll)) {
            weekInfo.days.forEachIndexed { dayIndex, day ->
                DayCell(
                    isChecked  = checkStates[checkKey(product.id, dayIndex)] == true,
                    isEditable = day.isEditable,
                    isConfirmed = isConfirmed,
                    width      = DAY_COL,
                    onToggle   = { onToggle(dayIndex) }
                )
            }
        }
    }
}

// ── Composants partagés ────────────────────────────────────────────────────────

@Composable
private fun ColDivider(color: Color) {
    VerticalDivider(modifier = Modifier.fillMaxHeight(), thickness = 1.dp, color = color)
}

@Composable
private fun DayHeaderCell(day: DayInfo, isConfirmed: Boolean, width: Dp) {
    val primary  = MaterialTheme.colorScheme.primary
    val disabled = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)

    val bg = when {
        day.isEditable && isConfirmed -> GreenBg.copy(alpha = 0.15f)
        day.isEditable                -> primary.copy(alpha = 0.12f)
        else                          -> Color.Transparent
    }
    val textColor = when {
        day.isEditable && isConfirmed -> GreenConfirmed
        day.isEditable                -> primary
        else                          -> disabled
    }

    Box(
        modifier = Modifier
            .width(width)
            .background(bg)
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = day.name,
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = if (day.isEditable) FontWeight.Bold else FontWeight.Normal,
                color      = textColor,
            )
            Text(
                text       = day.dayNumber,
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = if (day.isEditable) FontWeight.Bold else FontWeight.Normal,
                color      = textColor,
            )
            when {
                day.isEditable && isConfirmed ->
                    Text("✓ ok", style = MaterialTheme.typography.labelSmall,
                         color = GreenConfirmed, fontWeight = FontWeight.Bold)
                day.isEditable ->
                    Text("cmd", style = MaterialTheme.typography.labelSmall,
                         color = primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DayCell(
    isChecked: Boolean,
    isEditable: Boolean,
    isConfirmed: Boolean,
    width: Dp,
    onToggle: () -> Unit,
) {
    val enabled = isEditable && !isConfirmed
    val lockedBg = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
    val confirmedBg = GreenBg.copy(alpha = 0.10f)

    val bg = when {
        isEditable && isConfirmed -> confirmedBg
        !isEditable               -> lockedBg
        else                      -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Checkbox(
            checked  = isChecked,
            onCheckedChange = { if (enabled) onToggle() },
            enabled  = enabled,
            colors   = when {
                isEditable && isConfirmed -> CheckboxDefaults.colors(
                    checkedColor         = GreenConfirmed,
                    disabledCheckedColor = GreenConfirmed.copy(alpha = 0.6f),
                    disabledUncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                )
                !enabled -> CheckboxDefaults.colors(
                    disabledCheckedColor   = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    disabledUncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                )
                else -> CheckboxDefaults.colors()
            }
        )
    }
}

@Composable
private fun HeaderCell(text: String, width: Dp, fontWeight: FontWeight = FontWeight.Normal) {
    Box(
        modifier = Modifier.width(width).padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text      = text,
            style     = MaterialTheme.typography.labelMedium,
            fontWeight = fontWeight,
            color     = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
