package com.example.unihogarguaranda.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unihogarguaranda.model.MockData
import com.example.unihogarguaranda.model.Property
import com.example.unihogarguaranda.ui.screens.FilterBottomSheet
import com.example.unihogarguaranda.ui.screens.FullProfileScreen
import com.example.unihogarguaranda.ui.screens.InteractiveMapScreen
import com.example.unihogarguaranda.ui.screens.PropertyDetailScreen
import com.example.unihogarguaranda.ui.theme.*

data class CategoryItem(val id: String, val title: String, val color: Color, val iconText: String)

val showcaseCategories = listOf(
    CategoryItem("hab", "Habitaciones", RedUEB, "🛏️"),
    CategoryItem("dep", "Departamentos", BlueUEB, "🏢"),
    CategoryItem("cas", "Casas", Color(0xFF10B981), "🏡")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    studentEmail: String,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var selectedCategory by remember { mutableStateOf("hab") }
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedPropertyDetail by remember { mutableStateOf<Property?>(null) }

    // Lista mutable de propiedades con estado compartido
    var propertyList by remember { mutableStateOf(MockData.initialProperties) }

    // Si está viendo el detalle de una vivienda
    if (selectedPropertyDetail != null) {
        PropertyDetailScreen(
            property = selectedPropertyDetail!!,
            onBack = { selectedPropertyDetail = null },
            onToggleFavorite = { prop ->
                propertyList = propertyList.map {
                    if (it.id == prop.id) it.copy(isFavorite = !it.isFavorite) else it
                }
                selectedPropertyDetail = selectedPropertyDetail?.copy(isFavorite = !selectedPropertyDetail!!.isFavorite)
            },
            onContactOwner = { prop ->
                Toast.makeText(context, "Abriendo WhatsApp con datos de: ${prop.title}", Toast.LENGTH_LONG).show()
            }
        )
        return
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            onDismiss = { showFilterSheet = false },
            onApply = { minP, maxP, type, services ->
                Toast.makeText(context, "Filtro aplicado: $$minP - $$maxP ($type)", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Uni", fontWeight = FontWeight.Black, fontSize = 22.sp, color = BlueUEB)
                        Text("Hogar", fontWeight = FontWeight.Black, fontSize = 22.sp, color = RedUEB)
                        Text(" Guaranda", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = BlueUEB)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        Toast.makeText(context, "No tienes notificaciones pendientes", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificaciones",
                            tint = RedUEB
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 6.dp) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RedUEB,
                        selectedTextColor = RedUEB,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = ChipBackground
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Place, contentDescription = "Mapa") },
                    label = { Text("Mapa", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RedUEB,
                        selectedTextColor = RedUEB,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = ChipBackground
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoritos") },
                    label = { Text("Favoritos", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RedUEB,
                        selectedTextColor = RedUEB,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = ChipBackground
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RedUEB,
                        selectedTextColor = RedUEB,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = ChipBackground
                    )
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> StudentCatalogContent(
                paddingValues = paddingValues,
                properties = propertyList,
                selectedCategory = selectedCategory,
                onSelectCategory = { selectedCategory = it },
                onFilterClick = { showFilterSheet = true },
                onPropertyClick = { selectedPropertyDetail = it },
                onToggleFavorite = { prop ->
                    propertyList = propertyList.map {
                        if (it.id == prop.id) it.copy(isFavorite = !it.isFavorite) else it
                    }
                }
            )
            1 -> InteractiveMapScreen(
                properties = propertyList,
                onFilterClick = { showFilterSheet = true },
                onPropertySelect = { selectedPropertyDetail = it }
            )
            2 -> FavoritesTabContent(
                paddingValues = paddingValues,
                properties = propertyList.filter { it.isFavorite },
                onPropertyClick = { selectedPropertyDetail = it },
                onRemoveFavorite = { prop ->
                    propertyList = propertyList.map {
                        if (it.id == prop.id) it.copy(isFavorite = false) else it
                    }
                }
            )
            3 -> FullProfileScreen(
                email = studentEmail,
                roleTitle = "Estudiante",
                onLogout = onLogout
            )
        }
    }
}

@Composable
fun StudentCatalogContent(
    paddingValues: PaddingValues,
    properties: List<Property>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onFilterClick: () -> Unit,
    onPropertyClick: (Property) -> Unit,
    onToggleFavorite: (Property) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Barra de búsqueda con botón de filtros
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onFilterClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar", tint = TextSecondary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Buscar por zona, precio o tipo...", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            }
        }

        // Categorías (Habitaciones, Departamentos, Casas)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                showcaseCategories.forEach { category ->
                    val isSelected = selectedCategory == category.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onSelectCategory(category.id) }
                            .padding(horizontal = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(category.color),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = category.iconText, fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = category.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) category.color else TextPrimary
                        )
                    }
                }
            }
        }

        // Banner informativo UEB
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlueUEB),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¡Encuentra tu hogar ideal!",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Viviendas cerca de la UEB y en todo Guaranda.",
                            color = Color(0xFFD1D5DB),
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(RedUEB),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("➔", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }

        // Sección Recomendados para ti
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recomendados para ti",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Ver todos ›",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BlueUEB,
                    modifier = Modifier.clickable { onFilterClick() }
                )
            }
        }

        // Listado de Tarjetas Inmobiliarias
        items(properties) { property ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPropertyClick(property) }
            ) {
                Column {
                    // Contenedor visual del inmueble
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏠", fontSize = 42.sp)
                            Text(
                                "Vista del Inmueble",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (property.isFeatured) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = RedUEB,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Destacado",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Botón de Favorito
                        IconButton(
                            onClick = { onToggleFavorite(property) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.9f))
                        ) {
                            Icon(
                                imageVector = if (property.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = RedUEB,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = property.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = property.location,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${property.beds} hab. · ${property.baths} · ${property.services.firstOrNull() ?: "Servicios"}",
                            fontSize = 12.sp,
                            color = BlueUEB
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$${property.price.toInt()}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BlueUEB
                                )
                                Text(
                                    text = " / mes",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📍", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = property.campusDistance,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun FavoritesTabContent(
    paddingValues: PaddingValues,
    properties: List<Property>,
    onPropertyClick: (Property) -> Unit,
    onRemoveFavorite: (Property) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Favoritos",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextPrimary
            )
            Text(
                text = "${properties.size} alojamientos guardados",
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (properties.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aún no has guardado viviendas en tus favoritos.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }

        items(properties) { property ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPropertyClick(property) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Miniatura
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏠", fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = property.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$${property.price.toInt()} / mes",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = BlueUEB
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "📍 ${property.campusDistance}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    IconButton(onClick = { onRemoveFavorite(property) }) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Quitar de favoritos",
                            tint = RedUEB
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerHomeScreen(
    ownerEmail: String,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Uni", fontWeight = FontWeight.Black, fontSize = 20.sp, color = BlueUEB)
                        Text("Hogar", fontWeight = FontWeight.Black, fontSize = 20.sp, color = RedUEB)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Propietario", fontSize = 13.sp, color = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    Toast.makeText(context, "Módulo de publicación de inmuebles", Toast.LENGTH_SHORT).show()
                },
                containerColor = RedUEB,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("+ Publicar inmueble", fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Mis Anuncios") },
                    label = { Text("Anuncios", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RedUEB,
                        selectedTextColor = RedUEB
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RedUEB,
                        selectedTextColor = RedUEB
                    )
                )
            }
        }
    ) { paddingValues ->
        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BlueUEB),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text("Panel de Arrendador", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("1 inmueble publicado · 2 solicitudes de visita pendientes", color = Color(0xFFD1D5DB), fontSize = 13.sp)
                        }
                    }
                }

                item {
                    Text("Tus Inmuebles", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Mini Dpto. Los Chasquis", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981)
                                ) {
                                    Text(
                                        "DISPONIBLE",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Canon: $150/mes • Depósito: $100", fontSize = 13.sp, color = BlueUEB)
                            Text("Cerca de la UEB · Guaranda", fontSize = 12.sp, color = TextSecondary)

                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { 
                                        Toast.makeText(context, "Editar anuncio", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = BlueUEB),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Editar")
                                }
                                OutlinedButton(
                                    onClick = { 
                                        Toast.makeText(context, "Estado pausado", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Pausar")
                                }
                            }
                        }
                    }
                }
            }
        } else {
            FullProfileScreen(
                email = ownerEmail,
                roleTitle = "Propietario",
                onLogout = onLogout
            )
        }
    }
}
