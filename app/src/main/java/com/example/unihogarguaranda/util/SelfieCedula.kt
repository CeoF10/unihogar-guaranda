package com.example.unihogarguaranda.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.File

/**
 * Foto del propietario sosteniendo su cédula. Se toma en el registro (o en Mi expediente),
 * se guarda en el teléfono y se envía a Bienestar UEB junto con el expediente.
 */
object SelfieCedula {
    private const val MAX_LADO = 1600

    private fun carpeta(context: Context) = File(context.filesDir, "selfie").apply { mkdirs() }
    private fun captura(context: Context) = File(carpeta(context), "captura.jpg")
    fun archivo(context: Context) = File(carpeta(context), "selfie_cedula.jpg")

    fun existe(context: Context) = archivo(context).let { it.exists() && it.length() > 0 }

    /** Uri donde la app de cámara escribe la foto a resolución completa. */
    fun uriCaptura(context: Context): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", captura(context).apply { delete() })

    /**
     * Convierte la captura en la foto definitiva: endereza según EXIF, reduce a 1600 px
     * y comprime en JPEG. Devuelve false si la cámara no entregó nada.
     */
    fun confirmarCaptura(context: Context): Boolean {
        val origen = captura(context)
        if (!origen.exists() || origen.length() == 0L) return false
        val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(origen.path, opciones)
        var muestra = 1
        while (maxOf(opciones.outWidth, opciones.outHeight) / (muestra * 2) >= MAX_LADO) muestra *= 2
        val bitmap = BitmapFactory.decodeFile(origen.path, BitmapFactory.Options().apply { inSampleSize = muestra }) ?: return false
        val grados = when (ExifInterface(origen.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        val escala = minOf(1f, MAX_LADO.toFloat() / maxOf(bitmap.width, bitmap.height))
        val matriz = Matrix().apply { postRotate(grados); postScale(escala, escala) }
        val final = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
        archivo(context).outputStream().use { final.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        origen.delete()
        return true
    }

    fun vistaPrevia(context: Context): Bitmap? =
        if (existe(context)) BitmapFactory.decodeFile(archivo(context).path, BitmapFactory.Options().apply { inSampleSize = 4 }) else null

    fun borrar(context: Context) {
        archivo(context).delete()
    }
}
