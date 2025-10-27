package com.example.naveventapp.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.naveventapp.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.flow.map
import com.google.firebase.auth.FirebaseAuth

// ---------- DataStore ----------
private val Context.dataStore by preferencesDataStore(name = "user_prefs")
private val KEY_REMEMBER = booleanPreferencesKey("remember_email")
private val KEY_EMAIL = stringPreferencesKey("email_saved")

// ---------- Helpers ----------
private fun mapFirebaseError(e: Exception): String {
    val msg = e.message ?: ""
    return when {
        msg.contains("password is invalid", true) -> "Contraseña incorrecta."
        msg.contains("no user record", true) -> "No existe una cuenta con ese correo."
        msg.contains("blocked all requests", true) -> "Demasiados intentos. Intenta más tarde."
        msg.contains("network", true) -> "Sin conexión. Verifica tu red."
        else -> "Error al iniciar sesión: ${e.localizedMessage}"
    }
}

// =============== PANTALLA ===============
@Composable
fun LogInScreen(
    onLoginSuccess: (uid: String) -> Unit = {},
    onCreateAccountClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Leer preferencias de “recordar correo”
    val remembered by remember {
        context.dataStore.data.map { it[KEY_REMEMBER] ?: false }
    }.collectAsState(initial = false)

    val savedEmail by remember {
        context.dataStore.data.map { it[KEY_EMAIL] ?: "" }
    }.collectAsState(initial = "")

    // Estados UI
    var email by remember(savedEmail) { mutableStateOf(savedEmail) }
    var pwd by remember { mutableStateOf("") }
    var remember by remember(remembered) { mutableStateOf(remembered) }
    var showPwd by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMsg) {
        errorMsg?.let {
            snackbarHostState.showSnackbar(it)
            errorMsg = null
        }
    }

    // Asegurar init (por si el Activity/Application no lo hizo aún)
    LaunchedEffect(Unit) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Spacer(Modifier.height(100.dp))

            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(50.dp))

            // ---- Correo
            Text(
                text = "Correo electrónico",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                singleLine = true,
                placeholder = { Text("tucorreo@dominio.com") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black,
                    cursorColor = Vinotinto,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // ---- Contraseña
            Text(
                text = "Contraseña",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = pwd,
                onValueChange = { pwd = it },
                singleLine = true,
                placeholder = { Text("••••••••") },
                visualTransformation = if (showPwd) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Text(
                        text = if (showPwd) "Ocultar" else "Ver",
                        color = Vinotinto,
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clickable { showPwd = !showPwd }
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black,
                    cursorColor = Vinotinto,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = remember,
                    onCheckedChange = { remember = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Vinotinto,
                        uncheckedColor = Vinotinto,
                        checkmarkColor = Blanco
                    )
                )
                Text(
                    text = "Recordar Correo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }

            Spacer(Modifier.height(200.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 260.dp)
                    .background(GrisOscuro, RoundedCornerShape(12.dp))
                    .padding(vertical = 24.dp, horizontal = 16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (email.isBlank() || pwd.isBlank()) {
                                errorMsg = "Completa correo y contraseña."
                                return@Button
                            }
                            scope.launch {
                                isLoading = true
                                try {
                                    // 🔐 Garantiza init por si acaso
                                    if (FirebaseApp.getApps(context).isEmpty()) {
                                        FirebaseApp.initializeApp(context)
                                    }

                                    // ✅ OBTÉN AUTH AQUÍ (no al componer)
                                    val auth = FirebaseAuth.getInstance()

                                    val result = auth
                                        .signInWithEmailAndPassword(email.trim(), pwd)
                                        .await()

                                    // Guardar/limpiar preferencia
                                    context.dataStore.edit { prefs ->
                                        prefs[KEY_REMEMBER] = remember
                                        if (remember) prefs[KEY_EMAIL] = email.trim()
                                        else prefs.remove(KEY_EMAIL)
                                    }

                                    val uid = result.user?.uid.orEmpty()
                                    onLoginSuccess(uid)
                                } catch (e: Exception) {
                                    errorMsg = mapFirebaseError(e)
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Vinotinto,
                            contentColor = Blanco,
                            disabledContainerColor = Vinotinto.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(52.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Entrando...")
                        } else {
                            Text(
                                "Iniciar Sesión",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 20.sp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row {
                        Text(
                            text = "¿No tienes cuenta? ",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                        )
                        Text(
                            text = "crea una",
                            color = Color(0xFF2196F3),
                            textDecoration = TextDecoration.Underline,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable { onCreateAccountClick() }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LogInPreview() {
    NavEventTheme { LogInScreen() }
}
