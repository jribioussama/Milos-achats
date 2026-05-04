package com.example.milos_achats.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.milos_achats.MilosApp
import com.example.milos_achats.data.BAR_SUPPLIERS
import com.example.milos_achats.data.BarProduct
import com.example.milos_achats.data.DAYS
import com.example.milos_achats.data.SupplierSection
import com.example.milos_achats.data.checkKey
import com.example.milos_achats.ui.viewmodel.BarProductsViewModel

private val NAME_COL = 160.dp
private val QTY_COL = 52.dp
private val DAY_COL = 44.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarProductsScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as MilosApp
    val vm: BarProductsViewModel = viewModel(factory = BarProductsViewModel.Factory(app.repository))
    val checkStates by vm.checkStates.collectAsStateWithLifecycle()

    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Réinitialiser la semaine") },
            text = { Text("Toutes les cases cochées seront effacées. Continuer ?") },
            confirmButton = {
                TextButton(onClick = {
                    vm.resetWeek()
                    showResetDialog = false
                }) { Text("Réinitialiser") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Annuler") }
            }
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
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Réinitialiser semaine")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                )
            )
        }
    ) { padding ->
        val hScroll = rememberScrollState()

        Column(modifier = Modifier.padding(padding)) {
            TableHeader(hScroll)
            HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.primary)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                BAR_SUPPLIERS.forEach { supplier ->
                    item(key = "header_${supplier.id}") {
                        SupplierHeader(supplier = supplier, hScroll = hScroll)
                    }
                    items(
                        items = supplier.products,
                        key = { it.id }
                    ) { product ->
                        ProductRow(
                            product = product,
                            hScroll = hScroll,
                            checkStates = checkStates,
                            onToggle = { dayIndex -> vm.toggle(product.id, dayIndex) }
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeader(hScroll: ScrollState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderCell(text = "Produit", width = NAME_COL, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.horizontalScroll(hScroll)) {
            HeaderCell(text = "Qté", width = QTY_COL, fontWeight = FontWeight.Bold)
            DAYS.forEach { day ->
                HeaderCell(text = day, width = DAY_COL, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SupplierHeader(supplier: SupplierSection, hScroll: ScrollState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .width(NAME_COL)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = supplier.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = supplier.deliveryInfo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
            }
        }
        Row(modifier = Modifier.horizontalScroll(hScroll)) {
            Spacer(modifier = Modifier.width(QTY_COL + DAY_COL * DAYS.size))
        }
    }
}

@Composable
private fun ProductRow(
    product: BarProduct,
    hScroll: ScrollState,
    checkStates: Map<String, Boolean>,
    onToggle: (dayIndex: Int) -> Unit,
) {
    val anyChecked = DAYS.indices.any { checkStates[checkKey(product.id, it)] == true }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (anyChecked) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                else Color.Transparent
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pinned: product name
        Column(
            modifier = Modifier
                .width(NAME_COL)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = product.nameFr,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
            )
            Text(
                text = product.nameAr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Scrollable: qty + day checkboxes
        Row(modifier = Modifier.horizontalScroll(hScroll)) {
            Box(
                modifier = Modifier
                    .width(QTY_COL)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.quantity,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                )
            }
            DAYS.indices.forEach { dayIndex ->
                val key = checkKey(product.id, dayIndex)
                val isChecked = checkStates[key] == true
                DayCell(
                    isChecked = isChecked,
                    width = DAY_COL,
                    onToggle = { onToggle(dayIndex) }
                )
            }
        }
    }
}

@Composable
private fun DayCell(isChecked: Boolean, width: Dp, onToggle: () -> Unit) {
    Box(
        modifier = Modifier.width(width),
        contentAlignment = Alignment.Center
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onToggle() },
        )
    }
}

@Composable
private fun HeaderCell(text: String, width: Dp, fontWeight: FontWeight = FontWeight.Normal) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = fontWeight,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
