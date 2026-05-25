package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.YumasteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: YumasteViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
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
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Brand Logo Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Yumaste",
                    color = PrimaryIndigo,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.testTag("brand_logo_text")
                )
                Text(
                    text = " Shop",
                    color = SecondaryViolet,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Light
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
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
                        text = "Bentornato!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Text(
                        text = "Accedi al tuo piano nutrizionale e ordini",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 20.dp)
                    )

                    // Error Message
                    if (error != null) {
                        Text(
                            text = error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .testTag("login_error_text")
                                .padding(bottom = 12.dp)
                                .align(Alignment.Start)
                        )
                    }

                    // Email Field
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input"),
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Mail,
                                contentDescription = "Email Icon"
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password Icon"
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Nascondi Password" else "Mostra Password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Login Action Button
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_login_button"),
                        enabled = !isLoading,
                        onClick = {
                            if (email.trim().isEmpty() || password.trim().isEmpty()) {
                                Toast.makeText(context, "Inserisci tutti i campi!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.login(email.trim(), password.trim()) {
                                    Toast.makeText(context, "Accesso completato! Ciao benvenuto.", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
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
                                "Accedi",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation to register
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Non hai ancora un account? ",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                TextButton(
                    modifier = Modifier.testTag("go_to_register_button"),
                    onClick = onNavigateToRegister
                ) {
                    Text(
                        "Registrati ora",
                        color = PrimaryIndigo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
