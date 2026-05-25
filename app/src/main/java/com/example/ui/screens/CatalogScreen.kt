package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.BoxCatalogo
import com.example.ui.theme.*
import com.example.ui.viewmodel.YumasteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: YumasteViewModel,
    onNavigateToBoxDetail: (Int) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userName by viewModel.userName.collectAsState()

    val boxes by viewModel.boxes.collectAsState()
    val isLoading by viewModel.isLoadingCatalog.collectAsState()
    val error by viewModel.catalogError.collectAsState()

    val currentPage by viewModel.currentPage.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()

    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.catalogSearchQuery.collectAsState()

    var showAiChefQuiz by remember { mutableStateOf(false) }

    val categories = listOf(
        "Tutte", "Vegana", "Carne", "Pesce", "Keto", 
        "SenzaGlutine", "Dolci", "Panificati", "Giapponese", "Italiana", "Pasta"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSlate)
    ) {
        // --- 1. SUPERB TOP HEADER ---
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Greeting and Auth Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val greet = if (isLoggedIn) "Ciao, ${userName ?: "Utente"}!" else "Benvenuto!"
                        Text(
                            text = greet,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextSlateDark
                        )
                        Text(
                            text = if (isLoggedIn) "Scegli la tua dieta della settimana" else "Sfoglia e ordina i migliori menù d'Italia",
                            fontSize = 12.sp,
                            color = TextSlateMedium
                        )
                    }

                    if (isLoggedIn) {
                        IconButton(
                            modifier = Modifier.testTag("catalog_logout_button"),
                            onClick = {
                                viewModel.logout {
                                    Toast.makeText(context, "Disconnesso con successo.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Log out",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Button(
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("catalog_login_redirect_button"),
                            onClick = onNavigateToLogin,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                        ) {
                            Text("Accedi", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Real-time Search Filter Bar
                var searchInput by remember { mutableStateOf(searchQuery) }
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("catalog_search_bar"),
                    value = searchInput,
                    onValueChange = {
                        searchInput = it
                        viewModel.onSearchQueryChanged(it)
                    },
                    placeholder = { Text("Cerca la tua Box preferita...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, "Search Icon") },
                    trailingIcon = {
                        if (searchInput.isNotEmpty()) {
                            IconButton(onClick = {
                                searchInput = ""
                                viewModel.onSearchQueryChanged("")
                            }) {
                                Icon(Icons.Default.Clear, "Clear Search")
                            }
                        }
                    },
                    shape = RoundedCornerShape(26.dp),
                    singleLine = true
                )
            }
        }

        // --- 2. REGULAR SCROLLABLE SCREEN BODY ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            // Horizontal Categories Scroll Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        modifier = Modifier.testTag("category_chip_$cat"),
                        selected = isSelected,
                        onClick = { viewModel.onCategorySelected(cat) },
                        label = { Text(cat, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryIndigo,
                            selectedLabelColor = BackgroundSlate
                        )
                    )
                }
            }

            // --- 3. EXCITING AI CHEF CONTEXT BANNER & ASSISTANT MODAL ---
            if (!isLoading && error == null && searchQuery.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("ai_chef_banner"),
                    colors = CardDefaults.cardColors(containerColor = PrimaryIndigo)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Icon",
                                tint = AccentCoral
                            )
                            Text(
                                "Non sai cosa scegliere? Chef AI",
                                color = BackgroundSlate,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            "Dicci i tuoi obiettivi nutrizionali e intolleranze. L'intelligenza Yumaste indicherà la Box più bilanciata per te.",
                            color = BackgroundSlate.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Button(
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("toggle_ai_chef_button"),
                            onClick = { showAiChefQuiz = !showAiChefQuiz },
                            colors = ButtonDefaults.buttonColors(containerColor = BackgroundSlate, contentColor = PrimaryIndigo),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = if (showAiChefQuiz) "Chiudi Assistente" else "Chiedi all'IA",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Collapsible Form Section
                        AnimatedVisibility(visible = showAiChefQuiz) {
                            SmartChefForm(
                                viewModel = viewModel,
                                onNavigateToBoxDetail = onNavigateToBoxDetail,
                                onClose = { showAiChefQuiz = false }
                            )
                        }
                    }
                }
            }

            // --- 4. PAGINATED MEAL BOX GRID CONTAINER ---
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryIndigo)
                }
            } else if (error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = error ?: "Si è verificato un errore sconosciuto.",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.fetchCatalog() }) {
                        Text("Riprova Ora")
                    }
                }
            } else if (boxes.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = "No results",
                        tint = TextSlateLight,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Nessuna Box pasto trovata.",
                        color = TextSlateDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Prova a modificare i termini di ricerca o la categoria.",
                        color = TextSlateMedium,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(boxes) { box ->
                        MealBoxCard(
                            box = box,
                            onCardClicked = { onNavigateToBoxDetail(box.id) },
                            onAddToCartClicked = {
                                if (isLoggedIn) {
                                    viewModel.addBoxToCart(box.id, 1) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Devi essere loggato per inserire nei carrelli!", Toast.LENGTH_SHORT).show()
                                    onNavigateToLogin()
                                }
                            }
                        )
                    }
                }
            }

            // --- 5. LOWER PAGINATION CONTROLS BAR ---
            if (!isLoading && error == null && totalPages > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        modifier = Modifier.testTag("prev_page_button"),
                        enabled = currentPage > 0,
                        onClick = { viewModel.onPrevPage() }
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Pagina Precedente")
                    }

                    Text(
                        text = "Pagina ${currentPage + 1} di $totalPages",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = TextSlateMedium
                    )

                    IconButton(
                        modifier = Modifier.testTag("next_page_button"),
                        enabled = currentPage < totalPages - 1,
                        onClick = { viewModel.onNextPage() }
                    ) {
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Pagina Successiva")
                    }
                }
            }
        }
    }
}

@Composable
fun MealBoxCard(
    box: BoxCatalogo,
    onCardClicked: () -> Unit,
    onAddToCartClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(262.dp)
            .clickable { onCardClicked() }
            .testTag("box_card_${box.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                // Product Image loaded dynamically
                if (!box.immagineUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = box.immagineUrl,
                        contentDescription = box.nome,
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
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Meal Icon",
                            tint = TextSlateLight,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Discount tag overlay if applies
                val isDiscounted = box.prezzoScontato != null && box.prezzoScontato < box.prezzo
                if (isDiscounted) {
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = box.scontoApplicato ?: "OFFERTA",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Categories
                val categoryText = box.categoria ?: box.categorie?.joinToString(", ") ?: ""
                Text(
                    text = categoryText.uppercase(),
                    fontSize = 10.sp,
                    color = SecondaryViolet,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Title name
                Text(
                    text = box.nome,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextSlateDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )

                // Subtitle Description
                Text(
                    text = box.descrizione ?: "",
                    fontSize = 11.sp,
                    color = TextSlateMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp,
                    modifier = Modifier.weight(1f)
                )

                // Footer price and Cart Plus button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val isDiscounted = box.prezzoScontato != null && box.prezzoScontato < box.prezzo
                        if (isDiscounted) {
                            Text(
                                text = "€${String.format("%.2f", box.prezzo)}",
                                style = androidx.compose.ui.text.TextStyle(
                                    textDecoration = TextDecoration.LineThrough,
                                    fontStyle = FontStyle.Italic
                                ),
                                fontSize = 11.sp,
                                color = TextSlateLight
                            )
                            Text(
                                text = "€${String.format("%.2f", box.prezzoScontato!!)}",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = "€${String.format("%.2f", box.prezzo)}",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = TextSlateDark
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryIndigo)
                            .clickable { onAddToCartClicked() }
                            .testTag("add_box_btn_${box.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aggiungi al carrello",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartChefForm(
    viewModel: YumasteViewModel,
    onNavigateToBoxDetail: (Int) -> Unit,
    onClose: () -> Unit
) {
    var obiettivo by remember { mutableStateOf("Mangiare sano") }
    var tipoDieta by remember { mutableStateOf("Omnivora") }
    var calorie by remember { mutableStateOf(2000) }
    var allergeni by remember { mutableStateOf("") }

    val result by viewModel.aiResult.collectAsState()
    val isLoading by viewModel.isLoadingAiRecommend.collectAsState()
    val error by viewModel.aiRecommendError.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("smart_chef_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderSlateSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Yumaste Nutrizionista Virtuale",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextSlateDark
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (result == null) {
                // Goal Goal Objectives
                Text("Il tuo obiettivo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSlateMedium)
                listOf("Mangiare sano", "Dimagrimento", "Massa muscolare").forEach { goal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { obiettivo = goal }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = obiettivo == goal,
                            onClick = { obiettivo = goal },
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(goal, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Diet preferences
                Text("Stile alimentare", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSlateMedium)
                listOf("Omnivora", "Vegetariana", "Vegana").forEach { style ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tipoDieta = style }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tipoDieta == style,
                            onClick = { tipoDieta = style },
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(style, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Kcal values
                Text("Target Calorie Giornaliere: $calorie kcal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSlateMedium)
                Slider(
                    value = calorie.toFloat(),
                    onValueChange = { calorie = it.toInt() },
                    valueRange = 1000f..4000f,
                    steps = 30
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Allergens
                Text("Allergeni da evitare", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSlateMedium)
                OutlinedTextField(
                    value = allergeni,
                    onValueChange = { allergeni = it },
                    placeholder = { Text("es: Glutine, Lattosio (separati da virgola)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_ai_recommend_button"),
                    enabled = !isLoading,
                    onClick = {
                        val array = allergeni.split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                        viewModel.requestAiRecommend(obiettivo, tipoDieta, calorie, array)
                    }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Flare, "Dust Icon")
                            Text("Trova la Box ideale", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Show AI Chef recommendations
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PrimaryIndigo.copy(alpha = 0.06f), shape = RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = result?.messaggio ?: "",
                            fontStyle = FontStyle.Italic,
                            fontSize = 14.sp,
                            color = TextSlateDark,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (result?.boxId != null) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("go_to_recommended_box_btn"),
                            onClick = {
                                result?.boxId?.let { onNavigateToBoxDetail(it) }
                                onClose()
                            }
                        ) {
                            Text("Mostrami la box: ${result?.nomeBox}")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = { viewModel.clearAiResult() }
                    ) {
                        Text("Riprova il test")
                    }
                }
            }
        }
    }
}
