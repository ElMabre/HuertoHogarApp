<p align="center">
  <img src="https://raw.githubusercontent.com/ElMabre/ProyectoHuertoHogar/refs/heads/main/img/huertohogarlogoconfondo.png" width="300" alt="HuertoHogar Logo"/>
</p>

<h1 align="center">HuertoHogar – Aplicación Móvil Android</h1>

<p align="center">
  <b>Asignatura:</b> Desarrollo de Aplicaciones Móviles (DSY1105) · <b>Duoc UC</b><br>
  <b>Docente:</b> [Nombre del Docente]<br>
  <b>Integrantes:</b> Matias Guzman, Felipe Quezada, Danilo Celis
</p>

---

## 1. Descripción del Proyecto

**HuertoHogar** es una aplicación móvil nativa desarrollada en Kotlin con Jetpack Compose, diseñada para conectar a pequeños agricultores locales con consumidores finales. La app ofrece una experiencia completa de e-commerce que permite explorar un catálogo de productos frescos, gestionar un carrito de compras, realizar pedidos y visualizar el historial de compras.

La solución implementa una arquitectura **MVVM (Model-View-ViewModel)** robusta y se conecta a un ecosistema de microservicios distribuidos para la gestión de datos.

## 2. Arquitectura y Backend (Microservicios)

El sistema backend de HuertoHogar está desplegado en la nube utilizando **Amazon Web Services (AWS)**.

* **Infraestructura:** Instancia EC2 (Ubuntu Linux).
* **Servidor Web:** Nginx configurado como Proxy Inverso.
* **Microservicios:** Desarrollados en Spring Boot (Java), ejecutándose en puertos independientes y orquestados por Nginx.
* **Base de Datos:** MySQL (Relacional).

### Endpoints y Conexión
[cite_start]La aplicación Android se conecta a la **IP Pública**: `http://18.211.31.168/`[cite: 1740].

Nginx redirige el tráfico interno a los siguientes microservicios:

| Servicio | Ruta Base | Puerto Interno | Descripción |
| :--- | :--- | :--- | :--- |
| **Auth** | `/api/auth` | `:8081` | Login y Registro de usuarios. |
| **Productos** | `/api/productos` | `:8082` | Catálogo público de productos. |
| **Pedidos** | `/api/pedidos` | `:8083` | Creación y consulta de historial. |
| **Usuarios** | `/api/usuarios` | `:8081` | Actualización de perfil. |

### API Externa
[cite_start]Adicionalmente, la aplicación consume la API pública **TheMealDB** para ofrecer una sección de "Recetas del Mundo", integrando contenido externo sin interferir con los servicios propios[cite: 1729].

## 3. Funcionalidades Clave

* **Autenticación Segura:** Login y Registro con validaciones locales y remotas. Persistencia de sesión (Token JWT) mediante DataStore.
* **Catálogo Dinámico:** Visualización de productos con imágenes, precios y stock en tiempo real.
* **Carrito de Compras:** Lógica local para agregar/quitar ítems y cálculo de totales antes del checkout.
* **Gestión de Pedidos:** * Checkout conectado al microservicio de pedidos.
    * Historial de compras ("Mis Pedidos") con opción de **Cancelar Pedido** (DELETE).
* **Perfil de Usuario:** Edición de datos personales (Dirección, Comuna) y cambio de foto de perfil usando Cámara/Galería.
* **Geolocalización:** Mapa interactivo con Google Maps mostrando las sucursales físicas.

## 4. Stack Tecnológico

* **Lenguaje:** Kotlin
* **UI:** Jetpack Compose (Material Design 3)
* **Arquitectura:** MVVM + Repository Pattern
* **Networking:** Retrofit + OkHttp + Gson
* **Carga de Imágenes:** Coil
* **Asincronía:** Coroutines & StateFlow
* **Testing:** JUnit + Mockk (Cobertura > 80% en ViewModel y Model)

## 5. Pasos para Ejecutar

1.  **Clonar el repositorio:**
    git clone [https://github.com/ElMabre/HuertoHogarApp.git](https://github.com/ElMabre/HuertoHogarApp.git)
2.  **Abrir en Android Studio:** Seleccionar la carpeta raíz del proyecto.
3.  **Sincronizar Gradle:** Esperar a que se descarguen las dependencias.
4.  **Ejecutar:** Seleccionar un emulador o dispositivo físico (Min SDK 26) y presionar "Run".
    * *Nota:* Asegúrese de tener conexión a internet para cargar los datos del EC2 y los mapas.

## 6. Evidencia de Entrega (APK Firmado)

El proyecto ha sido configurado para generar un **APK firmado** en modo `release` utilizando un Keystore seguro.

### 6.1 Configuración del Keystore (.jks)
El archivo `huertohogar-key.jks` se encuentra en la raíz del módulo `app`, configurado en el `build.gradle.kts`.

<p align="center">
  <img src="https://i.ibb.co/hFFkgXyg/archivo-jks.png" width="600" alt="Ubicación del archivo JKS en el proyecto">
</p>

### 6.2 Generación Exitosa del APK
Captura de pantalla de Android Studio confirmando la generación correcta del `app-release.apk`.

<p align="center">
  <img src="https://i.ibb.co/fGq3ZLZg/apk-generado.png" width="600" alt="Build Successful - APK Generado">
</p>

---
<p align="center">
  Desarrollado para Evaluación Parcial 4 - 2025
</p>