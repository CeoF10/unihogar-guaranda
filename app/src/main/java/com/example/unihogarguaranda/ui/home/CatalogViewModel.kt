package com.example.unihogarguaranda.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihogarguaranda.data.FiltroCatalogo
import com.example.unihogarguaranda.data.UniHogarRepository
import com.example.unihogarguaranda.data.toProperty
import com.example.unihogarguaranda.model.Property
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUiState(
    val propiedades: List<Property> = emptyList(),
    val favoritas: List<Property> = emptyList(),
    val total: Int = 0,
    val pagina: Int = 1,
    val filtro: FiltroCatalogo = FiltroCatalogo(),
    val busqueda: String = "",
    val mensajeVacio: String? = null,
    val cargando: Boolean = true,
    val error: String? = null
)

/**
 * Estado del catálogo del estudiante (RF06, RF07 y RF09):
 * búsqueda, filtros, paginación y favoritos. La pantalla solo dibuja este estado.
 */
class CatalogViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UniHogarRepository(application)
    private val _ui = MutableStateFlow(CatalogUiState())
    val ui: StateFlow<CatalogUiState> = _ui.asStateFlow()

    private val favoritos: Set<Long> get() = _ui.value.favoritas.map { it.id }.toSet()
    private var busquedaJob: Job? = null
    private var cargaJob: Job? = null

    init {
        cargar()
    }

    // Espera a que el usuario deje de escribir antes de consultar la API
    fun onBusqueda(texto: String) {
        _ui.update { it.copy(busqueda = texto) }
        busquedaJob?.cancel()
        busquedaJob = viewModelScope.launch {
            delay(400)
            aplicarFiltro(_ui.value.filtro.copy(texto = texto.trim().ifEmpty { null }))
        }
    }

    fun aplicarFiltro(filtro: FiltroCatalogo) {
        if (filtro == _ui.value.filtro) return
        _ui.update { it.copy(filtro = filtro) }
        cargar()
    }

    // Tocar una categoría filtra por ese tipo; tocarla otra vez quita el filtro
    fun alternarTipo(tipo: String) {
        val actual = _ui.value.filtro
        aplicarFiltro(actual.copy(tipo = if (actual.tipo == tipo) null else tipo))
    }

    fun limpiarFiltros() = aplicarFiltro(FiltroCatalogo(texto = _ui.value.filtro.texto))

    fun recargar() = cargar()

    private fun cargar() {
        cargaJob?.cancel()
        cargaJob = viewModelScope.launch {
            _ui.update { it.copy(cargando = true) }
            val favs = repo.favoritos().map { it.toProperty(isFavorite = true) }
            _ui.update { it.copy(favoritas = favs) }
            repo.catalogo(_ui.value.filtro, 1)
                .onSuccess { p ->
                    _ui.update {
                        it.copy(
                            propiedades = p.items.map { i -> i.toProperty(isFavorite = i.id in favoritos) },
                            total = p.total,
                            pagina = 1,
                            mensajeVacio = p.mensaje,
                            cargando = false
                        )
                    }
                }
                .onFailure { e -> _ui.update { it.copy(cargando = false, error = e.message) } }
        }
    }

    // RF06: la siguiente página se pide solo cuando el usuario la necesita
    fun cargarMas() {
        val estado = _ui.value
        if (estado.propiedades.size >= estado.total) return
        viewModelScope.launch {
            repo.catalogo(estado.filtro, estado.pagina + 1)
                .onSuccess { p ->
                    _ui.update {
                        it.copy(
                            propiedades = it.propiedades + p.items.map { i -> i.toProperty(isFavorite = i.id in favoritos) },
                            pagina = p.page
                        )
                    }
                }
                .onFailure { e -> _ui.update { it.copy(error = e.message) } }
        }
    }

    // RF09: se marca al instante y, si la API falla, se revierte
    fun alternarFavorito(propiedad: Property) {
        val nuevo = !propiedad.isFavorite
        marcar(propiedad, nuevo)
        viewModelScope.launch {
            repo.setFavorito(propiedad.id, nuevo).onFailure { e ->
                marcar(propiedad, !nuevo)
                _ui.update { it.copy(error = e.message) }
            }
        }
    }

    fun quitarFavorito(propiedad: Property) {
        if (propiedad.isFavorite) alternarFavorito(propiedad)
    }

    private fun marcar(propiedad: Property, favorito: Boolean) {
        _ui.update { s ->
            s.copy(
                propiedades = s.propiedades.map { if (it.id == propiedad.id) it.copy(isFavorite = favorito) else it },
                favoritas = if (favorito) s.favoritas.filter { it.id != propiedad.id } + propiedad.copy(isFavorite = true)
                else s.favoritas.filter { it.id != propiedad.id }
            )
        }
    }

    fun errorMostrado() = _ui.update { it.copy(error = null) }
}
