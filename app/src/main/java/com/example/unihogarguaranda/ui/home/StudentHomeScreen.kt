package com.example.unihogarguaranda.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.unihogarguaranda.ui.screens.ChatScreen
import com.example.unihogarguaranda.ui.screens.ComparisonScreen
import com.example.unihogarguaranda.ui.screens.VisitsScreen
import com.example.unihogarguaranda.ui.screens.moneda
import com.example.unihogarguaranda.ui.screens.rememberListingAction
import com.example.unihogarguaranda.ui.screens.rememberVisitPicker
import com.example.unihogarguaranda.data.NuevaVisitaRequest
import com.example.unihogarguaranda.data.ReporteRequest
import com.example.unihogarguaranda.data.toProperty
import com.example.unihogarguaranda.data.Session
import com.example.unihogarguaranda.data.ApiClient
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
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    var selectedPropertyDetail by remember { mutableStateOf<Property?>(null) }
    // Al girar se pierde el objeto, pero se guarda el id para volver a abrir el mismo detalle
    var detailId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showVisits by rememberSaveable { mutableStateOf(false) }
    var showComparison by rememberSaveable { mutableStateOf(false) }
    var comparisonIds by rememberSaveable { mutableStateOf(longArrayOf()) }
    val comparison = remember { mutableStateMapOf<Long, Property>() }
    var showMessages by rememberSaveable { mutableStateOf(false) }
    var chatId by rememberSaveable { mutableStateOf<Long?>(null) }
    var chatTitulo by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val abrirChat: (Property) -> Unit = { prop ->
        scope.launch {
            val auth = Session.bearer(context)
            try {
                val r = auth?.let { ApiClient.service.abrirConversacion(it, prop.id) }
                if (r?.isSuccessful == true) {
                    chatTitulo = prop.title
                    chatId = r.body()?.id
                } else {
                    Toast.makeText(context, ApiClient.errorMessage(r?.errorBody()?.string(), "No se pudo abrir el chat"), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Sin conexión con el servidor UniHogar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Todo el estado del catálogo vive en el ViewModel; la pantalla solo lo dibuja
    val vm: CatalogViewModel = viewModel()
    val ui by vm.ui.collectAsState()
    val elegirFecha = rememberVisitPicker()
    val enviarAccion = rememberListingAction()

    LaunchedEffect(comparisonIds) {
        for (id in comparisonIds) {
            if (comparison[id] == null) {
                comparison[id] = (ui.propiedades + ui.favoritas).firstOrNull { it.id == id }
                    ?: try {
                        ApiClient.service.getInmueble(id).body()?.toProperty()
                    } catch (_: java.io.IOException) {
                        null
                    } ?: continue
            }
        }
    }
    fun alternarComparacion(prop: Property) {
        if (prop.id in comparisonIds) {
            comparisonIds = comparisonIds.filterNot { it == prop.id }.toLongArray()
            comparison.remove(prop.id)
        } else if (comparisonIds.size < 3) {
            comparison[prop.id] = prop
            comparisonIds += prop.id
        } else {
            Toast.makeText(context, "Puedes comparar hasta tres viviendas", Toast.LENGTH_SHORT).show()
        }
    }

    ui.error?.let { mensaje ->
        LaunchedEffect(mensaje) {
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
            vm.errorMostrado()
        }
    }

    LaunchedEffect(selectedPropertyDetail) {
        selectedPropertyDetail?.let { detailId = it.id }
    }
    LaunchedEffect(detailId, ui.propiedades, ui.favoritas) {
        if (selectedPropertyDetail == null && detailId != null) {
            val id = detailId ?: return@LaunchedEffect
            selectedPropertyDetail = (ui.propiedades + ui.favoritas).firstOrNull { it.id == id }
                ?: try {
                    ApiClient.service.getInmueble(id).body()?.toProperty()
                } catch (_: java.io.IOException) {
                    null
                }
        }
    }

    chatId?.let { id ->
        ChatScreen(conversacionId = id, titulo = chatTitulo, onBack = { chatId = null })
        return
    }

    if (showMessages) {
        MessagesScreen(
            onBack = { showMessages = false },
            onAbrirConversacion = { id, titulo -> chatTitulo = titulo; chatId = id }
        )
        return
    }

    if (showVisits) {
        VisitsScreen(propietario = false, onBack = { showVisits = false })
        return
    }
    if (showComparison) {
        ComparisonScreen(
            propiedades = comparisonIds.map { comparison[it] }.filterNotNull(),
            onRemove = ::alternarComparacion,
            onBack = { showComparison = false }
        )
        return
    }

    // Si está viendo el detalle de una vivienda
    if (selectedPropertyDetail != null) {
        PropertyDetailScreen(
            property = selectedPropertyDetail!!,
            onBack = { selectedPropertyDetail = null; detailId = null },
            onToggleFavorite = { prop ->
                vm.alternarFavorito(prop)
                selectedPropertyDetail = selectedPropertyDetail?.copy(isFavorite = !prop.isFavorite)
            },
            onContactOwner = { prop -> contactarPropietario(context, prop) },
            onMessageOwner = abrirChat,
            inComparison = selectedPropertyDetail!!.id in comparisonIds,
            onCompare = ::alternarComparacion,
            onShowComparison = if (comparisonIds.isNotEmpty()) ({ showComparison = true }) else null,
            onVisit = { prop ->
                elegirFecha { fecha ->
                    enviarAccion { auth -> ApiClient.service.solicitarVisita(auth, prop.id, NuevaVisitaRequest(fecha)) }
                }
            },
            onReport = { prop, motivo ->
                enviarAccion { auth -> ApiClient.service.reportar(auth, prop.id, ReporteRequest(motivo)) }
            },
            onCopyContact = { prop ->
                prop.contact?.let { numero ->
                    context.getSystemService(ClipboardManager::class.java)
                        .setPrimaryClip(ClipData.newPlainText("Contacto del anuncio", numero))
                    Toast.makeText(context, "Contacto copiado", Toast.LENGTH_SHORT).show()
                }
            },
            onNavigate = { prop ->
                val destino = Uri.encode("${prop.lat},${prop.lng}")
                val ruta = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destino")
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, ruta))
                } catch (_: android.content.ActivityNotFoundException) {
                    Toast.makeText(context, "No hay una app para abrir la ruta", Toast.LENGTH_SHORT).show()
                }
            },
            onShare = { prop ->
                val texto = listOfNotNull(
                    "${prop.title} · UniHogar Guaranda #${prop.id}",
                    "${moneda(prop.price)} / mes",
                    prop.contact?.let { "Contacto: $it" }
                ).joinToString("\n")
                val intent = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, texto)
                context.startActivity(Intent.createChooser(intent, "Compartir vivienda"))
            }
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

                    }
                },
                actions = {
                    if (comparisonIds.isNotEmpty()) {
                        TextButton(onClick = { showComparison = true }) { Text("Comparar (${comparisonIds.size})") }
                    }
                    IconButton(onClick = { showMessages = true }) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Mensajes",
                            tint = BlueUEB
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
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
                        indicatorColor = RedUEB.copy(alpha = 0.08f)
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
                        indicatorColor = RedUEB.copy(alpha = 0.08f)
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
                        indicatorColor = RedUEB.copy(alpha = 0.08f)
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
                        indicatorColor = RedUEB.copy(alpha = 0.08f)
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
                loading = ui.cargando,
                loadingMore = ui.cargandoMas,
                onLoadMore = vm::cargarMas
            )
            1 -> InteractiveMapScreen(
                properties = ui.propiedades,
                onFilterClick = { showFilterSheet = true },
                onPropertySelect = { selectedPropertyDetail = it },
                modifier = Modifier.padding(paddingValues),
                total = ui.total,
                loadingMore = ui.cargandoMas,
                onLoadMore = vm::cargarMas
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
                        "Visitas" -> showVisits = true
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
    loading: Boolean = false,
    loadingMore: Boolean = false,
    onLoadMore: (() -> Unit)? = null
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("GUARANDA · VIDA UNIVERSITARIA", fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold, color = RedUEB)
                Text("Tu próximo hogar,\nmás cerca.", fontSize = 28.sp, lineHeight = 35.sp, fontWeight = FontWeight.Bold, color = BlueUEB)
                Text("Un espacio para vivir tu nueva etapa.", fontSize = 13.sp, color = TextSecondary)
            }
        }
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
                    placeholder = {
                        Text(
                            "Buscar zona o nombre",
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = if (searchText.isNotEmpty()) {
                        {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Borrar búsqueda", tint = TextSecondary)
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
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

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(showcaseCategories, key = { it.id }) { category ->
                    val selected = selectedType == category.id
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectType(category.id) },
                        label = { Text(category.title, fontSize = 12.sp) },
                        leadingIcon = { Icon(category.icon, null, modifier = Modifier.size(18.dp)) },
                        shape = RoundedCornerShape(14.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White,
                            selectedContainerColor = BlueUEB,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            labelColor = TextSecondary,
                            iconColor = TextSecondary
                        )
                    )
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
                                text = "Cerca de clases.\nCerca de todo.",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Explora alojamientos en el mapa",
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
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
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
                    text = "Encuentra tu espacio",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (total == 1) "1 disponible" else "$total disponibles",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        if (loading) {
            item {
                Row(Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BlueUEB)
                    Spacer(Modifier.width(12.dp))
                    Text("Cargando alojamientos…", color = TextSecondary)
                }
            }
        }
        if (properties.isEmpty() && !loading) {
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
                            text = "Pruebe con otra búsqueda o ajuste los filtros para encontrar alojamientos disponibles.",
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
                OutlinedButton(onClick = onLoadMore, enabled = !loading && !loadingMore, modifier = Modifier.fillMaxWidth()) {
                    Text(if (loadingMore) "Cargando…" else "Cargar más (${total - properties.size} restantes)")
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
