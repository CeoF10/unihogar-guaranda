# UniHogar Guaranda 🏠📱

> **Propuesta técnica y arquitectónica para el Producto Mínimo Viable (MVP) móvil de arriendos estudiantiles.**  
> Universidad Estatal de Bolívar — Carrera de Software — Sexto Ciclo "A".

---

## 👥 Integrantes
* **Sebastian Velastegui**
* **Neicer Jimenez**

**Docente:** Ing. Patricia León Monar, Mgs.  
**Asignatura:** Desarrollo de Software Móvil

---

## 📌 Descripción del Proyecto
La búsqueda de vivienda estudiantil en Guaranda suele ser un proceso disperso e informal. **UniHogar Guaranda** busca centralizar la oferta de habitaciones, departamentos y casas cerca de los campus de la **Universidad Estatal de Bolívar (UEB)**, bajo un modelo similar al de Airbnb.

* **Estudiantes:** Validación mediante correo institucional (`@ueb.edu.ec`), catálogo georreferenciado con mapas, filtros avanzados, comparador de hasta 3 viviendas y coordinación de visitas.
* **Propietarios:** Registro con correo personal, publicación y administración de inmuebles, subida de fotos, especificación de costos transparentes (arriendo, servicios y garantía) y gestión de visitas.

---

## 🛠️ Stack Tecnológico & Arquitectura

Arquitectura Cliente-Servidor estructurada en capas independientes:

| Capa / Componente | Tecnología | Responsabilidad |
| :--- | :--- | :--- |
| **Frontend Móvil** | **Kotlin + Jetpack Compose** (Material 3) | UI declarativa nativa Android, MVVM, StateFlow y persistencia local con **Room** (caché offline). |
| **Backend API REST** | **Spring Boot 3 + Kotlin** | Endpoints RESTful, seguridad con **JWT**, control de acceso por roles y lógica de negocio. |
| **Base de Datos** | **PostgreSQL 16** | Modelo relacional 3FN con soporte espacial para georreferenciación. |
| **Servicios Externos** | **Google Maps SDK for Android** | Renderizado de mapas interactivos, marcadores de inmuebles y campus universitarios. |
| **Notificaciones** | **Firebase Cloud Messaging (FCM)** | Alertas push sobre solicitudes y estados de visita. |
| **Contacto** | **WhatsApp API / Intent** | Apertura de chat directo con mensaje contextual del anuncio. |

---

## 🚀 Funcionalidades Principales

- [x] **Cuentas y perfiles:** Roles diferenciados (Estudiante / Propietario / Admin).
- [x] **Gestión de Inmuebles:** Publicación, edición, pausa y marcado de arriendo.
- [x] **Catálogo con Filtros:** Por rango de precio, tipo de inmueble, servicios y distancia al campus.
- [x] **Mapa Interactivo:** Visualización de viviendas y sedes universitarias con selector manual de coordenadas.
- [x] **Costo Transparente:** Diferenciación clara entre canon mensual, servicios y depósito.
- [x] **Comparador de Viviendas:** Comparativa lado a lado de hasta 3 opciones.
- [x] **Gestión de Visitas:** Solicitud, aceptación, rechazo o reprogramación.
- [x] **Favoritos:** Guardado en local/remoto para acceso rápido.

---

## 📅 Planificación y Entregables (Semanas 7 a 16)

* **Semana 7:** Cierre de alcance, diagramas, bocetos en Figma y pruebas de Google Maps SDK.
* **Semana 8:** Setup de proyectos (Compose + Spring Boot + PostgreSQL), autenticación y JWT.
* **Semana 9:** Permisos, gestión de inmuebles básica y subida de fotografías.
* **Semana 10:** Catálogo interactivo, paginación y filtros dinámicos.
* **Semana 11:** Integración de Google Maps SDK, marcadores y permisos de GPS.
* **Semana 12:** Sistema de favoritos y visualización de costos desglosados.
* **Semana 13:** Comparador de 3 viviendas y módulo de solicitudes de visita.
* **Semana 14:** Pruebas de usuario, reportes y módulo visual complementario (360°).
* **Semana 15:** Congelamiento de código, corrección de bugs y validación de respaldo de BD.
* **Semana 16:** Generación de APK demostrable, informe final y sustentación.

---

## 📄 Licencia
Este proyecto es desarrollado con fines académicos para la Universidad Estatal de Bolívar.
