# UniHogar Guaranda

App Android para consultar y publicar arriendos estudiantiles cerca de los campus de la Universidad Estatal de Bolívar en Guaranda. Proyecto académico de Sebastian Velastegui y Neicer Jimenez.

[Ver los repositorios del sistema UniHogar](https://github.com/CeoF10/unihogar).

## Qué incluye la app

| Perfil | Pantallas disponibles |
|---|---|
| Estudiante | Catálogo, detalle de inmuebles, mapa, favoritos, comparación de hasta tres viviendas, visitas, reportes, mensajes y perfil |
| Propietario | Anuncios propios, fotos y panorama 360°, solicitudes de visita, expediente, mensajes y perfil |

La app usa Kotlin, Jetpack Compose y Material 3. Consume la API REST de UniHogar con Retrofit, carga imágenes con Coil y muestra mapas con osmdroid. Los servicios de ubicación se integran mediante Google Play Services.

En **Mis publicaciones**, el propietario puede subir un JPEG equirectangular 2:1 de hasta 5 MB.
En la ficha, el estudiante toca **Ver recorrido 360°**, arrastra para mirar alrededor y usa los botones de zoom.
La ficha incluye su galería y su ubicación aunque el inmueble carezca de panorama.
Para cargar las imágenes ficticias de prueba, ejecuta `npm run demo:fotos` en la API y abre el anuncio #1.

## Ejecutar en Android Studio

1. Clona este repositorio y abre su carpeta raíz en Android Studio.
2. Instala Android SDK 37 y sincroniza Gradle. La app requiere Android API 24 o superior en el dispositivo o emulador.
3. Levanta la base de datos y la API siguiendo la [guía del sistema UniHogar](https://github.com/CeoF10/unihogar#entorno-local). Se necesita acceso a esos repositorios para probar los flujos con datos.
4. Ejecuta la configuración `app` en el emulador. La URL predeterminada para la versión de depuración es `http://10.0.2.2:3000/api/`, donde `10.0.2.2` apunta al equipo anfitrión.

En un teléfono físico, para la versión de depuración, conecta el teléfono y tu equipo a la misma red y agrega esta propiedad a `local.properties`. Sustituye la IP del ejemplo por la de tu equipo y conserva la barra final.

```properties
API_BASE_URL=http://192.168.1.20:3000/api/
```

La versión de producción exige HTTPS para conectarse a la API.

Para generar un APK de depuración desde Linux:

```bash
./gradlew :app:assembleDebug
```

## Primera prueba

Con la API activa y una cuenta de prueba, entra como estudiante y abre un inmueble desde el catálogo. Comprueba el detalle, la ubicación en el mapa y la lista de favoritos. Con una cuenta de propietario, comprueba que la pantalla inicial muestre sus anuncios.
