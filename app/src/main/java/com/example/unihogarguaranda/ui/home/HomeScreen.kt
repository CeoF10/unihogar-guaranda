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
import kotlinx.coroutines.delay
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import coil3.compose.AsyncImage
import androidx.compose.ui.text.style.TextOverflow
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
    CategoryItem("Habitación", "Habitaciones", RedUEB, Icons.Default.Bed),
    CategoryItem("Departamento", "Departamentos", BlueUEB, Icons.Default.Apartment),
    CategoryItem("Casa", "Casas", Color(0xFF10B981), Icons.Default.Home)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    studentEmail: String,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var searchText by remember { mutableStateOf("") }
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
        distanciaMaxKm = filtro.distanciaMaxKm,
        q = filtro.texto,
        page = pagina
    ).body()

    // Buscador: espera a que el usuario deje de escribir antes de consultar la API
    LaunchedEffect(searchText) {
        delay(400)
        filtro = filtro.copy(texto = searchText.trim().ifEmpty { null })
    }

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
            onContactOwner = { prop -> contactarPropietario(context, prop) }
        )
        return
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            filtroActual = filtro,
            onDismiss = { showFilterSheet = false },
            onApply = { nuevo ->
                filtro = nuevo
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
                searchText = searchText,
                onSearchChange = { searchText = it },
                selectedType = filtro.tipo,
                onSelectType = { tipo -> filtro = filtro.copy(tipo = if (filtro.tipo == tipo) null else tipo) },
                activeFilters = filtro.activos,
                onClearFilters = { filtro = FiltroCatalogo(texto = filtro.texto) },
                onBannerClick = { selectedTab = 1 },
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
    searchText: String,
    onSearchChange: (String) -> Unit,
    selectedType: String?,
    onSelectType: (String) -> Unit,
    activeFilters: Int,
    onClearFilters: () -> Unit,
    onBannerClick: () -> Unit,
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
                OutlinedTextField(
                    value = searchText,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Buscar por zona o nombre...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = if (searchText.isNotEmpty()) {
                        {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Borrar búsqueda", tint = TextSecondary)
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        unfocusedBorderColor = DividerColor,
                        focusedBorderColor = BlueUEB
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                BadgedBox(
                    badge = {
                        if (activeFilters > 0) Badge(containerColor = RedUEB) { Text("$activeFilters") }
                    }
                ) {
                    IconButton(
                        onClick = onFilterClick,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (activeFilters > 0) BlueUEB else Color.White)
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Filtros",
                            tint = if (activeFilters > 0) Color.White else BlueUEB
                        )
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
                    val isSelected = selectedType == category.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectType(category.id) }
                            .padding(horizontal = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(18.dp))
                                // Sin selección todas se ven activas; con selección se atenúan las demás
                                .background(if (selectedType == null || isSelected) category.color else category.color.copy(alpha = 0.25f)),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBannerClick() }
            ) {
                Box {
                    Image(
                        painter = painterResource(R.drawable.campus_ueb_banner),
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
                                text = "Mira en el mapa las viviendas cerca de la UEB.",
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

        // Filtros activos con opción de limpiarlos
        if (activeFilters > 0) {
            item {
                AssistChip(
                    onClick = onClearFilters,
                    label = { Text("$activeFilters filtro(s) activo(s) · Limpiar") },
                    leadingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        // Listado de tarjetas
        items(properties, key = { it.id }) { property ->
            PropertyCard(
                property = property,
                onClick = { onPropertyClick(property) },
                onToggleFavorite = { onToggleFavorite(property) }
            )
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
                text = if (properties.size == 1) "1 alojamiento guardado" else "${properties.size} alojamientos guardados",
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
                        FotoInmueble(property, iconSize = 32)
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

// Foto principal del inmueble o un marcador si todavía no tiene fotos
@Composable
fun FotoInmueble(property: Property, iconSize: Int = 42) {
    if (property.fotoUrls.isNotEmpty()) {
        AsyncImage(
            model = property.fotoUrls.first(),
            contentDescription = property.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFFE8EEF7), Color(0xFFD5DFEC)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (property.type) {
                    "Habitación" -> Icons.Default.Bed
                    "Departamento" -> Icons.Default.Apartment
                    else -> Icons.Default.Home
                },
                contentDescription = null,
                tint = BlueUEB.copy(alpha = 0.45f),
                modifier = Modifier.size(iconSize.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PropertyCard(property: Property, onClick: () -> Unit, onToggleFavorite: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                FotoInmueble(property)

                // Etiquetas sobre la foto: tipo y verificación
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(shape = RoundedCornerShape(8.dp), color = BlueUEB) {
                        Text(
                            property.type,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF047857)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Verificado UEB", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.95f))
                ) {
                    Icon(
                        imageVector = if (property.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (property.isFavorite) "Quitar de favoritos" else "Guardar en favoritos",
                        tint = RedUEB,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (property.fotoUrls.size > 1) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                    ) {
                        Text(
                            "${property.fotoUrls.size} fotos",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = property.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = RedUEB, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = listOf(property.location, property.campusDistance.takeIf { it.isNotEmpty() }?.let { "$it del campus" })
                            .filterNotNull()
                            .filter { it.isNotEmpty() }
                            .joinToString(" · "),
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    (listOf("${property.beds} hab.") + property.services.take(3)).forEach { etiqueta ->
                        Surface(shape = RoundedCornerShape(8.dp), color = ChipBackground) {
                            Text(
                                etiqueta,
                                fontSize = 11.sp,
                                color = BlueUEB,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (property.services.size > 3) {
                        Text("+${property.services.size - 3}", fontSize = 11.sp, color = TextSecondary,
                            modifier = Modifier.padding(vertical = 4.dp))
                    }
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$${property.price.toInt()}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = BlueUEB
                    )
                    Text(" / mes", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 3.dp))
                    Spacer(modifier = Modifier.weight(1f))
                    property.costos?.let { costos ->
                        Text(
                            text = costos.totalMensual?.let { "Total $${it.toInt()}" } ?: "Total por confirmar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (costos.totalMensual == null) RedUEB else TextSecondary,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

// RF12 básico: abre WhatsApp con un mensaje sobre el anuncio; si no hay WhatsApp, abre el marcador
fun contactarPropietario(context: Context, property: Property) {
    val telefono = property.contact?.filter { it.isDigit() }
    if (telefono.isNullOrEmpty()) {
        Toast.makeText(context, "Este anuncio no tiene un contacto registrado", Toast.LENGTH_SHORT).show()
        return
    }
    // Números de Ecuador: 09XXXXXXXX -> 5939XXXXXXXX
    val internacional = if (telefono.startsWith("0")) "593" + telefono.drop(1) else telefono
    val mensaje = Uri.encode("Hola, vi su anuncio \"${property.title}\" en UniHogar Guaranda. ¿Sigue disponible?")
    val whatsapp = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$internacional?text=$mensaje"))
    try {
        context.startActivity(whatsapp)
    } catch (e: android.content.ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefono")))
    }
}
