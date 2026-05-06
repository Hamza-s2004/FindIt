package com.example.findit.ui.compose

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.findit.data.auth.AuthManager

@Composable
fun ComposeScreen(
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val auth = AuthManager(context)

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var registrationPending by remember { mutableStateOf(false) }
    var pendingEmail by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Register",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!registrationPending) {
                Button(onClick = {

                    // Password policy: min 8 chars, uppercase, lowercase, digit, special
                    val pw = password
                    val pwOk = pw.length >= 8 && pw.any { it.isDigit() } && pw.any { it.isLowerCase() } && pw.any { it.isUpperCase() } && pw.any { !it.isLetterOrDigit() }

                    if (name.isBlank() || email.isBlank() || !pwOk) {
                        message = "Password must be at least 8 chars and include upper, lower, digit and special char. Fill all fields properly."
                        return@Button
                    }

                    auth.register(name, email, password) { success, msg ->
                        Handler(Looper.getMainLooper()).post {
                            if (success) {
                                registrationPending = true
                                pendingEmail = email
                                message = msg ?: "Registered. Please verify your email (check inbox) before logging in."
                            } else {
                                message = msg ?: "Error"
                            }
                        }
                    }

                }) {
                    Text("Register")
                }
            } else {
                // Pending verification UI
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        // Check verification and complete registration
                        auth.completeRegistrationIfVerified { ok, msg ->
                            Handler(Looper.getMainLooper()).post {
                                if (ok) {
                                    message = "Email verified. Logging in..."
                                    onSuccess()
                                } else {
                                    message = msg ?: "Not verified yet"
                                }
                            }
                        }
                    }) {
                        Text("I verified")
                    }

                    Button(onClick = {
                        auth.resendVerificationEmail { sent, msg ->
                            Handler(Looper.getMainLooper()).post {
                                if (sent) message = "Verification email resent" else message = msg ?: "Failed to resend"
                            }
                        }
                    }) {
                        Text("Resend verification")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = message)
        }
    }
}