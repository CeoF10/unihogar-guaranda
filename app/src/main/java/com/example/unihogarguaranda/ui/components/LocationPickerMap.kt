package com.example.unihogarguaranda.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.unihogarguaranda.R
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

private val GUARANDA_CENTER = GeoPoint(-1.5921, -79.0035)

/**
 * RF07: mapa donde el propietario toca para ubicar el inmueble sin conceder permiso de GPS.
 */
@Composable
fun LocationPickerMap(
    latitude: Double?,
    longitude: Double?,
    onPick: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            Configuration.getInstance().userAgentValue = "UniHogarGuaranda-Android/1.0"
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)
                controller.setCenter(GUARANDA_CENTER)
                overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        onPick(p.latitude, p.longitude)
                        return true
                    }

                    override fun longPressHelper(p: GeoPoint): Boolean = false
                }))
            }
        },
        update = { mapView ->
            // Se conserva la capa de toques (posición 0) y se redibuja el marcador
            mapView.overlays.removeAll { it is Marker }
            if (latitude != null && longitude != null) {
                val punto = GeoPoint(latitude, longitude)
                mapView.overlays.add(Marker(mapView).apply {
                    position = punto
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = androidx.core.content.ContextCompat.getDrawable(mapView.context, R.drawable.ic_map_pin_selected)
                    title = "Ubicación del inmueble"
                })
                // Si el punto cambió (GPS o toque), el mapa se centra en él
                if (mapView.tag != punto.toString()) {
                    mapView.tag = punto.toString()
                    mapView.controller.animateTo(punto, maxOf(mapView.zoomLevelDouble, 17.0), 600L)
                }
            }
            mapView.invalidate()
        }
    )
}
