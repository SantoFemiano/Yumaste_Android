package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Ordine
import com.example.data.model.OrdiniDettagliDTO
import com.example.ui.theme.*
import com.example.ui.viewmodel.YumasteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdiniScreen(
    viewModel: YumasteViewModel,
    onBack: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoadingOrders.collectAsState()
    val ordersError by viewModel.ordersError.collectAsState()

    val expandedOrderId by viewModel.expandedOrderId.collectAsState()
    val expandedOrderDetails by viewModel.expandedOrderDetails.collectAsState()
    val isLoadingDetails by viewModel.isLoadingOrderDetails.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchOrders()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EarthyBackgroundGradient)
    ) {
        // Screen Top Menu Navigation
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
            tonalElevation = 6.dp,
            shadowElevation = 3.dp,
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("orders_back_btn")) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go Menu", tint = TextSlateDark)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("I miei Ordini", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextSlateDark)
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryIndigo)
            }
        } else if (ordersError != null) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CloudOff, "Cloud error", tint = TextSlateLight, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Errore nel caricamento cronologia ordini.", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.fetchOrders() }) { Text("Ricarica") }
            }
        } else if (orders.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HistoryToggleOff,
                    contentDescription = "No order history",
                    tint = TextSlateLight,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Qualcosa bolle in pentola...",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = TextSlateDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Non hai ancora effettuato ordini su Yumaste Shop. Effettua la tua prima spesa per visualizzarla qui!",
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = TextSlateMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onBack) {
                    Text("Inizia la tua Spesa Benessere", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(orders) { order ->
                    val isExpanded = expandedOrderId == order.id
                    OrderRowCard(
                        order = order,
                        isExpanded = isExpanded,
                        onExpandToggle = { viewModel.toggleOrderExpansion(order.id) },
                        details = if (isExpanded) expandedOrderDetails else emptyList(),
                        isLoadingDetails = isExpanded && isLoadingDetails
                    )
                }
            }
        }
    }
}

@Composable
fun OrderRowCard(
    order: Ordine,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    details: List<OrdiniDettagliDTO>,
    isLoadingDetails: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main card row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandToggle() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Ordine #${order.codiceOrdine}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextSlateDark
                    )
                    Text(
                        "Data: ${order.dataOrdine}",
                        fontSize = 12.sp,
                        color = TextSlateMedium
                    )
                    Text(
                        "Totale: €${String.format("%.2f", order.totalePrezzo)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = PrimaryIndigo,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val badgeColor = when (order.statoSpedizione.uppercase()) {
                        "CONSEGNATO" -> Color(0xFF10B981)
                        "SPEDITO" -> Color(0xFF3B82F6)
                        "IN_ELABORAZIONE" -> Color(0xFFF59E0B)
                        else -> Color(0xFF94A3B8)
                    }

                    Surface(
                        color = badgeColor.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, badgeColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = order.statoSpedizione.replace("_", " ").uppercase(),
                            color = badgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand info",
                        tint = TextSlateMedium
                    )
                }
            }

            // Expanded Collapsible Area
            AnimatedVisibility(visible = isExpanded) {
                Divider()
                if (isLoadingDetails) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PrimaryIndigo)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundSlate)
                            .padding(16.dp)
                    ) {
                        Text(
                            "Dettagli Prodotti",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextSlateDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        details.forEach { det ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Fastfood,
                                        contentDescription = "Item Box",
                                        tint = SecondaryViolet,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Box pasto id: ${det.boxid}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    "${det.quantita}x | €${String.format("%.2f", det.prezzounitario)}",
                                    fontSize = 12.sp,
                                    color = TextSlateMedium,
                                    textAlign = TextAlign.End
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Spedizione metadata fields
                        if (details.isNotEmpty()) {
                            val general = details[0]
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                                Text("Metodo di Pagamento: ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSlateMedium)
                                Text(general.metodopagamento.replace("_", " "), fontSize = 11.sp, color = TextSlateDark)
                            }
                            if (general.corriere != null) {
                                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                                    Text("Corriere Logistico: ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSlateMedium)
                                    Text(general.corriere!!, fontSize = 11.sp, color = TextSlateDark)
                                }
                            }
                            if (general.indirizzorespdtodev != null) {
                                val addr = general.indirizzorespdtodev!!
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text("Spedito a: ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextSlateMedium)
                                    Text("${addr.via}, ${addr.civico} - ${addr.citta} (${addr.provincia.uppercase()})", fontSize = 11.sp, color = TextSlateDark)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
