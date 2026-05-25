package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CarrelloItem
import com.example.data.model.Indirizzo
import com.example.ui.theme.*
import com.example.ui.viewmodel.YumasteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarrelloScreen(
    viewModel: YumasteViewModel,
    onNavigateToCatalog: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val cartItems by viewModel.cartItems.collectAsState()
    val totalPrice by viewModel.cartTotalPrice.collectAsState()
    val isLoadingCart by viewModel.isLoadingCart.collectAsState()
    val cartError by viewModel.cartError.collectAsState()

    val addresses by viewModel.addresses.collectAsState()
    val selectedAddressId by viewModel.selectedAddressId.collectAsState()
    val isCheckoutLoading by viewModel.isCheckoutLoading.collectAsState()
    val checkoutSuccessCode by viewModel.checkoutSuccessOrderCode.collectAsState()

    var showAddAddressForm by remember { mutableStateOf(false) }

    // Forms fields
    var via by remember { mutableStateOf("") }
    var civico by remember { mutableStateOf("") }
    var cap by remember { mutableStateOf("") }
    var citta by remember { mutableStateOf("") }
    var provincia by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchCart()
        viewModel.fetchAddresses()
    }

    if (checkoutSuccessCode != null) {
        // High polish Checkout Success Modal Card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("success_checkout_modal"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(68.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Ordine Confermato!",
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = TextSlateDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Il tuo pagamento è andato a buon fine. Gli Chef Yumaste stanno iniziando a preparare la tua Box benessere!",
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = TextSlateMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        color = PrimaryIndigo.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "CODICE ORDINE: ${checkoutSuccessCode}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PrimaryIndigo,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dismiss_success_modal_btn"),
                        onClick = {
                            viewModel.fetchOrders() // Refresh orders before leaving
                            viewModel.logout { } // Triggers clear state fallback
                            viewModel.fetchUserProfileAndData() // Reinitialize
                            onNavigateToCatalog()
                        }
                    ) {
                        Text("Torna alla Home", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSlate)
        ) {
            // Screen Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillNavigableHeader()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ShoppingCart, "Cart Icon", tint = PrimaryIndigo)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Il mio Carrello", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextSlateDark)
                }
            }

            if (isLoadingCart && cartItems.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryIndigo)
                }
            } else if (cartError != null) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CloudOff, "Cloud error", tint = TextSlateLight, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Carrello non accessibile.", fontWeight = FontWeight.Bold)
                    Button(onClick = { viewModel.fetchCart() }) { Text("Ricarica") }
                }
            } else if (cartItems.isEmpty()) {
                // EXTREMELY POLISHED EMPTY STATE
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveShoppingCart,
                        contentDescription = "Empty cart",
                        tint = TextSlateLight,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Il tuo carrello è vuoto",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = TextSlateDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Sfoglia il nostro catalogo e trova la Box benessere ideale per i tuoi allenamenti o piaceri nutrienti.",
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = TextSlateMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        modifier = Modifier.testTag("back_to_shop_btn"),
                        onClick = onNavigateToCatalog
                    ) {
                        Text("Sfoglia il Menù Chef", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    // --- CART LIST ELEMENTS ---
                    Text(
                        text = "Articoli selezionati",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextSlateDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    cartItems.forEach { item ->
                        CartItemRow(
                            item = item,
                            onQuantityChanged = { q -> viewModel.updateCartItemQuantity(item.boxId, q) },
                            onDelete = { viewModel.removeCartItem(item.boxId) }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // --- SHIPPING ADDRESS SELECTION LAYOUT ---
                    Text(
                        text = "Indirizzo di Spedizione",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextSlateDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, BorderSlateSoft),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (addresses.isEmpty() && !showAddAddressForm) {
                                Text(
                                    "Nessun indirizzo salvato. Aggiungine uno per procedere.",
                                    fontSize = 13.sp,
                                    color = TextSlateMedium
                                )
                            } else {
                                addresses.forEach { addr ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { addr.id?.let { viewModel.selectAddress(it) } }
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedAddressId == addr.id,
                                            onClick = { addr.id?.let { viewModel.selectAddress(it) } },
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                "${addr.via}, ${addr.civico}",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                "${addr.cap} - ${addr.citta} (${addr.provincia.uppercase()})",
                                                fontSize = 12.sp,
                                                color = TextSlateMedium
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action button to append inline Address form
                            TextButton(
                                modifier = Modifier
                                    .testTag("add_address_inline_toggle_btn")
                                    .align(Alignment.Start),
                                onClick = { showAddAddressForm = !showAddAddressForm },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(
                                        imageVector = if (showAddAddressForm) Icons.Default.Close else Icons.Default.AddLocation,
                                        contentDescription = "Location"
                                    )
                                    Text(
                                        text = if (showAddAddressForm) "Annulla inserimento" else "+ Nuovo indirizzo",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            // Inline Form inputs
                            AnimatedVisibility(visible = showAddAddressForm) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                ) {
                                    OutlinedTextField(
                                        modifier = Modifier.fillMaxWidth().testTag("address_via"),
                                        value = via,
                                        onValueChange = { via = it },
                                        label = { Text("Via/Corso") },
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            modifier = Modifier.weight(0.5f).testTag("address_civico"),
                                            value = civico,
                                            onValueChange = { civico = it },
                                            label = { Text("N. civico") },
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            modifier = Modifier.weight(0.5f).testTag("address_cap"),
                                            value = cap,
                                            onValueChange = { cap = it },
                                            label = { Text("CAP") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            modifier = Modifier.weight(0.6f).testTag("address_citta"),
                                            value = citta,
                                            onValueChange = { citta = it },
                                            label = { Text("Città") },
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            modifier = Modifier.weight(0.4f).testTag("address_provincia"),
                                            value = provincia,
                                            onValueChange = { if (it.length <= 2) provincia = it },
                                            label = { Text("Prov. (2 let.)") },
                                            singleLine = true
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("submit_address_btn"),
                                        onClick = {
                                            if (via.isEmpty() || civico.isEmpty() || cap.isEmpty() || citta.isEmpty() || provincia.isEmpty()) {
                                                Toast.makeText(context, "Compila la scheda indirizzo!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                viewModel.addNewAddress(via, civico, cap, citta, provincia) {
                                                    Toast.makeText(context, "Indirizzo aggiunto e preselezionato!", Toast.LENGTH_SHORT).show()
                                                    via = ""; civico = ""; cap = ""; citta = ""; provincia = ""
                                                    showAddAddressForm = false
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Salva Indirizzo")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- ORDER TOTAL RECEIPT SUMMARY ---
                    Text(
                        text = "Riepilogo Ordine",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextSlateDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, BorderSlateSoft)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotale prodotti", color = TextSlateMedium, fontSize = 13.sp)
                                Text("€${String.format("%.2f", totalPrice)}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Spedizione Box", color = TextSlateMedium, fontSize = 13.sp)
                                Text("GRATIS", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 13.sp)
                            }
                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Totale complessivo", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextSlateDark)
                                Text(
                                    "€${String.format("%.2f", totalPrice)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 19.sp,
                                    color = PrimaryIndigo
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("execute_checkout_btn"),
                                enabled = !isCheckoutLoading,
                                onClick = {
                                    viewModel.executeCheckout(
                                        onSuccess = { code ->
                                            Toast.makeText(context, "Acquisto effettuato con successo!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { error ->
                                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            ) {
                                if (isCheckoutLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.CreditCard, "Card payment")
                                        Text("Invia Ordine & Paga", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CarrelloItem,
    onQuantityChanged: (Int) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("cart_item_${item.boxId}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderSlateSoft)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coil thumbnail image
            if (!item.immagineUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = item.immagineUrl,
                    contentDescription = item.nomeBox,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BackgroundSlate),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Restaurant, "Meal image", tint = TextSlateLight, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Body text details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nomeBox,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextSlateDark,
                    maxLines = 1
                )
                val unitPrice = item.prezzoScontato ?: item.prezzo ?: 0.0
                Text(
                    text = "€${String.format("%.2f", unitPrice)} / cad.",
                    fontSize = 12.sp,
                    color = TextSlateMedium
                )
                Text(
                    text = "Subtotale: €${String.format("%.2f", unitPrice * item.quantita)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = PrimaryIndigo,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Plus / Minus quantity and Delete Actions Column
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("cart_delete_${item.boxId}")
                ) {
                    Icon(Icons.Default.Delete, "Remove item", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .background(BackgroundSlate, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease",
                        tint = TextSlateDark,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .clickable { if (item.quantita > 1) onQuantityChanged(item.quantita - 1) }
                    )
                    Text(
                        text = item.quantita.toString(),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = TextSlateDark,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase",
                        tint = TextSlateDark,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .clickable { onQuantityChanged(item.quantita + 1) }
                    )
                }
            }
        }
    }
}

// Composable helper to get custom height for header
@Composable
fun Modifier.fillNavigableHeader(): Modifier = this.fillMaxWidth()
