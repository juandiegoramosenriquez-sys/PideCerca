# PideCerca

Plataforma web y móvil para pedidos anticipados en puestos de comida rápida. El cliente pide y paga desde su celular, el negocio recibe el pedido al instante y avisa cuando está listo para recoger.

## Partes del proyecto

| Carpeta | Qué es | Tecnología | Quién la usa |
|---------|--------|------------|--------------|
| `backend/` | API REST central | Java 21, Spring Boot 4, JWT, PostgreSQL | Todas las aplicaciones |
| `web/negocio/` | Panel del negocio | React 19 + Vite | Dueños y dueñas de los puestos |
| `web/admin/` | Panel del administrador | Django 6 | Administrador de la plataforma |
| `android/` | App del cliente | Kotlin + Jetpack Compose | Clientes |
| `database/` | Script SQL y datos de prueba | PostgreSQL | Referencia |
| `docs/` | Requisitos, casos de uso, arquitectura, interfaces y API | Markdown | Equipo |

## Cómo ejecutar

### Requisitos

- JDK 21 (IntelliJ IDEA lo trae)
- Node.js 20 o superior
- Python 3.12 o superior
- Android Studio con un emulador

### 1. Backend (puerto 8080)

La contraseña de la base de datos va en `backend/secrets.properties`, que no se sube a GitHub. Si no lo tienes, copia `backend/secrets.example.properties` con ese nombre y completa los datos de Supabase.

Desde IntelliJ: abre la carpeta `backend` y ejecuta `BackendApplication`.

Desde la terminal:

```bash
cd backend
mvnw spring-boot:run
```

Para probar sin internet, con una base de datos local H2:

```bash
cd backend
mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

La primera vez se crean solas las cuentas y datos de prueba (ver tabla abajo).

### 2. Web del negocio, React (puerto 5173)

```bash
cd web/negocio
npm install
npm run dev
```

Abre http://localhost:5173

### 3. Panel del administrador, Django (puerto 8000)

```bash
cd web/admin
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
python manage.py runserver 8000
```

Abre http://localhost:8000

### 4. App Android

1. Abre la carpeta `android` en Android Studio.
2. Espera a que termine la sincronización de Gradle.
3. Ejecuta la app en el emulador.

El emulador llega al backend de tu PC por `http://10.0.2.2:8080/api/`. Para usar un **celular real**, cambia `pidecerca.apiUrl` en `android/gradle.properties` por la IP de tu PC en la red wifi, por ejemplo `http://192.168.1.50:8080/api/`.

## Cuentas de prueba

| Rol | Correo | Contraseña | Dónde entra |
|-----|--------|------------|-------------|
| Administrador | admin@pidecerca.com | admin123 | Panel Django |
| Negocio | negocio@pidecerca.com | negocio123 | Web React |
| Cliente | cliente@pidecerca.com | cliente123 | App Android |

Hay más cuentas en [database/README.md](database/README.md).

## Prueba rápida del flujo completo

1. En la app Android entra como cliente, elige "Hamburguesería Don Lucho", agrega productos y paga.
2. En la web del negocio suena un aviso y aparece el pedido en **Nuevos**. Toca **Aceptar**.
3. La app del cliente cambia a **Preparando** y muestra el tiempo estimado.
4. En la web toca **Ya está listo**. Al cliente le llega la notificación **¡Tu pedido está listo!**
5. Cuando el cliente llega, toca **Entregado al cliente**.
6. En el panel Django el administrador ve el pedido y las ventas en el resumen.

## Diseño

- Color de marca a elegir entre 5 colores eléctricos (morado por defecto), con modo claro, oscuro o automático.
- Colores fijos por estado: amarillo = nuevo, azul = cocinando, verde = listo, rojo = cancelado.
- La web del negocio está pensada para usarse desde el celular en puestos callejeros: menú arriba, botones grandes, un solo botón por paso y aviso con sonido cuando llega un pedido.

## Documentación

- [Requisitos](docs/Requisitos/requisitos.md)
- [Casos de uso](docs/CasoDeUso/casos-uso.md)
- [Arquitectura](docs/Arquitectura/arquitectura.md)
- [Base de datos](docs/BaseDeDatos/base-datos.md)
- [Interfaces](docs/Interfaces/interfaces.md)
- [API REST](docs/API/api.md)
