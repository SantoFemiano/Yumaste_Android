package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RegistrazioneRequest
import com.example.ui.theme.*
import com.example.ui.viewmodel.YumasteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrazioneScreen(
    viewModel: YumasteViewModel,
    onRegistrationSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var cf by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var cognome by remember { mutableStateOf("") }
    var dataNascita by remember { mutableStateOf("") } // YYYY-MM-DD
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val error by viewModel.authError.collectAsState()
    val isLoading by viewModel.isAuthLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EarthyBackgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .verticalScroll(scrollState)
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand Logo Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Yumaste",
                    color = PrimaryIndigo,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = " Shop",
                    color = SecondaryViolet,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Registrati",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Text(
                        text = "Inserisci i tuoi dati per gustare le nostre Box pasto",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 16.dp)
                    )

                    // Error Message
                    if (error != null) {
                        Text(
                            text = error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .testTag("register_error_text")
                                .padding(bottom = 12.dp)
                                .align(Alignment.Start)
                        )
                    }

                    // Nome
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_nome_input"),
                        value = nome,
                        onValueChange = { nome = it },
                        label = { Text("Nome") },
                        leadingIcon = { Icon(Icons.Default.Person, "Name icon") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Cognome
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_cognome_input"),
                        value = cognome,
                        onValueChange = { cognome = it },
                        label = { Text("Cognome") },
                        leadingIcon = { Icon(Icons.Default.Person, "Surname icon") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Codice Fiscale
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_cf_input"),
                        value = cf,
                        onValueChange = { if (it.length <= 16) cf = it.uppercase() },
                        label = { Text("Codice Fiscale (16 car.)") },
                        leadingIcon = { Icon(Icons.Default.Badge, "CF icon") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Data Nascita
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_birthdate_input"),
                        value = dataNascita,
                        onValueChange = { dataNascita = it },
                        placeholder = { Text("es: 1995-10-15") },
                        label = { Text("Data di Nascita (AAAA-MM-GG)") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, "Date icon") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Telefono
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_phone_input"),
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Numero di Telefono") },
                        leadingIcon = { Icon(Icons.Default.Phone, "Phone icon") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Email
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_email_input"),
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Mail, "Email icon") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Password
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_password_input"),
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, "Password icon") },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit Register Button
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_register_button"),
                        enabled = !isLoading,
                        onClick = {
                            if (nome.trim().isEmpty() || cognome.trim().isEmpty() || cf.trim().isEmpty() ||
                                dataNascita.trim().isEmpty() || telefono.trim().isEmpty() ||
                                email.trim().isEmpty() || password.trim().isEmpty()
                            ) {
                                Toast.makeText(context, "Compila tutti i campi richiesti!", Toast.LENGTH_SHORT).show()
                            } else if (cf.trim().length != 16) {
                                Toast.makeText(context, "Il codice fiscale deve essere di 16 caratteri!", Toast.LENGTH_SHORT).show()
                            } else {
                                val req = RegistrazioneRequest(
                                    cf = cf.trim(),
                                    nome = nome.trim(),
                                    cognome = cognome.trim(),
                                    dataNascita = dataNascita.trim(),
                                    telefono = telefono.trim(),
                                    email = email.trim(),
                                    password = password.trim()
                                )
                                viewModel.register(req) {
                                    Toast.makeText(context, "Registrazione completata! Benvenuto, effettua ora l'accesso.", Toast.LENGTH_LONG).show()
                                    onRegistrationSuccess()
                                }
                            }
                        }
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                "Registrati",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back to login anchor
            TextButton(
                modifier = Modifier.testTag("back_to_login_button"),
                onClick = onNavigateToLogin
            ) {
                Text(
                    "Hai già un account? Accedi qui",
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
