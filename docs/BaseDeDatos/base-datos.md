# Diseño de la Base de Datos PideCerca

## 1. Descripción

La base de datos de PideCerca será utilizada para almacenar y organizar la información necesaria para el funcionamiento de la plataforma.

Permitirá gestionar usuarios, negocios, categorías, productos, pedidos, pagos y notificaciones.

## 2. Entidades principales

### Usuario

Almacena la información de los usuarios del sistema.

**Campos principales:**

* id_usuario
* nombre
* apellido
* correo
* contraseña
* teléfono
* rol
* estado

### Negocio

Almacena la información de los establecimientos registrados en PideCerca.

**Campos principales:**

* id_negocio
* nombre
* descripción
* dirección
* latitud
* longitud
* teléfono
* estado

### Categoría

Permite clasificar los productos de cada negocio.

**Campos principales:**

* id_categoria
* nombre
* descripción
* estado

### Producto

Almacena los productos ofrecidos por los negocios.

**Campos principales:**

* id_producto
* nombre
* descripción
* precio
* imagen
* disponibilidad
* id_categoria
* id_negocio

### Pedido

Registra los pedidos realizados por los clientes.

**Campos principales:**

* id_pedido
* fecha
* total
* estado
* tiempo_preparacion
* id_usuario
* id_negocio

### Detalle del Pedido

Almacena los productos incluidos dentro de cada pedido.

**Campos principales:**

* id_detalle
* cantidad
* precio_unitario
* subtotal
* id_pedido
* id_producto

### Pago

Registra la información relacionada con el pago de un pedido.

**Campos principales:**

* id_pago
* monto
* fecha
* metodo_pago
* estado
* id_pedido

### Notificación

Almacena las notificaciones relacionadas con los pedidos y usuarios.

**Campos principales:**

* id_notificacion
* mensaje
* fecha
* leida
* id_usuario
* id_pedido

## 3. Relaciones principales

* Un usuario puede realizar muchos pedidos.
* Un negocio puede recibir muchos pedidos.
* Un negocio puede tener muchos productos.
* Una categoría puede contener muchos productos.
* Un pedido puede contener varios detalles de pedido.
* Un producto puede aparecer en varios detalles de pedido.
* Cada pedido tiene un pago asociado.
* Un usuario puede recibir muchas notificaciones.
* Un pedido puede generar varias notificaciones.

## 4. Claves principales y foráneas

Las entidades utilizarán una clave primaria (PK) para identificar cada registro de forma única.

Las claves foráneas (FK) permitirán establecer las relaciones entre las entidades.

Ejemplos:

* `id_usuario` en PEDIDO → referencia a USUARIO.
* `id_negocio` en PEDIDO → referencia a NEGOCIO.
* `id_categoria` en PRODUCTO → referencia a CATEGORIA.
* `id_producto` en DETALLE_PEDIDO → referencia a PRODUCTO.
* `id_pedido` en DETALLE_PEDIDO → referencia a PEDIDO.

## 5. Diagrama Entidad-Relación

El siguiente diagrama representa la estructura y las relaciones principales de la base de datos de PideCerca:

![Diagrama Entidad-Relación de PideCerca](![alt text](base-datos-pidecerca.png))

## 6. Objetivo del diseño

El diseño de la base de datos busca organizar correctamente la información del sistema, evitar datos repetidos y facilitar el acceso y mantenimiento de la información.

La estructura permitirá que PideCerca gestione de manera organizada los usuarios, negocios, productos, pedidos y pagos.

