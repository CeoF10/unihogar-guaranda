package com.example.unihogarguaranda

import android.graphics.Bitmap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.example.unihogarguaranda.model.MockData
import com.example.unihogarguaranda.ui.home.StudentCatalogContent
import com.example.unihogarguaranda.ui.login.WelcomeScreen
import com.example.unihogarguaranda.ui.theme.UniHogarGuarandaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.File

class AppAppearanceTest {
    @get:Rule val compose = createComposeRule()

    @Test fun welcomeActionsRemainAccessible() {
        var started = false
        var signedIn = false
        compose.setContent {
            UniHogarGuarandaTheme {
                WelcomeScreen(onStart = { started = true }, onHaveAccount = { signedIn = true })
            }
        }
        capture("welcome")
        compose.onNodeWithText("Encontrar mi hogar").performScrollTo().performClick()
        compose.onNodeWithText("Ya tengo una cuenta").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(true, started)
            assertEquals(true, signedIn)
        }
    }

    @Test fun catalogCategoriesAndFavoritesWork() {
        var selected: String? = null
        var saved = false
        compose.setContent {
            var category by remember { mutableStateOf<String?>(null) }
            UniHogarGuarandaTheme {
                StudentCatalogContent(
                    paddingValues = PaddingValues(top = 32.dp),
                    properties = listOf(MockData.initialProperties.first().copy(isFavorite = false, price = 150.75)),
                    searchText = "", onSearchChange = {},
                    selectedType = category,
                    onSelectType = { category = it; selected = it },
                    activeFilters = 0, onClearFilters = {}, onBannerClick = {}, onFilterClick = {},
                    onPropertyClick = {}, onToggleFavorite = { saved = true }
                )
            }
        }
        capture("catalog")
        compose.onNodeWithText("Habitaciones").performClick()
        compose.runOnIdle { assertEquals("Habitación", selected) }
        compose.onNodeWithText("$150.75").performScrollTo().assertIsDisplayed()
        capture("property")
        compose.onNodeWithContentDescription("Guardar en favoritos").performClick()
        compose.runOnIdle { assertEquals(true, saved) }
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.getExternalFilesDir(null), "$name.png")
        file.outputStream().use { stream ->
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
    }
}
