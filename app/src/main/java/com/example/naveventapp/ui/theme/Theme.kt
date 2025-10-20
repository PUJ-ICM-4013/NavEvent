package com.example.naveventapp.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.naveventapp.R

val Vinotinto = Color(0xFF7E0E0E)
val GrisOscuro = Color(0xFF404040)
val Blanco = Color(0xFFFFFFFF)

// Fuentes locales (res/font/*.ttf)
private val Cutive = FontFamily(
    Font(R.font.cutive_regular)
)
private val Cairo = FontFamily(
    Font(R.font.cairo_regular, FontWeight.Normal)
)
private val Calligraffitti = FontFamily(
    Font(R.font.calligraffitti_regular)
)

private val CairoBold = FontFamily(
    Font(R.font.cairo_bold, FontWeight.Black)
)
val AppTypography = Typography(
    displaySmall = TextStyle( // Cutive 32 para el nombre de la app
        fontFamily = Cutive,
        fontSize = 32.sp,
        color = Blanco
    ),
    headlineSmall = TextStyle(
        fontFamily = CairoBold,
        fontWeight = FontWeight.Black,
        fontSize = 50.sp,
        color = Color.Black
    ),
    bodyMedium = TextStyle( // Cairo (texto cualquiera)
        fontFamily = Cairo,
        fontSize = 14.sp,
        color = Color.Black
    ),
    titleSmall = TextStyle( // Slogan Calligraffitti
        fontFamily = Calligraffitti,
        fontSize = 28.sp,
        color = Blanco
    )
)

private val LightColors = lightColorScheme(
    primary = Vinotinto,
    onPrimary = Blanco,
    surface = Blanco,
    onSurface = Color.Black
)

@Composable
fun NavEventTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, typography = AppTypography, content = content)
}


