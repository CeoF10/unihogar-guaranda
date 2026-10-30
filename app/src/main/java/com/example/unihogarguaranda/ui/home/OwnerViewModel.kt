package com.example.unihogarguaranda.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unihogarguaranda.data.ExpedienteResponse
import com.example.unihogarguaranda.data.InmuebleResponse
import com.example.unihogarguaranda.data.UniHogarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OwnerUiState(
    val anuncios: List<InmuebleResponse> = emptyList(),
    val expediente: ExpedienteResponse? = null,
    val cargando: Boolean = true,
    val error: String? = null
) {
    val visibles get() = anuncios.count { it.estado == "DISPONIBLE" }
    val enRevision get() = anuncios.count { it.estado == "EN_AUDITORIA" }
    val borradores get() = anuncios.count { it.estado == "BORRADOR" || it.estado == "PAUSADO" }
}

/** Estado del panel del propietario (RF03 y RF04): sus anuncios y su expediente. */
class OwnerViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UniHogarRepository(application)
    private val _ui = MutableStateFlow(OwnerUiState())
    val ui: StateFlow<OwnerUiState> = _ui.asStateFlow()

    init {
        recargar()
    }

    fun recargar() {
        viewModelScope.launch {
            val anuncios = repo.misInmuebles()
            val expediente = repo.expediente()
            _ui.update {
                it.copy(
                    anuncios = anuncios.getOrDefault(emptyList()).sortedByDescending { a -> a.id },
                    expediente = expediente.getOrNull(),
                    cargando = false,
                    error = anuncios.exceptionOrNull()?.message
                )
            }
        }
    }

    fun errorMostrado() = _ui.update { it.copy(error = null) }
}
