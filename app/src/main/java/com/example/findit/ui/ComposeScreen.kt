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

            Button(onClick = {

                if (name.isBlank() || email.isBlank() || password.length < 6) {
                    message = "Fill all fields properly"
                    return@Button
                }

                auth.register(name, email, password) { success, msg ->

                    // 🔥 FIX: switch to main thread
                    Handler(Looper.getMainLooper()).post {
                        if (success) {
                            message = "Registered!"
                            onSuccess()   // ✅ now works
                        } else {
                            message = msg ?: "Error"
                        }
                    }
                }

            }) {
                Text("Register")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = message)
        }
    }
}