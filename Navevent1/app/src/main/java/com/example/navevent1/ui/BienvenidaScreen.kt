package com.example.navevent1.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.foundation.text.ClickableText

private val DarkBg = Color(0xFF3F3F3F)


@Composable
fun BienvenidaScreen(navController: NavHostController) {
    Surface(color = DarkBg) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                //título NavEvent
                Text(
                    text = "NavEvent",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                //circulo (placeholder de logo/imagen)
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD9D9D9))
                )

                //frase
                Text(
                    text = "\"\"Eventos que fluyen contigo.\"\"",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                //boton Iniciar Sesión
                Button(
                    onClick = { navController.navigate("login") },
                    shape = CircleShape,
                ) {
                    Text("Iniciar Sesión", color = Color.White)
                }

                //enlace crear cuenta
                val annotated = buildAnnotatedString {
                    append("¿No tienes cuenta? ")
                    val tag = "crear"
                    pushStringAnnotation(tag, tag)
                    withStyle(SpanStyle(color = Color(0xFF64B5F6))) {
                        append("crea una")
                    }
                    pop()
                }
                ClickableText(
                    text = annotated,
                    style = LocalTextStyle.current.copy(color = Color(0xFFEAEAEA)),
                    onClick = { offset ->
                        annotated.getStringAnnotations("crear", offset, offset).firstOrNull()?.let {
                            navController.navigate("registro")
                        }
                    }
                )
            }
        }
    }
}
