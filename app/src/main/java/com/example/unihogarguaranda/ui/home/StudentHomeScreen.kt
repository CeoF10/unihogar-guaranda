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
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
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
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedPropertyDetail by remember { mutableStateOf<Property?>(null) }
    var showMessages by remember { mutableStateOf(false) }

    // Todo el estado del catálogo vive en el ViewModel; la pantalla solo lo dibuja
    val vm: CatalogViewModel = viewModel()
    val ui by vm.ui.collectAsState()

    ui.error?.let { mensaje ->
        LaunchedEffect(mensaje) {
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
            vm.errorMostrado()
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
                vm.alternarFavorito(prop)
                selectedPropertyDetail = selectedPropertyDetail?.copy(isFavorite = !prop.isFavorite)
            },
            onContactOwner = { prop -> contactarPropietario(context, prop) }
        )
        return
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            filtroActual = ui.filtro,
            onDismiss = { showFilterSheet = false },
            onApply = { nuevo ->
                vm.aplicarFiltro(nuevo)
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
                properties = ui.propiedades,
                searchText = ui.busqueda,
                onSearchChange = vm::onBusqueda,
                selectedType = ui.filtro.tipo,
                onSelectType = vm::alternarTipo,
                activeFilters = ui.filtro.activos,
                onClearFilters = vm::limpiarFiltros,
                onBannerClick = { selectedTab = 1 },
                onFilterClick = { showFilterSheet = true },
                onPropertyClick = { selectedPropertyDetail = it },
                onToggleFavorite = vm::alternarFavorito,
                total = ui.total,
                emptyMessage = ui.mensajeVacio,
                onLoadMore = vm::cargarMas
            )
            1 -> InteractiveMapScreen(
                properties = ui.propiedades,
                onFilterClick = { showFilterSheet = true },
                onPropertySelect = { selectedPropertyDetail = it },
                modifier = Modifier.padding(paddingValues)
            )
            2 -> FavoritesTabContent(
                paddingValues = paddingValues,
                properties = ui.favoritas,
                onPropertyClick = { selectedPropertyDetail = it },
                onRemoveFavorite = vm::quitarFavorito
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
