package com.example.unihogarguaranda.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.unihogarguaranda.R

// Tipografía del showcase: Poppins (moderna, legible y amigable)
val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_extrabold, FontWeight.ExtraBold),
    Font(R.font.poppins_extrabold, FontWeight.Black)
)

// Todos los estilos Material usan Poppins; los Text sin estilo heredan bodyLarge
private val Base = Typography()

val Typography = Typography(
    displayLarge = Base.displayLarge.copy(fontFamily = Poppins),
    displayMedium = Base.displayMedium.copy(fontFamily = Poppins),
    displaySmall = Base.displaySmall.copy(fontFamily = Poppins),
    headlineLarge = Base.headlineLarge.copy(fontFamily = Poppins),
    headlineMedium = Base.headlineMedium.copy(fontFamily = Poppins),
    headlineSmall = Base.headlineSmall.copy(fontFamily = Poppins),
    titleLarge = Base.titleLarge.copy(fontFamily = Poppins),
    titleMedium = Base.titleMedium.copy(fontFamily = Poppins),
    titleSmall = Base.titleSmall.copy(fontFamily = Poppins),
    bodyLarge = Base.bodyLarge.copy(fontFamily = Poppins),
    bodyMedium = Base.bodyMedium.copy(fontFamily = Poppins),
    bodySmall = Base.bodySmall.copy(fontFamily = Poppins),
    labelLarge = Base.labelLarge.copy(fontFamily = Poppins),
    labelMedium = Base.labelMedium.copy(fontFamily = Poppins),
    labelSmall = Base.labelSmall.copy(fontFamily = Poppins)
)
