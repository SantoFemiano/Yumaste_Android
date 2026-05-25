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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.YumasteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfiloScreen(
    viewModel: YumasteViewModel,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val profile by viewModel.userProfile.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val addresses by viewModel.addresses.collectAsState()

    // Form states
    var isEditingProfile by remember { mutableStateOf(false) }
    var nome by remember { mutableStateOf("") }
    var cognome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var isChangingPassword by remember { mutableStateOf(false) }
    var vecchiaPsw by remember { mutableStateOf("") }
    var nuovaPsw by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var showAddressForm by remember { mutableStateOf(false) }
    var via by remember { mutableStateOf("") }
    var civico by remember { mutableStateOf("") }
    var cap by remember { mutableStateOf("") }
    var citta by remember { mutableStateOf("") }
    var provincia by remember { mutableStateOf("") }

    // Init values when profile loads
    LaunchedEffect(profile) {
        profile?.let {
            nome = it.nome
            cognome = it.cognome
            email = it.email
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfileAndData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EarthyBackgroundGradient)
    ) {
        // Top Header Title block
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
            tonalElevation = 6.dp,
            shadowElevation = 3.dp,
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Person, "User profile icon", tint = PrimaryIndigo)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Profilo Utente", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextSlateDark)
            }
        }

        if (profile == null) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryIndigo)
            }
        } else {
            val curr = profile!!
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillPrimaryKeyColumn()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- USER AVATAR BADGE ---
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = ((curr.nome.take(1)) + (curr.cognome.take(1))).uppercase()
                    Text(
                        text = initials,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "${curr.nome} ${curr.cognome}",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = TextSlateDark
                )
                Text(
                    text = curr.email,
                    fontSize = 13.sp,
                    color = TextSlateMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Error / Success prompts
                if (authError != null) {
                    Text(
                        text = authError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // --- DATI ACCOUNT DETAILS CARD ---
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("profile_credentials_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Dati Personali", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextSlateDark)
                            TextButton(onClick = { isEditingProfile = !isEditingProfile }) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(imageVector = if (isEditingProfile) Icons.Default.Close else Icons.Default.Edit, contentDescription = "Edit profile", modifier = Modifier.size(16.dp))
                                    Text(if (isEditingProfile) "Annulla" else "Modifica", fontSize = 13.sp)
                                }
                            }
                        }

                        if (!isEditingProfile) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ProfileDetailField("Nome:", curr.nome)
                            ProfileDetailField("Cognome:", curr.cognome)
                            ProfileDetailField("Inoltrato email:", curr.email)
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth().testTag("profile_nome"),
                                value = nome,
                                onValueChange = { nome = it },
                                label = { Text("Nome") },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth().testTag("profile_cognome"),
                                value = cognome,
                                onValueChange = { cognome = it },
                                label = { Text("Cognome") },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth().testTag("profile_email"),
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                modifier = Modifier.fillMaxWidth().testTag("profile_save_btn"),
                                enabled = !isAuthLoading,
                                onClick = {
                                    if (nome.trim().isEmpty() || cognome.trim().isEmpty() || email.trim().isEmpty()) {
                                        Toast.makeText(context, "I campi non possono essere vuoti!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.updateProfile(nome.trim(), cognome.trim(), email.trim()) {
                                            isEditingProfile = false
                                            Toast.makeText(context, "Profilo aggiornato con successo!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            ) {
                                if (isAuthLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                } else {
                                    Text("Salva Modifiche")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- CHANGE PASSWORD CARD SECTION ---
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("profile_security_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Security, "Shield security", tint = PrimaryIndigo, modifier = Modifier.size(18.dp))
                                Text("Sicurezza & Password", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextSlateDark)
                            }

                            TextButton(onClick = { isChangingPassword = !isChangingPassword }) {
                                Text(if (isChangingPassword) "Chiudi" else "Cambia", fontSize = 13.sp)
                            }
                        }

                        if (isChangingPassword) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth().testTag("pwd_old"),
                                value = vecchiaPsw,
                                onValueChange = { vecchiaPsw = it },
                                label = { Text("Vecchia Password") },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth().testTag("pwd_new"),
                                value = nuovaPsw,
                                onValueChange = { nuovaPsw = it },
                                label = { Text("Nuova Password") },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle visually"
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                modifier = Modifier.fillMaxWidth().testTag("profile_pwd_save_btn"),
                                enabled = !isAuthLoading,
                                onClick = {
                                    if (vecchiaPsw.trim().isEmpty() || nuovaPsw.trim().isEmpty()) {
                                        Toast.makeText(context, "Attestati i campi password!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.changePassword(vecchiaPsw, nuovaPsw) {
                                            vecchiaPsw = ""; nuovaPsw = ""
                                            isChangingPassword = false
                                            Toast.makeText(context, "Password modificata correttamente!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            ) {
                                if (isAuthLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                } else {
                                    Text("Aggiorna Password")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- MANAGED SHIPPING ADDRESSES CARD ---
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("profile_addresses_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.HomeWork, "Address icon", tint = PrimaryIndigo, modifier = Modifier.size(18.dp))
                                Text("I miei Indirizzi Salvati", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextSlateDark)
                            }
                            TextButton(onClick = { showAddressForm = !showAddressForm }) {
                                Text(if (showAddressForm) "Chiudi" else "+ Aggiungi", fontSize = 13.sp)
                            }
                        }

                        // Listed addresses
                        if (addresses.isEmpty() && !showAddressForm) {
                            Text(
                                text = "Nessun indirizzo salvato.",
                                fontStyle = FontStyle.Italic,
                                fontSize = 12.sp,
                                color = TextSlateMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                addresses.forEach { addr ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.LocationOn, "Pin", tint = SecondaryViolet, modifier = Modifier.size(18.dp))
                                        Column {
                                            Text("${addr.via}, ${addr.civico}", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                            Text("${addr.cap} - ${addr.citta} (${addr.provincia.uppercase()})", fontSize = 12.sp, color = TextSlateMedium)
                                        }
                                    }
                                }
                            }
                        }

                        // Expandable Form
                        AnimatedVisibility(visible = showAddressForm) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth().testTag("profile_addr_via"),
                                    value = via,
                                    onValueChange = { via = it },
                                    label = { Text("Via/Corso") },
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        modifier = Modifier.weight(0.5f).testTag("profile_addr_civico"),
                                        value = civico,
                                        onValueChange = { civico = it },
                                        label = { Text("Iv. civico") },
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        modifier = Modifier.weight(0.5f).testTag("profile_addr_cap"),
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
                                        modifier = Modifier.weight(0.6f).testTag("profile_addr_citta"),
                                        value = citta,
                                        onValueChange = { citta = it },
                                        label = { Text("Città") },
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        modifier = Modifier.weight(0.4f).testTag("profile_addr_provincia"),
                                        value = provincia,
                                        onValueChange = { if (it.length <= 2) provincia = it },
                                        label = { Text("Provincia (2 let.)") },
                                        singleLine = true
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    modifier = Modifier.fillMaxWidth().testTag("profile_addr_save"),
                                    onClick = {
                                        if (via.isEmpty() || civico.isEmpty() || cap.isEmpty() || citta.isEmpty() || provincia.isEmpty()) {
                                            Toast.makeText(context, "Compila la scheda dell'indirizzo!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.addNewAddress(via, civico, cap, citta, provincia) {
                                                Toast.makeText(context, "Indirizzo aggiunto correttamente!", Toast.LENGTH_SHORT).show()
                                                via = ""; civico = ""; cap = ""; citta = ""; provincia = ""
                                                showAddressForm = false
                                            }
                                        }
                                    }
                                ) {
                                    Text("Aggiungi Indirizzo")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // --- GLOBAL LOGOUT CTA ---
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("profile_logout_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        viewModel.logout {
                            Toast.makeText(context, "Log-out effettuato con successo.", Toast.LENGTH_SHORT).show()
                            onNavigateToLogin()
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = "Exit Account")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Esci dal tuo Account", fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun ProfileDetailField(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextSlateMedium)
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextSlateDark)
    }
}

// Composable helper to get maximum column width for layouts
@Composable
fun Modifier.fillPrimaryKeyColumn(): Modifier = this.fillMaxWidth()
