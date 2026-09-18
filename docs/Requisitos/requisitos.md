# PideCerca

## 1. Descripción del proyecto

PideCerca es una plataforma para realizar pedidos de comida rápida en negocios cercanos.

La plataforma estará enfocada principalmente en pedidos para llevar. El cliente podrá seleccionar un negocio cercano, revisar su menú, realizar un pedido, efectuar el pago y recibir un tiempo estimado para recogerlo.

El objetivo principal es reducir el tiempo de espera del cliente en el establecimiento y facilitar al negocio la organización de los pedidos.

---

## 2. Tipo de comida

La plataforma estará orientada principalmente a comida rápida, por ejemplo:

* Pollo a la brasa.
* Salchipapas.
* Hamburguesas.
* Combos.
* Papas fritas.
* Bebidas.
* Otros productos ofrecidos por cada negocio.

Cada negocio podrá administrar sus propios productos, categorías y precios.

---

## 3. Usuarios del sistema

PideCerca contará con tres tipos principales de usuarios:

### 3.1 Cliente

El cliente podrá:

* Registrarse e iniciar sesión.
* Buscar negocios cercanos.
* Consultar los negocios disponibles.
* Seleccionar un negocio.
* Ver el menú de cada negocio.
* Consultar productos, categorías y precios.
* Seleccionar productos.
* Elegir cantidades y opciones disponibles.
* Agregar productos al carrito.
* Modificar o eliminar productos del carrito.
* Ver el precio total del pedido.
* Confirmar el pedido.
* Realizar el pago.
* Consultar el estado de su pedido.
* Ver el tiempo estimado de preparación.
* Recibir una notificación cuando su pedido esté listo.
* Acudir al negocio para recoger el pedido.
* Consultar sus pedidos anteriores.

### 3.2 Negocio

El negocio tendrá un panel de administración sencillo para gestionar sus operaciones.

Podrá:

* Iniciar sesión.
* Administrar la información de su negocio.
* Registrar productos.
* Modificar productos.
* Eliminar o desactivar productos.
* Modificar precios.
* Organizar productos por categorías.
* Indicar la disponibilidad de sus productos.
* Ver los pedidos recibidos.
* Ver los productos y cantidades de cada pedido.
* Aceptar o rechazar pedidos.
* Cambiar el estado de los pedidos.
* Indicar un tiempo estimado de preparación.
* Marcar un pedido como listo para recoger.
* Confirmar la entrega del pedido al cliente.
* Consultar pedidos anteriores.

El panel deberá mostrar claramente los pedidos pendientes y los pedidos en preparación para que el negocio pueda saber qué debe preparar.

### 3.3 Administrador

El administrador tendrá control general de la plataforma.

Podrá:

* Iniciar sesión en el panel administrativo.
* Administrar usuarios.
* Consultar información de los usuarios.
* Administrar negocios.
* Registrar o habilitar negocios.
* Deshabilitar negocios.
* Consultar pedidos.
* Gestionar categorías generales.
* Supervisar el funcionamiento de la plataforma.

---

## 4. Funcionamiento principal del sistema

El proceso principal de PideCerca será:

1. El cliente ingresa a PideCerca.
2. El cliente inicia sesión.
3. El sistema obtiene o utiliza la ubicación del cliente.
4. El sistema muestra negocios cercanos.
5. El cliente selecciona un negocio.
6. El cliente revisa el menú.
7. El cliente selecciona los productos.
8. Los productos se agregan al carrito.
9. El cliente revisa y confirma su pedido.
10. El sistema calcula el total.
11. El cliente realiza el pago.
12. El sistema registra el pedido.
13. El negocio recibe el pedido.
14. El negocio acepta o rechaza el pedido.
15. Si acepta el pedido, comienza la preparación.
16. El negocio establece o confirma un tiempo estimado de preparación.
17. El cliente puede consultar el estado de su pedido.
18. El negocio marca el pedido como listo.
19. El sistema notifica al cliente que puede recogerlo.
20. El cliente acude al negocio.
21. El cliente recoge el pedido.
22. El negocio confirma que el pedido fue recogido.
23. El pedido queda registrado en el historial.

---

## 5. Estados del pedido

Los pedidos tendrán los siguientes estados:

```text
PENDIENTE
    ↓
ACEPTADO
    ↓
EN PREPARACIÓN
    ↓
LISTO PARA RECOGER
    ↓
RECOGIDO
```

También podrá existir:

```text
CANCELADO
```

El estado permitirá que tanto el cliente como el negocio conozcan la situación actual del pedido.

---

## 6. Panel del negocio

El panel del negocio será una de las partes principales de PideCerca.

Deberá mostrar los pedidos de manera clara y sencilla.

Ejemplo:

```text
PEDIDOS

Pedido #001
-------------------------
2x Hamburguesa
1x Salchipapa
1x Gaseosa

Estado: EN PREPARACIÓN
Tiempo estimado: 15 min


Pedido #002
-------------------------
1x Pollo + papas
2x Gaseosa

Estado: PENDIENTE
```

El negocio podrá seleccionar un pedido y consultar todos sus detalles.

El panel deberá permitir identificar rápidamente:

* Pedidos pendientes.
* Pedidos aceptados.
* Pedidos en preparación.
* Pedidos listos para recoger.
* Pedidos recogidos.
* Pedidos cancelados.

---

# 7. Requerimientos Funcionales

Los requerimientos funcionales describen las funciones que deberá realizar el sistema.

## 7.1 Cliente

* **RF01.** El cliente podrá registrarse en PideCerca.
* **RF02.** El cliente podrá iniciar sesión.
* **RF03.** El cliente podrá cerrar sesión.
* **RF04.** El cliente podrá visualizar negocios cercanos.
* **RF05.** El cliente podrá buscar negocios.
* **RF06.** El cliente podrá seleccionar un negocio.
* **RF07.** El cliente podrá visualizar el menú de un negocio.
* **RF08.** El cliente podrá visualizar las categorías de productos.
* **RF09.** El cliente podrá visualizar el precio de los productos.
* **RF10.** El cliente podrá seleccionar productos.
* **RF11.** El cliente podrá agregar productos al carrito.
* **RF12.** El cliente podrá modificar las cantidades de los productos.
* **RF13.** El cliente podrá eliminar productos del carrito.
* **RF14.** El cliente podrá visualizar el resumen de su pedido.
* **RF15.** El sistema podrá calcular el total del pedido.
* **RF16.** El cliente podrá confirmar su pedido.
* **RF17.** El cliente podrá realizar el pago.
* **RF18.** El cliente podrá consultar sus pedidos.
* **RF19.** El cliente podrá consultar el estado de su pedido.
* **RF20.** El cliente podrá consultar el tiempo estimado de preparación.
* **RF21.** El cliente podrá recibir una notificación cuando su pedido esté listo.
* **RF22.** El cliente podrá consultar su historial de pedidos.

## 7.2 Negocio

* **RF23.** El negocio podrá iniciar sesión.
* **RF24.** El negocio podrá administrar la información de su establecimiento.
* **RF25.** El negocio podrá registrar productos.
* **RF26.** El negocio podrá modificar productos.
* **RF27.** El negocio podrá eliminar o desactivar productos.
* **RF28.** El negocio podrá modificar los precios de sus productos.
* **RF29.** El negocio podrá administrar categorías.
* **RF30.** El negocio podrá establecer la disponibilidad de sus productos.
* **RF31.** El negocio podrá visualizar los pedidos recibidos.
* **RF32.** El negocio podrá consultar los detalles de cada pedido.
* **RF33.** El negocio podrá aceptar pedidos.
* **RF34.** El negocio podrá rechazar pedidos.
* **RF35.** El negocio podrá actualizar el estado de los pedidos.
* **RF36.** El negocio podrá establecer el tiempo estimado de preparación.
* **RF37.** El negocio podrá marcar un pedido como listo para recoger.
* **RF38.** El negocio podrá confirmar que un pedido fue recogido.
* **RF39.** El negocio podrá consultar el historial de pedidos.

## 7.3 Administrador

* **RF40.** El administrador podrá iniciar sesión.
* **RF41.** El administrador podrá visualizar los usuarios registrados.
* **RF42.** El administrador podrá administrar las cuentas de los usuarios.
* **RF43.** El administrador podrá visualizar los negocios registrados.
* **RF44.** El administrador podrá registrar negocios.
* **RF45.** El administrador podrá habilitar negocios.
* **RF46.** El administrador podrá deshabilitar negocios.
* **RF47.** El administrador podrá consultar los pedidos realizados.
* **RF48.** El administrador podrá administrar las categorías generales.
* **RF49.** El administrador podrá consultar información general de la plataforma.

## 7.4 Sistema

* **RF50.** El sistema podrá utilizar la ubicación del cliente para mostrar negocios cercanos.
* **RF51.** El sistema podrá almacenar la información de los usuarios.
* **RF52.** El sistema podrá almacenar la información de los negocios.
* **RF53.** El sistema podrá almacenar la información de los productos.
* **RF54.** El sistema podrá registrar los pedidos.
* **RF55.** El sistema podrá registrar los detalles de cada pedido.
* **RF56.** El sistema podrá actualizar el estado de los pedidos.
* **RF57.** El sistema podrá calcular el total de cada pedido.
* **RF58.** El sistema podrá enviar notificaciones relacionadas con el estado del pedido.
* **RF59.** El sistema podrá mantener un historial de pedidos.
* **RF60.** El sistema podrá controlar los permisos según el tipo de usuario.

---

# 8. Requerimientos No Funcionales

Los requerimientos no funcionales describen las características de calidad que deberá cumplir PideCerca.

* **RNF01.** La interfaz deberá ser sencilla y fácil de utilizar.
* **RNF02.** El sistema deberá presentar la información de manera clara y ordenada.
* **RNF03.** El sistema deberá proteger la información personal de los usuarios.
* **RNF04.** El sistema deberá proteger la información relacionada con los pagos.
* **RNF05.** El sistema deberá requerir autenticación para acceder a las funciones correspondientes.
* **RNF06.** El sistema deberá diferenciar los permisos entre cliente, negocio y administrador.
* **RNF07.** Las operaciones principales deberán responder en un tiempo adecuado.
* **RNF08.** El sistema deberá mantener la información de los pedidos de forma consistente.
* **RNF09.** La aplicación móvil deberá adaptarse correctamente a diferentes tamaños de pantalla.
* **RNF10.** El sistema web deberá adaptarse a diferentes tamaños de pantalla.
* **RNF11.** El sistema deberá contar con mecanismos de respaldo de información.
* **RNF12.** El sistema deberá estar diseñado para permitir futuras mejoras y nuevas funcionalidades.
* **RNF13.** El sistema deberá registrar las operaciones importantes para facilitar su seguimiento.
* **RNF14.** La información almacenada deberá contar con mecanismos de protección contra accesos no autorizados.
* **RNF15.** El sistema deberá mantener la disponibilidad durante el horario de funcionamiento de los negocios.
* **RNF16.** El sistema deberá permitir que el panel del negocio sea utilizado de manera sencilla y rápida.
* **RNF17.** El sistema deberá mantener una comunicación adecuada entre la aplicación móvil, el sistema web y el backend.
* **RNF18.** El sistema deberá permitir realizar mantenimiento y actualización de sus componentes sin afectar innecesariamente las demás partes de la plataforma.

---

# 9. Objetivo principal

El objetivo de PideCerca es facilitar la compra de comida rápida para llevar, permitiendo que el cliente realice su pedido y pago con anticipación para reducir el tiempo de espera al momento de recogerlo.

Al mismo tiempo, la plataforma permitirá que los negocios administren sus productos y pedidos desde un panel sencillo, organizado y fácil de utilizar.

---

# 10. Alcance inicial

La primera versión de PideCerca estará enfocada en:

* Clientes que desean realizar pedidos de comida rápida para recoger.
* Negocios de comida rápida que desean administrar sus pedidos.
* Un administrador encargado de supervisar la plataforma.
* Búsqueda de negocios cercanos.
* Visualización de menús.
* Carrito de compras.
* Realización de pedidos.
* Pago de pedidos.
* Seguimiento del estado del pedido.
* Notificación de pedido listo.
* Recogida del pedido en el establecimiento.
* Administración de productos y pedidos por parte del negocio.
* Administración general de la plataforma.

La entrega a domicilio no formará parte del enfoque principal de la primera versión.
