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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import com.example.unihogarguaranda.data.ApiClient
import com.example.unihogarguaranda.data.NuevoInmuebleRequest
import com.example.unihogarguaranda.data.Session
import com.example.unihogarguaranda.data.toProperty
import com.example.unihogarguaranda.data.FiltroCatalogo
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.io.IOException
import com.example.unihogarguaranda.model.Property
import com.example.unihogarguaranda.ui.screens.FilterBottomSheet
import com.example.unihogarguaranda.ui.screens.FullProfileScreen
import com.example.unihogarguaranda.ui.screens.InteractiveMapScreen
import com.example.unihogarguaranda.ui.screens.MessagesScreen
import com.example.unihogarguaranda.R
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.unihogarguaranda.ui.screens.PropertyDetailScreen
import com.example.unihogarguaranda.ui.theme.*

data class CategoryItem(val id: String, val title: String, val color: Color, val icon: androidx.compose.ui.graphics.vector.ImageVector)

val showcaseCategories = listOf(
    CategoryItem("hab", "Habitaciones", RedUEB, Icons.Default.Bed),
    CategoryItem("dep", "Departamentos", BlueUEB, Icons.Default.Apartment),
    CategoryItem("cas", "Casas", Color(0xFF10B981), Icons.Default.Home)
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
    var showMessages by remember { mutableStateOf(false) }

    // Catálogo de inmuebles verificados (datos semilla, alineados con init.sql)
    var propertyList by remember { mutableStateOf<List<Property>>(emptyList()) }
    val scope = rememberCoroutineScope()

    // RF06: filtros combinados que se envían a la API
    var filtro by remember { mutableStateOf(FiltroCatalogo()) }
    var sinResultados by remember { mutableStateOf<String?>(null) }
    var totalCatalogo by remember { mutableStateOf(0) }
    var paginaActual by remember { mutableStateOf(1) }
    var favoritosIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    suspend fun pedirPagina(pagina: Int) = ApiClient.service.getInmuebles(
        tipo = filtro.tipo,
        precioMin = filtro.precioMin,
        precioMax = filtro.precioMax,
        servicios = filtro.servicios.takeIf { it.isNotEmpty() }?.joinToString(","),
        page = pagina
    ).body()

    // RF06: la siguiente página se pide solo cuando el usuario la necesita
    fun cargarMas() {
        scope.launch {
            try {
                val siguiente = pedirPagina(paginaActual + 1) ?: return@launch
                paginaActual = siguiente.page
                propertyList = propertyList + siguiente.items.map { it.toProperty(isFavorite = it.id in favoritosIds) }
            } catch (e: IOException) {
                Toast.makeText(context, "No se pudo cargar más", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Catálogo real desde la API (solo inmuebles verificados) + favoritos del usuario
    LaunchedEffect(filtro) {
        try {
            val pagina = pedirPagina(1)
            val auth = Session.bearer(context)
            favoritosIds = auth?.let { ApiClient.service.getFavoritos(it).body() }?.map { it.id }?.toSet() ?: emptySet()
            propertyList = pagina?.items?.map { it.toProperty(isFavorite = it.id in favoritosIds) } ?: emptyList()
            totalCatalogo = pagina?.total ?: 0
            paginaActual = 1
            sinResultados = pagina?.mensaje
        } catch (e: IOException) {
            Toast.makeText(context, "No se pudo cargar el catálogo. Verifique la API.", Toast.LENGTH_LONG).show()
        }
    }

    // Marca el favorito al instante y lo guarda en la API
    fun setFavorite(prop: Property, value: Boolean) {
        propertyList = propertyList.map { if (it.id == prop.id) it.copy(isFavorite = value) else it }
        scope.launch {
            val auth = Session.bearer(context) ?: return@launch
            try {
                if (value) ApiClient.service.addFavorito(auth, prop.id)
                else ApiClient.service.removeFavorito(auth, prop.id)
            } catch (e: IOException) {
                Toast.makeText(context, "No se pudo guardar el favorito", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showMessages) {
        MessagesScreen(onBack = { showMessages = false })
        return
    }

    // Si está viendo el detalle de una vivienda
    if (selectedPropertyDetail != null) {
        PropertyDetailScreen(
            property = selectedPropertyDetail!!,
            onBack = { selectedPropertyDetail = null },
            onToggleFavorite = { prop ->
                setFavorite(prop, !prop.isFavorite)
                selectedPropertyDetail = selectedPropertyDetail?.copy(isFavorite = !prop.isFavorite)
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
                filtro = FiltroCatalogo(
                    precioMin = minP.toDouble(),
                    precioMax = maxP.toDouble(),
                    tipo = type,
                    servicios = services.toList()
                )
                showFilterSheet = false
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
                    IconButton(onClick = { showMessages = true }) {
                        BadgedBox(badge = { Badge(containerColor = RedUEB) }) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = "Notificaciones",
                                tint = BlueUEB
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
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
                onToggleFavorite = { prop -> setFavorite(prop, !prop.isFavorite) },
                total = totalCatalogo,
                emptyMessage = sinResultados,
                onLoadMore = ::cargarMas
            )
            1 -> InteractiveMapScreen(
                properties = propertyList,
                onFilterClick = { showFilterSheet = true },
                onPropertySelect = { selectedPropertyDetail = it },
                modifier = Modifier.padding(paddingValues)
            )
            2 -> FavoritesTabContent(
                paddingValues = paddingValues,
                properties = propertyList.filter { it.isFavorite },
                onPropertyClick = { selectedPropertyDetail = it },
                onRemoveFavorite = { prop -> setFavorite(prop, false) }
            )
            3 -> FullProfileScreen(
                email = studentEmail,
                roleTitle = "Estudiante",
                onLogout = onLogout,
                modifier = Modifier.padding(paddingValues),
                onOptionClick = { option ->
                    when (option) {
                        "Mensajes" -> showMessages = true
                        "Mis favoritos" -> selectedTab = 2
                        else -> Toast.makeText(context, "$option (próximamente)", Toast.LENGTH_SHORT).show()
                    }
                }
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
    onToggleFavorite: (Property) -> Unit,
    total: Int = properties.size,
    emptyMessage: String? = null,
    onLoadMore: (() -> Unit)? = null
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
                            Icon(
                                imageVector = category.icon,
                                contentDescription = category.title,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
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
                Box {
                    Image(
                        painter = painterResource(R.drawable.campus_ueb),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.horizontalGradient(
                                    0f to BlueUEB,
                                    0.55f to BlueUEB.copy(alpha = 0.75f),
                                    1f to BlueUEB.copy(alpha = 0.15f)
                                )
                            )
                    )
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 26.dp),
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
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Sección Inmuebles en Guaranda
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inmuebles en Guaranda",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "$total disponibles",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        if (properties.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BackgroundLight,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = emptyMessage ?: "No hay inmuebles publicados aún",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "El catálogo de alojamientos se habilitará en las siguientes entregas cuando los propietarios publiquen sus ofertas.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
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
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(42.dp)
                            )
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
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = RedUEB,
                                    modifier = Modifier.size(14.dp)
                                )
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

        if (onLoadMore != null && properties.size < total) {
            item {
                OutlinedButton(onClick = onLoadMore, modifier = Modifier.fillMaxWidth()) {
                    Text("Cargar más (${total - properties.size} restantes)")
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
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(32.dp)
                        )
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = RedUEB,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = property.campusDistance,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
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
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }
    var showRegisterWizard by remember { mutableStateOf(false) }
    var hasSubmittedProperty by remember { mutableStateOf(false) }
    var showMisPublicaciones by remember { mutableStateOf(false) }
    var showMiExpediente by remember { mutableStateOf(false) }

    if (showMiExpediente) {
        MiExpedienteScreen(onBack = { showMiExpediente = false })
        return
    }

    if (showMisPublicaciones) {
        MisPublicacionesScreen(onBack = { showMisPublicaciones = false })
        return
    }

    if (showRegisterWizard) {
        com.example.unihogarguaranda.ui.screens.RegisterPropertyWizardScreen(
            onDismiss = { showRegisterWizard = false },
            onPropertySubmitted = { draft ->
                scope.launch {
                    val auth = Session.bearer(context) ?: return@launch
                    try {
                        val response = ApiClient.service.crearInmueble(auth, draft)
                        if (response.isSuccessful) {
                            showRegisterWizard = false
                            hasSubmittedProperty = true
                            Toast.makeText(
                                context,
                                "Borrador guardado. Suba las fotos y publíquelo en Mis publicaciones.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val message = ApiClient.errorMessage(response.errorBody()?.string(), "No se pudo registrar el inmueble")
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: IOException) {
                        Toast.makeText(context, "Sin conexión con la API UniHogar", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
        return
    }

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
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = RedUEB
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    showRegisterWizard = true
                },
                containerColor = RedUEB,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("+ Registrar inmueble por pasos", fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
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
                            Text("0 inmuebles publicados · 0 solicitudes pendientes", color = Color(0xFFD1D5DB), fontSize = 13.sp)
                        }
                    }
                }

                // ============================================================
                // MODULO DE AUDITORIA Y SEGURIDAD JURIDICA (CANTÓN GUARANDA)
                // ============================================================
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Gavel,
                                        contentDescription = null,
                                        tint = BlueUEB,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Acreditación Jurídica",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = BlueUEB
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFEF3C7)
                                ) {
                                    Text(
                                        text = "EN AUDITORÍA",
                                        color = Color(0xFF92400E),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Expediente en verificación por Bienestar Estudiantil UEB y Catastro Municipal:",
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cédula ecuatoriana validada con Registro Civil (Módulo 10)", fontSize = 11.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Título de dominio / Predial en cotejo con GAD Guaranda", fontSize = 11.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Planilla de servicio básico de Guaranda adjunta", fontSize = 11.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Nota: La publicación de inmuebles se habilitará una vez aprobado el dictamen legal para proteger la seguridad de los estudiantes.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                lineHeight = 15.sp
                            )
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
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = BackgroundLight,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Apartment,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Aún no tienes inmuebles publicados",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "La publicación y gestión de inmuebles estará activa en la siguiente fase del proyecto.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        } else {
            FullProfileScreen(
                email = ownerEmail,
                roleTitle = "Propietario",
                onLogout = onLogout,
                modifier = Modifier.padding(paddingValues),
                onOptionClick = { option ->
                    if (option == "Mis publicaciones") showMisPublicaciones = true
                    else if (option == "Mi expediente") showMiExpediente = true
                    else Toast.makeText(context, "$option (próximamente)", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
