package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Ingrediente
import com.example.data.model.MacroTotali
import com.example.ui.theme.*
import com.example.ui.viewmodel.YumasteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DettaglioBoxScreen(
    boxId: Int,
    viewModel: YumasteViewModel,
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    val detail by viewModel.activeBoxDetail.collectAsState()
    val aiDescription by viewModel.activeBoxDescriptionAi.collectAsState()
    val isLoadingDetail by viewModel.isLoadingBoxDetail.collectAsState()
    val isLoadingAiDesc by viewModel.isLoadingBoxDescriptionAi.collectAsState()
    val error by viewModel.boxDetailError.collectAsState()

    LaunchedEffect(boxId) {
        viewModel.fetchBoxDetail(boxId)
    }

    Scaffold(
        bottomBar = {
            if (detail != null && error == null) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val activeD = detail!!
                            val isDiscounted = activeD.prezzoScontato != null && activeD.prezzoScontato!! < activeD.prezzoOriginale
                            if (isDiscounted) {
                                Text(
                                    text = "€${String.format("%.2f", activeD.prezzoOriginale)}",
                                    style = androidx.compose.ui.text.TextStyle(textDecoration = TextDecoration.LineThrough),
                                    fontSize = 12.sp,
                                    color = TextSlateLight
                                )
                                Text(
                                    text = "€${String.format("%.2f", activeD.prezzoScontato!!)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text(
                                    text = "€${String.format("%.2f", activeD.prezzoOriginale)}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = TextSlateDark
                                )
                            }
                        }

                        Button(
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("detail_add_to_cart_btn"),
                            onClick = {
                                if (isLoggedIn) {
                                    viewModel.addBoxToCart(boxId, 1) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Loggati per inoltrare ordini o inserire nei carrelli!", Toast.LENGTH_SHORT).show()
                                    onNavigateToLogin()
                                }
                            },
                        ) {
                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Add Icon")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Aggiungi al carrello", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isLoadingDetail) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryIndigo)
            }
        } else if (error != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Error, "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(error ?: "Errore", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onBack) {
                    Text("Indietro al menù")
                }
            }
        } else if (detail != null) {
            val active = detail!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
            ) {
                // Image Banner with Floating Back button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    if (!active.immagineUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = active.immagineUrl,
                            contentDescription = active.nome,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(BorderSlateSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Restaurant, "Meal Food", modifier = Modifier.size(64.dp), tint = TextSlateLight)
                        }
                    }

                    // Floating circular Back button
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .testTag("detail_back_button")
                            .padding(16.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .align(Alignment.TopStart)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Categorie
                    Text(
                        text = active.categoria.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryViolet
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Title
                    Text(
                        text = active.nome,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = TextSlateDark
                    )

                    // Portions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.RestaurantMenu, "Portion Icon", tint = TextSlateMedium, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Porzioni per Box: ${active.porzioni}",
                            fontSize = 13.sp,
                            color = TextSlateMedium
                        )
                    }

                    // --- AI CHEF INSIGHT CARD ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("ai_description_card"),
                        colors = CardDefaults.cardColors(containerColor = PrimaryIndigo.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.AutoAwesome, "AI icon", tint = AccentCoral, modifier = Modifier.size(18.dp))
                                Text("Presentazione dello Chef AI", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryIndigo)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            if (isLoadingAiDesc) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp), color = PrimaryIndigo)
                            } else {
                                Text(
                                    text = aiDescription ?: "Caricamento presentazione nutrizionale...",
                                    fontSize = 13.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = TextSlateDark,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // --- NUTRITIONAL HUB DIAGRAMS ---
                    active.macroTotali?.let { macros ->
                        Text(
                            text = "Valori Nutrizionali per Box",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextSlateDark,
                            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                        )
                        NutritionHubLayout(macros)
                    }

                    // --- ALLERGEN WARNING WARNINGS ---
                    if (!active.allergeni.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, "Warning Allergeni", tint = MaterialTheme.colorScheme.error)
                                Column {
                                    Text("Attenzione Allergeni!", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                                    Text(
                                        text = "Questa box contiene: ${active.allergeni!!.joinToString(", ")}",
                                        fontSize = 12.sp,
                                        color = TextSlateDark
                                    )
                                }
                            }
                        }
                    }

                    // --- INGREDIENT LIST SECTIONS ---
                    if (!active.ingredienti.isNullOrEmpty()) {
                        Text(
                            text = "Ingredienti Selezionati (${active.ingredienti!!.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextSlateDark,
                            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            active.ingredienti!!.forEach { ing ->
                                IngredientRow(ing)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun NutritionHubLayout(macros: MacroTotali) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nutrition_hub"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderSlateSoft)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Calories Indicator Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Apporto Energetico", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(
                    "${macros.chilocalorie.toInt()} kcal",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = PrimaryIndigo
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Progress Split Bars: Proteins, Carbs, Fats
            Text("Proteine: ${macros.proteine}g", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = (macros.proteine / 100.0).coerceIn(0.0, 1.0).toFloat(),
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFEF4444), // Red proteins
                trackColor = BorderSlateSoft
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("Carboidrati: ${macros.carboidrati}g", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = (macros.carboidrati / 200.0).coerceIn(0.0, 1.0).toFloat(),
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFFBBF24), // Yellow carbs
                trackColor = BorderSlateSoft
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("Grassi: ${macros.grassi}g", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = (macros.grassi / 100.0).coerceIn(0.0, 1.0).toFloat(),
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF10B981), // Green lipids
                trackColor = BorderSlateSoft
            )

            // Additional supporting micro nutrients
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                if (macros.zuccheri != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Zuccheri", fontSize = 11.sp, color = TextSlateMedium)
                        Text("${macros.zuccheri}g", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSlateDark)
                    }
                }
                if (macros.fibre != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Fibre", fontSize = 11.sp, color = TextSlateMedium)
                        Text("${macros.fibre}g", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSlateDark)
                    }
                }
                if (macros.sale != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Sale", fontSize = 11.sp, color = TextSlateMedium)
                        Text("${macros.sale}g", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSlateDark)
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientRow(ing: Ingrediente) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderSlateSoft)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Adjust, "Bullet", tint = SecondaryViolet, modifier = Modifier.size(16.dp))
                    Text(ing.nomeIngrediente, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Text(
                    "${ing.quantitaNellaBox} ${ing.unitaMisura}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextSlateMedium
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(BackgroundSlate, shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Calorie: ${ing.chilocalorie.toInt()} kcal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Proteine: ${ing.proteine}g", fontSize = 11.sp)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Carboidrati: ${ing.carboidrati}g", fontSize = 11.sp)
                        Text("Grassi: ${ing.grassi}g", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
