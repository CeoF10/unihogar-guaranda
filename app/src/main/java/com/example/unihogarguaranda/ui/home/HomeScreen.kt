package com.example.unihogarguaranda.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unihogarguaranda.ui.theme.*

data class CategoryItem(val id: String, val title: String, val color: Color, val iconText: String)
data class ShowcaseProperty(
    val id: Long,
    val title: String,
    val location: String,
    val specs: String,
    val price: Double,
    val distance: String,
    val isFeatured: Boolean = false
)

val showcaseCategories = listOf(
    CategoryItem("hab", "Habitaciones", RedUEB, "🛏️"),
    CategoryItem("dep", "Departamentos", BlueUEB, "🏢"),
    CategoryItem("cas", "Casas", Color(0xFF10B981), "🏡")
)

val showcaseProperties = listOf(
    ShowcaseProperty(
        id = 1,
        title = "Habitación amoblada",
        location = "Cerca de la UEB · Guaranda",
        specs = "1 hab. · Baño compartido · WiFi",
        price = 150.0,
        distance = "1.2 km",
        isFeatured = true
    ),
    ShowcaseProperty(
        id = 2,
        title = "Departamento en el centro",
        location = "Av. Guayaquil y Convención · Guaranda",
        specs = "2 hab. · 1 baño · WiFi",
        price = 250.0,
        distance = "800 m",
        isFeatured = false
    ),
    ShowcaseProperty(
        id = 3,
        title = "Casa compartida estudiantes",
        location = "Sector Alpachaca · Guaranda",
        specs = "3 hab. · 2 baños · Lavadora",
        price = 200.0,
        distance = "1.5 km",
        isFeatured = false
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    studentEmail: String,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedCategory by remember { mutableStateOf("hab") }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Uni", fontWeight = FontWeight.Black, fontSize = 20.sp, color = BlueUEB)
                        Text("Hogar", fontWeight = FontWeight.Black, fontSize = 20.sp, color = RedUEB)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notificaciones */ }) {
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
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RedUEB,
                        selectedTextColor = RedUEB,
                        indicatorColor = ChipBackground
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Place, contentDescription = "Mapa") },
                    label = { Text("Mapa", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RedUEB,
                        selectedTextColor = RedUEB,
                        indicatorColor = ChipBackground
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoritos") },
                    label = { Text("Favoritos", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RedUEB,
                        selectedTextColor = RedUEB,
                        indicatorColor = ChipBackground
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RedUEB,
                        selectedTextColor = RedUEB,
                        indicatorColor = ChipBackground
                    )
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> StudentCatalogContent(paddingValues, selectedCategory) { selectedCategory = it }
            1 -> MapTabContent(paddingValues)
            2 -> FavoritesTabContent(paddingValues)
            3 -> ProfileTabContent(paddingValues, studentEmail, "Estudiante", onLogout)
        }
    }
}

@Composable
fun StudentCatalogContent(
    paddingValues: PaddingValues,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Barra de búsqueda con icono
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
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
                            text = "Viviendas verificadas cerca de la UEB y en todo Guaranda.",
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
                    color = BlueUEB
                )
            }
        }

        items(showcaseProperties) { property ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Contenedor de imagen simulada
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(ChipBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏠 Foto del inmueble", color = TextSecondary, fontSize = 14.sp)

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

                        // Icono de favorito
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.85f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("♡", color = RedUEB, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                            text = property.specs,
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
                                    text = property.distance,
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
fun MapTabContent(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🗺️ Vista de Mapa", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BlueUEB)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Inmuebles geolocalizados cerca de la UEB", fontSize = 14.sp, color = TextSecondary)
        }
    }
}

@Composable
fun FavoritesTabContent(paddingValues: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Tus Favoritos", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(showcaseProperties) { property ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(property.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("$${property.price.toInt()} / mes • ${property.distance}", fontSize = 13.sp, color = BlueUEB)
                    }
                    Text("❤️", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun ProfileTabContent(
    paddingValues: PaddingValues,
    email: String,
    roleTitle: String,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(BlueUEB),
            contentAlignment = Alignment.Center
        ) {
            Text("👤", fontSize = 34.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Neicer Jimenez", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
        Text(email, fontSize = 13.sp, color = TextSecondary)

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = BlueUEB,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎓", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Rol Activo", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text(roleTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = RedUEB)
        ) {
            Text("Cerrar sesión", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerHomeScreen(
    ownerEmail: String,
    onLogout: () -> Unit
) {
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
                onClick = { /* Publicar */ },
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

                items(showcaseProperties.take(1)) { property ->
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
                                Text(property.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
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
                            Text("Canon: $${property.price.toInt()}/mes • Depósito: $100", fontSize = 13.sp, color = BlueUEB)
                            Text(property.location, fontSize = 12.sp, color = TextSecondary)

                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { /* Editar */ },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = BlueUEB),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Editar")
                                }
                                OutlinedButton(
                                    onClick = { /* Pausar */ },
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
            ProfileTabContent(paddingValues, ownerEmail, "Propietario", onLogout)
        }
    }
}
