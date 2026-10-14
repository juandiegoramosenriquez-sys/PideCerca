# API REST de PideCerca

URL base local: `http://localhost:8080/api`

Todas las rutas, excepto registro, login y salud, requieren la cabecera:

```
Authorization: Bearer <token>
```

Los errores se devuelven así: `{ "mensaje": "Texto del error" }`, con código 400, 401, 403, 404 o 409.

## Autenticación

| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| POST | `/auth/registro` | Público | Registra un cliente y devuelve `{ token, usuario }` |
| POST | `/auth/login` | Público | `{ correo, password }` → `{ token, usuario }` |
| GET | `/auth/me` | Cualquiera | Usuario de la sesión |
| GET | `/salud` | Público | Comprueba que el backend está encendido |

## Usuarios

| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| GET / PUT | `/usuarios/me` | Cualquiera | Ver o editar mi perfil (nombre, apellido, teléfono) |
| PUT | `/usuarios/me/password` | Cualquiera | `{ passwordActual, nuevaPassword }` |
| GET | `/usuarios` | ADMIN | Lista de usuarios |
| POST | `/usuarios` | ADMIN | Crear usuario con rol |
| PUT | `/usuarios/{id}` | ADMIN | Editar usuario |
| PUT | `/usuarios/{id}/activo?valor=true` | ADMIN | Activar o desactivar |
| DELETE | `/usuarios/{id}` | ADMIN | Eliminar (solo si no tiene pedidos ni negocio) |

## Negocios

| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| GET | `/negocios?q=&tipo=&lat=&lng=` | Cualquiera | Negocios habilitados, ordenados por abiertos y cercanía (`distanciaMetros`) |
| GET | `/negocios/todos` | ADMIN | Todos los negocios, también los deshabilitados |
| GET | `/negocios/{id}` | Cualquiera | Detalle |
| GET / PUT | `/negocios/mio` | NEGOCIO | Ver o editar mi negocio |
| PUT | `/negocios/mio/abierto?valor=true` | NEGOCIO | Abrir o cerrar el puesto |
| POST | `/negocios` | ADMIN | `{ negocio, propietarioId }` o `{ negocio, nuevoPropietario }` |
| PUT | `/negocios/{id}?propietarioId=` | ADMIN | Editar o cambiar el dueño |
| POST | `/negocios/{id}/habilitar` · `/deshabilitar` | ADMIN | Habilitar o deshabilitar |
| DELETE | `/negocios/{id}` | ADMIN | Eliminar (solo si no tiene productos ni pedidos) |

## Categorías y productos

| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| GET | `/categorias` | Cualquiera | Categorías generales (`?todas=true` para el admin) |
| GET | `/categorias/mias` | NEGOCIO | Generales y propias de mi negocio |
| GET | `/categorias/negocio/{id}` | Cualquiera | Categorías que puede usar un negocio |
| POST / PUT / DELETE | `/categorias[/{id}]` | NEGOCIO, ADMIN | El admin gestiona las generales y el negocio las suyas |
| GET | `/productos/negocio/{id}?soloDisponibles=` | Cualquiera | Menú de un negocio |
| GET | `/productos/mios` | NEGOCIO | Productos de mi negocio |
| POST / PUT | `/productos[/{id}]` | NEGOCIO, ADMIN | Crear o editar |
| PUT | `/productos/{id}/disponible?valor=` | NEGOCIO, ADMIN | Marcar disponible o agotado |
| DELETE | `/productos/{id}` | NEGOCIO, ADMIN | Elimina, o lo marca no disponible si ya tiene pedidos |

## Pedidos

| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| POST | `/pedidos` | CLIENTE | `{ negocioId, metodoPago, notas, items: [{ productoId, cantidad }] }`. Crea el pedido con sus detalles y su pago |
| GET | `/pedidos/mios` | CLIENTE | Mis pedidos |
| GET | `/pedidos/negocio?activos=true` | NEGOCIO | Pedidos de mi negocio (activos o todos) |
| GET | `/pedidos?estado=` | ADMIN | Todos los pedidos |
| GET | `/pedidos/{id}` | Dueño del pedido, del negocio o ADMIN | Detalle |
| POST | `/pedidos/{id}/aceptar` | NEGOCIO | `{ minutos }`. PENDIENTE → ACEPTADO |
| POST | `/pedidos/{id}/preparar` | NEGOCIO | ACEPTADO → EN_PREPARACION |
| POST | `/pedidos/{id}/listo` | NEGOCIO | → LISTO_PARA_RECOGER |
| POST | `/pedidos/{id}/recogido` | NEGOCIO | → RECOGIDO |
| POST | `/pedidos/{id}/rechazar` | NEGOCIO | `{ motivo }`. → CANCELADO y reembolso |
| POST | `/pedidos/{id}/cancelar` | CLIENTE, ADMIN | Solo si está PENDIENTE. Hace el reembolso |
| PUT | `/pedidos/{id}/tiempo` | NEGOCIO | `{ minutos }`. Cambia el tiempo estimado |

Cada cambio de estado crea una notificación para el cliente. Los pedidos nuevos y las cancelaciones notifican al negocio.

## Notificaciones

| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| GET | `/notificaciones` | Cualquiera | Mis notificaciones |
| GET | `/notificaciones/no-leidas` | Cualquiera | `{ noLeidas }` |
| PUT | `/notificaciones/{id}/leer` | Cualquiera | Marcar como leída |
| PUT | `/notificaciones/leer-todas` | Cualquiera | Marcar todas como leídas |

## Administración

| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| GET | `/admin/resumen` | ADMIN | Totales de usuarios, negocios, pedidos, ventas y pedidos por estado |
| GET | `/pagos`, `/pagos/pedido/{id}` | ADMIN | Pagos registrados |
| GET | `/detalles-pedido/pedido/{id}` | ADMIN | Detalles de un pedido |
