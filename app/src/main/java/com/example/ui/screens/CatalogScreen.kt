package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.Brush
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
            .background(EarthyBackgroundGradient)
    ) {
        // --- 1. SUPERB TOP HEADER ---
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
            tonalElevation = 6.dp,
            shadowElevation = 3.dp,
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
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
                        .clickable { showAiChefQuiz = true }
                        .testTag("ai_chef_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryIndigo.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(PrimaryIndigo.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI",
                                    tint = AccentCoral,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Chef AI Yumaste ✨",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextSlateDark
                                )
                                Text(
                                    text = "Trova subito la tua Box nutrizionale ideale",
                                    fontSize = 11.sp,
                                    color = TextSlateMedium
                                )
                            }
                        }
                        
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Apri",
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (showAiChefQuiz) {
                    androidx.compose.ui.window.Dialog(
                        onDismissRequest = { showAiChefQuiz = false }
                    ) {
                        SmartChefForm(
                            viewModel = viewModel,
                            onNavigateToBoxDetail = onNavigateToBoxDetail,
                            onClose = { showAiChefQuiz = false }
                        )
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
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = box.id) {
        isVisible = true
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = (box.id % 4) * 40, easing = EaseOutQuad)
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.94f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(262.dp)
            .graphicsLayer(
                alpha = animatedAlpha,
                scaleX = animatedScale,
                scaleY = animatedScale
            )
            .clickable { onCardClicked() }
            .testTag("box_card_${box.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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

                    var buttonScale by remember { mutableStateOf(1f) }
                    val animatedButtonScale by animateFloatAsState(
                        targetValue = buttonScale,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium)
                    )
                    LaunchedEffect(buttonScale) {
                        if (buttonScale > 1f) {
                            kotlinx.coroutines.delay(120)
                            buttonScale = 1f
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .graphicsLayer(
                                scaleX = animatedButtonScale,
                                scaleY = animatedButtonScale
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryIndigo)
                            .clickable {
                                buttonScale = 1.3f
                                onAddToCartClicked()
                            }
                            .testTag("add_box_btn_${box.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aggiungi al carrello",
                            tint = BackgroundSlate,
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
            .padding(16.dp)
            .testTag("smart_chef_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite.copy(alpha = 0.98f)),
        border = BorderStroke(1.dp, BorderSlateSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Elegant Header Row with close icon button
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = AccentCoral,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Chef AI Yumaste",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextSlateDark
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Chiudi",
                        tint = TextSlateLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (result == null) {
                    // 1. Goal segmented pill card Row
                    Text("Il tuo obiettivo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val goals = listOf(
                            "Benessere" to "Mangiare sano",
                            "Dimagrire" to "Dimagrimento",
                            "Massa" to "Massa muscolare"
                        )
                        goals.forEach { (label, value) ->
                            val isSelected = obiettivo == value
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryIndigo.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) PrimaryIndigo else Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { obiettivo = value }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    color = if (isSelected) PrimaryIndigo else TextSlateMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Diet preference segmented pill card Row
                    Text("Stile alimentare", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val styles = listOf("Omnivora", "Vegetariana", "Vegana")
                        styles.forEach { style ->
                            val isSelected = tipoDieta == style
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryIndigo.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) PrimaryIndigo else Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { tipoDieta = style }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = style,
                                    fontSize = 12.sp,
                                    color = if (isSelected) PrimaryIndigo else TextSlateMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. calorie target slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Kcal Giornaliere", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                        Text("$calorie kcal", fontSize = 12.sp, fontWeight = FontWeight.Black, color = AccentCoral)
                    }
                    Slider(
                        value = calorie.toFloat(),
                        onValueChange = { calorie = it.toInt() },
                        valueRange = 1000f..4000f,
                        steps = 30,
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryIndigo,
                            activeTrackColor = PrimaryIndigo,
                            inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. allergens text field & smart touch suggestions tags
                    Text("Allergeni o intolleranze", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = allergeni,
                        onValueChange = { allergeni = it },
                        placeholder = { Text("es: Glutine, Lattosio...", fontSize = 12.sp, color = TextSlateLight) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryIndigo,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedContainerColor = Color.White.copy(alpha = 0.02f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Dynamic Tag Chips for single-tap insertion
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val popularAllergens = listOf("Glutine", "Lattosio", "Frutta guscio")
                        popularAllergens.forEach { allergen ->
                            val hasAllergen = allergeni.contains(allergen, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (hasAllergen) AccentCoral.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                                    .border(0.5.dp, if (hasAllergen) AccentCoral else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        val current = allergeni.trim()
                                        if (hasAllergen) {
                                            // Split, filter out match, re-join
                                            val parts = current.split(",")
                                                .map { it.trim() }
                                                .filter { !it.equals(allergen, ignoreCase = true) && it.isNotEmpty() }
                                            allergeni = parts.joinToString(", ")
                                        } else {
                                            allergeni = if (current.isEmpty()) allergen else "$current, $allergen"
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (hasAllergen) "✓ $allergen" else "+ $allergen",
                                    fontSize = 11.sp,
                                    color = if (hasAllergen) AccentCoral else TextSlateMedium
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    // Find Match Button
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("submit_ai_recommend_button"),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryIndigo,
                            contentColor = BackgroundSlate
                        ),
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            val array = allergeni.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                            viewModel.requestAiRecommend(obiettivo, tipoDieta, calorie, array)
                        }
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BackgroundSlate)
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Flare, "Sparkle Icon", modifier = Modifier.size(16.dp))
                                Text("Consigliami la Box ideale", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    // Show AI recommendation results in Dialog nicely and clean
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PrimaryIndigo.copy(alpha = 0.06f), shape = RoundedCornerShape(12.dp))
                                .border(1.dp, PrimaryIndigo.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                        ) {
                            Text(
                                text = result?.messaggio ?: "",
                                fontStyle = FontStyle.Italic,
                                fontSize = 13.sp,
                                color = TextSlateDark,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        if (result?.boxId != null) {
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("go_to_recommended_box_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryIndigo,
                                    contentColor = BackgroundSlate
                                ),
                                shape = RoundedCornerShape(12.dp),
                                onClick = {
                                    result?.boxId?.let { onNavigateToBoxDetail(it) }
                                    onClose()
                                }
                            ) {
                                Text("Mostrami la box: ${result?.nomeBox}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            onClick = { viewModel.clearAiResult() }
                        ) {
                            Text("Riprova il test", color = AccentCoral, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
