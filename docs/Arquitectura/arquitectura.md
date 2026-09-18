# Arquitectura del Sistema PideCerca

## 1. Descripción

PideCerca utiliza una arquitectura en la que las aplicaciones de los usuarios se comunican con un backend central, encargado de procesar la información, aplicar la lógica del sistema y conectarse con la base de datos.

## 2. Componentes del sistema

### Aplicación Android
Utilizada por los clientes para buscar negocios, consultar productos, realizar pedidos, efectuar pagos y consultar el estado de sus pedidos.

**Tecnologías:**
- Android
- Kotlin
- Jetpack Compose

### Aplicación Web
Utilizada por los negocios y administradores para gestionar la información del sistema.

**Tecnologías:**
- HTML
- CSS
- JavaScript
- Django

### Backend
Encargado de procesar las solicitudes, aplicar la lógica del sistema y proporcionar la API REST.

**Tecnologías:**
- Java
- Spring Boot
- API REST

### Base de Datos
Encargada de almacenar la información del sistema.

**Datos principales:**
- Usuarios
- Negocios
- Categorías
- Productos
- Pedidos
- Detalles de pedidos
- Pagos
- Notificaciones

### Servicios externos
Servicios que complementan el funcionamiento del sistema.

- Mapas y ubicación
- Pagos
- Notificaciones

## 3. Comunicación entre componentes

La comunicación principal del sistema seguirá el siguiente flujo:

**Aplicación Android / Aplicación Web → API REST → Backend Spring Boot → Base de Datos**

El Backend será el encargado de recibir las solicitudes, procesarlas y devolver la información correspondiente.

## 4. Herramientas utilizadas

- **Lucidchart:** elaboración de diagramas de arquitectura y diseño.
- **Android Studio:** desarrollo de la aplicación Android.
- **IntelliJ IDEA:** desarrollo del Backend con Java y Spring Boot.
- **Visual Studio Code:** desarrollo de la aplicación Web y documentación.
- **Git:** control de versiones.
- **GitHub:** almacenamiento y gestión del código fuente.

## 5. Diagrama de arquitectura

El siguiente diagrama representa la arquitectura general del sistema PideCerca:

![Diagrama de arquitectura de PideCerca](![alt text](arquitectura-pidecerca.png))

## 6. Resumen de la arquitectura

PideCerca estará compuesto por una aplicación móvil para los clientes, una aplicación web para negocios y administradores, un Backend desarrollado con Spring Boot y una base de datos central.

Las aplicaciones se comunicarán con el Backend mediante una API REST. El Backend procesará las solicitudes y administrará la comunicación con la base de datos y los servicios externos.

