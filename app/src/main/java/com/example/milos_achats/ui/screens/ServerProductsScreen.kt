package com.example.milos_achats.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.milos_achats.MilosApp
import com.example.milos_achats.data.*
import com.example.milos_achats.ui.viewmodel.ServerProductsViewModel
import com.example.milos_achats.util.AppLogger

private val S_NAME_COL = 160.dp
private val S_QTY_COL  = 52.dp
private val S_DAY_COL  = 44.dp

private val SGreenConfirmed = Color(0xFF2E7D32)
private val SGreenBg        = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerProductsScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as MilosApp
    val vm: ServerProductsViewModel = viewModel(factory = ServerProductsViewModel.Factory(app.repository))
    val checkStates by vm.checkStates.collectAsStateWithLifecycle()
    val weekInfo = remember { getWeekInfo() }

    val editableIndex = weekInfo.days.indexOfFirst { it.isEditable }
    val editableDay   = if (editableIndex >= 0) weekInfo.days[editableIndex] else null
    val isConfirmed   = editableIndex >= 0 && checkStates[confirmedServerKey(editableIndex, weekInfo.weekId)] == true
    val monthName     = weekInfo.monthHeader.split(" ").first()
    val formattedDate = editableDay?.let { "${it.fullName} ${it.dayNumber} $monthName" } ?: ""

    val checkedCount = if (editableIndex >= 0)
        SERVER_SUPPLIERS.sumOf { s ->
            s.products.count { checkStates[checkKey(it.id, editableIndex, weekInfo.weekId)] == true }
        } else 0

    var showDevalidateDialog by remember { mutableStateOf(false) }
    var showConfirmDialog    by remember { mutableStateOf(false) }
    var showOrderSummary     by remember { mutableStateOf(false) }

    // ── Recherche ──────────────────────────────────────────────────────────────
    var searchOpen      by remember { mutableStateOf(false) }
    var searchQuery     by remember { mutableStateOf("") }
    var currentMatchIdx by remember { mutableStateOf(0) }
    val listState       = rememberLazyListState()
    val focusRequester  = remember { FocusRequester() }

    val flatItemIndices: Map<String, Int> = remember {
        buildMap {
            var idx = 0
            SERVER_SUPPLIERS.forEach { supplier ->
                idx++
                supplier.products.forEachIndexed { j, p -> put(p.id, idx + j) }
                idx += supplier.products.size
            }
        }
    }
    val matches = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else SERVER_SUPPLIERS.flatMap { it.products }
            .filter { it.nameFr.contains(searchQuery, ignoreCase = true) || it.nameAr.contains(searchQuery, ignoreCase = true) }
            .mapNotNull { p -> flatItemIndices[p.id]?.let { i -> i to p } }
    }
    val safeMatchIdx   = if (matches.isEmpty()) 0 else currentMatchIdx.coerceIn(0, matches.size - 1)
    val currentMatchId = matches.getOrNull(safeMatchIdx)?.second?.id

    LaunchedEffect(searchQuery) {
        currentMatchIdx = 0
        if (searchQuery.isNotBlank()) AppLogger.log("RECHERCHE_SERVEUR", "\"$searchQuery\" → ${matches.size} résultat(s)")
    }
    LaunchedEffect(currentMatchId) {
        currentMatchId?.let { id -> flatItemIndices[id]?.let { listState.animateScrollToItem(it) } }
    }
    LaunchedEffect(searchOpen) {
        if (searchOpen) {
            AppLogger.log("RECHERCHE_SERVEUR", "Recherche ouverte")
            focusRequester.requestFocus()
        } else {
            if (searchQuery.isNotBlank()) AppLogger.log("RECHERCHE_SERVEUR", "Recherche fermée — requête: \"$searchQuery\"")
            else AppLogger.log("RECHERCHE_SERVEUR", "Recherche fermée")
            searchQuery = ""
            currentMatchIdx = 0
        }
    }

    if (showDevalidateDialog && editableDay != null) {
        AlertDialog(
            onDismissRequest = { showDevalidateDialog = false },
            title = { Text("Dévalider la commande") },
            text  = {
                Text("Vous allez dévalider la commande serveur & ménage du $formattedDate. " +
                     "La colonne redeviendra modifiable. Voulez-vous continuer ?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        AppLogger.log("SERVEUR", "Dévalidation commande $formattedDate")
                        vm.unvalidateOrder(editableIndex, weekInfo.weekId)
                        showDevalidateDialog = false
                    }
                ) { Text("Dévalider", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDevalidateDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (showConfirmDialog && editableIndex >= 0) {
        ServerConfirmDialog(
            formattedDate = formattedDate,
            checkStates   = checkStates,
            editableIndex = editableIndex,
            weekId        = weekInfo.weekId,
            onConfirm = {
                AppLogger.log("SERVEUR", "Confirmation commande $formattedDate — $checkedCount article(s)")
                vm.confirmOrder(editableIndex, weekInfo.weekId)
                showConfirmDialog = false
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

    if (showOrderSummary && editableIndex >= 0) {
        ServerConfirmDialog(
            formattedDate = formattedDate,
            checkStates   = checkStates,
            editableIndex = editableIndex,
            weekId        = weekInfo.weekId,
            isReadOnly    = true,
            onConfirm     = {},
            onDismiss     = { showOrderSummary = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (searchOpen) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White, fontSize = 17.sp),
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) Text("Rechercher un produit…", color = Color.White.copy(alpha = 0.55f), fontSize = 17.sp)
                                inner()
                            }
                        )
                    } else {
                        Text("Serveur & Ménage", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (searchOpen) {
                        if (matches.isNotEmpty()) {
                            Text("${safeMatchIdx + 1}/${matches.size}", color = Color.White, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 2.dp))
                            IconButton(onClick = {
                                val i = if (safeMatchIdx > 0) safeMatchIdx - 1 else matches.size - 1
                                currentMatchIdx = i
                                AppLogger.log("RECHERCHE_SERVEUR", "← résultat ${i + 1}/${matches.size}: ${matches.getOrNull(i)?.second?.nameFr ?: ""}")
                            }, enabled = matches.size > 1) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Précédent", tint = Color.White)
                            }
                            IconButton(onClick = {
                                val i = if (safeMatchIdx < matches.size - 1) safeMatchIdx + 1 else 0
                                currentMatchIdx = i
                                AppLogger.log("RECHERCHE_SERVEUR", "→ résultat ${i + 1}/${matches.size}: ${matches.getOrNull(i)?.second?.nameFr ?: ""}")
                            }, enabled = matches.size > 1) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Suivant", tint = Color.White)
                            }
                        } else if (searchQuery.isNotBlank()) {
                            Text("0 résultat", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                        IconButton(onClick = { searchOpen = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                        }
                    } else {
                        if (isConfirmed) {
                            TextButton(onClick = { showDevalidateDialog = true }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)) {
                                Text("Dévalider")
                            }
                        }
                        IconButton(onClick = { searchOpen = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Rechercher", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = MaterialTheme.colorScheme.primary,
                    titleContentColor          = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor     = MaterialTheme.colorScheme.onPrimary,
                )
            )
        },
        bottomBar = {
            if (editableDay != null) {
                ServerConfirmCta(
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
            ServerTableHeader(hScroll = hScroll, weekInfo = weekInfo, isConfirmed = isConfirmed)
            HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.primary)
            LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                SERVER_SUPPLIERS.forEach { supplier ->
                    item(key = "s_header_${supplier.id}") {
                        ServerSupplierHeader(supplier = supplier, hScroll = hScroll)
                    }
                    items(items = supplier.products, key = { it.id }) { product ->
                        ServerProductRow(
                            product        = product,
                            hScroll        = hScroll,
                            checkStates    = checkStates,
                            weekInfo       = weekInfo,
                            isConfirmed    = isConfirmed,
                            isCurrentMatch = product.id == currentMatchId,
                            onToggle       = { dayIndex ->
                                val checked = checkStates[checkKey(product.id, dayIndex, weekInfo.weekId)] != true
                                AppLogger.log("SERVEUR", "${if (checked) "✓" else "✗"} ${product.nameFr} — ${weekInfo.days[dayIndex].name}")
                                vm.toggle(product.id, dayIndex, weekInfo.weekId)
                            },
                            onCloseSearch  = {
                                AppLogger.log("RECHERCHE_SERVEUR", "Sélection: ${product.nameFr}")
                                searchOpen = false
                            }
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

@Composable
private fun ServerConfirmCta(
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SGreenBg.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = SGreenConfirmed, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text       = "Commande serveur & ménage du $formattedDate validée",
                        fontWeight = FontWeight.SemiBold,
                        color      = SGreenConfirmed,
                    )
                }
                OutlinedButton(
                    onClick  = onViewOrder,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = SGreenConfirmed),
                    border   = androidx.compose.foundation.BorderStroke(1.dp, SGreenConfirmed),
                ) {
                    Text("Vérifier la commande", fontWeight = FontWeight.SemiBold)
                }
            } else {
                Button(
                    onClick  = onClick,
                    enabled  = true,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = SGreenConfirmed,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    ),
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text       = "Confirmer la liste serveur & ménage du $formattedDate",
                        fontWeight = FontWeight.SemiBold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ServerConfirmDialog(
    formattedDate: String,
    checkStates: Map<String, Boolean>,
    editableIndex: Int,
    weekId: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isReadOnly: Boolean = false,
) {
    val groups = SERVER_SUPPLIERS.mapNotNull { supplier ->
        val checked = supplier.products.filter { checkStates[checkKey(it.id, editableIndex, weekId)] == true }
        if (checked.isNotEmpty()) supplier to checked else null
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text       = "Commande Serveur & Ménage — $formattedDate",
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
                LazyColumn(modifier = Modifier.heightIn(max = 380.dp)) {
                    groups.forEach { (supplier, products) ->
                        item(key = "dlg_s_${supplier.id}") {
                            Text(
                                text       = supplier.name,
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.primary,
                                modifier   = Modifier.padding(top = 8.dp, bottom = 2.dp),
                            )
                        }
                        items(items = products, key = { "dlg_s_${it.id}" }) { product ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = product.nameFr, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(text = product.nameAr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (isReadOnly) {
                        Button(onClick = onDismiss) { Text("Fermer") }
                    } else {
                        TextButton(onClick = onDismiss) { Text("Annuler") }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = onConfirm,
                            colors  = ButtonDefaults.buttonColors(containerColor = SGreenConfirmed),
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Confirmer")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerTableHeader(hScroll: ScrollState, weekInfo: WeekInfo, isConfirmed: Boolean) {
    val divColor = MaterialTheme.colorScheme.outline
    val bgColor  = MaterialTheme.colorScheme.surfaceVariant
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).background(bgColor),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(S_NAME_COL))
        SColDivider(divColor)
        Spacer(Modifier.width(S_QTY_COL))
        SColDivider(divColor)
        Row(modifier = Modifier.horizontalScroll(hScroll)) {
            Box(modifier = Modifier.width(S_DAY_COL * DAY_COUNT), contentAlignment = Alignment.Center) {
                Text(weekInfo.monthHeader, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = divColor.copy(alpha = 0.4f))
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).background(bgColor),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SHeaderCell("Produit", S_NAME_COL, FontWeight.Bold)
        SColDivider(divColor)
        SHeaderCell("Qté", S_QTY_COL, FontWeight.Bold)
        SColDivider(divColor)
        Row(modifier = Modifier.horizontalScroll(hScroll)) {
            weekInfo.days.forEach { day -> SDayHeaderCell(day, isConfirmed, S_DAY_COL) }
        }
    }
}

@Composable
private fun ServerSupplierHeader(supplier: SupplierSection, hScroll: ScrollState) {
    val divColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).background(MaterialTheme.colorScheme.primaryContainer),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.width(S_NAME_COL).padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(supplier.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(supplier.deliveryInfo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        }
        SColDivider(divColor)
        Spacer(Modifier.width(S_QTY_COL))
        SColDivider(divColor)
        Row(modifier = Modifier.horizontalScroll(hScroll)) { Spacer(Modifier.width(S_DAY_COL * DAY_COUNT)) }
    }
}

@Composable
private fun ServerProductRow(
    product: BarProduct, hScroll: ScrollState, checkStates: Map<String, Boolean>,
    weekInfo: WeekInfo, isConfirmed: Boolean, isCurrentMatch: Boolean, onToggle: (Int) -> Unit,
    onCloseSearch: () -> Unit,
) {
    val editableIdx = weekInfo.days.indexOfFirst { it.isEditable }
    val rowChecked  = editableIdx >= 0 && checkStates[checkKey(product.id, editableIdx, weekInfo.weekId)] == true
    val divColor    = MaterialTheme.colorScheme.outlineVariant
    val wrappedToggle = { dayIndex: Int -> if (isCurrentMatch) onCloseSearch(); onToggle(dayIndex) }
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).background(
            when { isCurrentMatch -> Color(0xFFFFEE58).copy(alpha = 0.45f); rowChecked && isConfirmed -> SGreenBg.copy(alpha = 0.07f); rowChecked -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f); else -> Color.Transparent }
        ).then(if (isCurrentMatch) Modifier.clickable { onCloseSearch() } else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.width(S_NAME_COL).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(product.nameFr, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp)
            Text(product.nameAr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        SColDivider(divColor)
        Box(modifier = Modifier.width(S_QTY_COL).fillMaxHeight(), contentAlignment = Alignment.Center) {
            Text(product.quantity, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, maxLines = 1)
        }
        SColDivider(divColor)
        Row(modifier = Modifier.horizontalScroll(hScroll)) {
            weekInfo.days.forEachIndexed { dayIndex, day ->
                SDayCell(checkStates[checkKey(product.id, dayIndex, weekInfo.weekId)] == true, day.isEditable, isConfirmed, S_DAY_COL) { wrappedToggle(dayIndex) }
            }
        }
    }
}

@Composable private fun SColDivider(color: Color) = VerticalDivider(modifier = Modifier.fillMaxHeight(), thickness = 1.dp, color = color)

@Composable
private fun SDayHeaderCell(day: DayInfo, isConfirmed: Boolean, width: Dp) {
    val primary = MaterialTheme.colorScheme.primary
    val disabled = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    val bg = when { day.isEditable && isConfirmed -> SGreenBg.copy(alpha = 0.15f); day.isEditable -> primary.copy(alpha = 0.12f); else -> Color.Transparent }
    val textColor = when { day.isEditable && isConfirmed -> SGreenConfirmed; day.isEditable -> primary; else -> disabled }
    Box(modifier = Modifier.width(width).background(bg).padding(vertical = 5.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(day.name, style = MaterialTheme.typography.labelSmall, fontWeight = if (day.isEditable) FontWeight.Bold else FontWeight.Normal, color = textColor)
            Text(day.dayNumber, style = MaterialTheme.typography.labelLarge, fontWeight = if (day.isEditable) FontWeight.Bold else FontWeight.Normal, color = textColor)
            when {
                day.isEditable && isConfirmed -> Text("✓ ok", style = MaterialTheme.typography.labelSmall, color = SGreenConfirmed, fontWeight = FontWeight.Bold)
                day.isEditable -> Text("cmd", style = MaterialTheme.typography.labelSmall, color = primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SDayCell(isChecked: Boolean, isEditable: Boolean, isConfirmed: Boolean, width: Dp, onToggle: () -> Unit) {
    val enabled = isEditable && !isConfirmed
    val bg = when { isEditable && isConfirmed -> SGreenBg.copy(alpha = 0.10f); !isEditable -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f); else -> Color.Transparent }
    Box(modifier = Modifier.width(width).fillMaxHeight().background(bg), contentAlignment = Alignment.Center) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { if (enabled) onToggle() },
            enabled = enabled,
            colors = when {
                isEditable && isConfirmed -> CheckboxDefaults.colors(checkedColor = SGreenConfirmed, disabledCheckedColor = SGreenConfirmed.copy(alpha = 0.6f), disabledUncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                !enabled -> CheckboxDefaults.colors(disabledCheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), disabledUncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                else -> CheckboxDefaults.colors()
            }
        )
    }
}

@Composable
private fun SHeaderCell(text: String, width: Dp, fontWeight: FontWeight = FontWeight.Normal) {
    Box(modifier = Modifier.width(width).padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = fontWeight, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
