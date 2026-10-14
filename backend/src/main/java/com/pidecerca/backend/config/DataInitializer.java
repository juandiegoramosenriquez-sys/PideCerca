package com.pidecerca.backend.config;

import com.pidecerca.backend.entity.*;
import com.pidecerca.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final NegocioRepository negocioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;
    private final NotificacionRepository notificacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminCorreo;
    private final String adminPassword;
    private final boolean demoHabilitado;

    public DataInitializer(
            UsuarioRepository usuarioRepository,
            NegocioRepository negocioRepository,
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository,
            PedidoRepository pedidoRepository,
            PagoRepository pagoRepository,
            NotificacionRepository notificacionRepository,
            PasswordEncoder passwordEncoder,
            @Value("${pidecerca.admin.correo}") String adminCorreo,
            @Value("${pidecerca.admin.password}") String adminPassword,
            @Value("${pidecerca.demo.habilitado}") boolean demoHabilitado) {

        this.usuarioRepository = usuarioRepository;
        this.negocioRepository = negocioRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
        this.pagoRepository = pagoRepository;
        this.notificacionRepository = notificacionRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminCorreo = adminCorreo;
        this.adminPassword = adminPassword;
        this.demoHabilitado = demoHabilitado;
    }

    @Override
    @Transactional
    public void run(String... args) {

        if (!usuarioRepository.existsByRol(Rol.ADMIN)) {
            usuario("Administrador", "PideCerca", adminCorreo, adminPassword, "999000111", Rol.ADMIN);
            log.info("Administrador inicial creado: {}", adminCorreo);
        }

        if (demoHabilitado && negocioRepository.count() == 0) {
            cargarDemo();
            log.info("Datos de demostración cargados");
        }
    }

    private void cargarDemo() {

        Usuario lucho = usuario("Rosa", "Huamán", "negocio@pidecerca.com", "negocio123", "987654321", Rol.NEGOCIO);
        Usuario polleria = usuario("Carmen", "Quispe", "polleria@pidecerca.com", "negocio123", "987111222", Rol.NEGOCIO);
        Usuario salchi = usuario("Luz", "Mendoza", "salchipapas@pidecerca.com", "negocio123", "987333444", Rol.NEGOCIO);
        Usuario ana = usuario("Ana", "López", "cliente@pidecerca.com", "cliente123", "912345678", Rol.CLIENTE);
        Usuario luis = usuario("Luis", "Torres", "luis@pidecerca.com", "cliente123", "923456789", Rol.CLIENTE);

        Map<String, Categoria> categorias = new HashMap<>();
        for (String nombre : List.of("Hamburguesas", "Pollo a la brasa", "Salchipapas", "Combos", "Bebidas", "Postres")) {
            Categoria categoria = new Categoria();
            categoria.setNombre(nombre);
            categoria.setDescripcion("Categoría general");
            categoria.setActivo(true);
            categorias.put(nombre, categoriaRepository.save(categoria));
        }

        Negocio donLucho = negocio("Hamburguesería Don Lucho", "Hamburguesas", "Hamburguesas al carbón hechas al momento",
                "Av. Grau 245, Cercado de Lima", -12.0560, -77.0365, 15, "Lun a Dom · 6:00 pm - 12:00 am", lucho);
        Negocio laEsquina = negocio("Pollería La Esquina", "Pollo a la brasa", "Pollo a la brasa con papas y ensalada",
                "Jr. Huánuco 812, Cercado de Lima", -12.0590, -77.0310, 25, "Lun a Dom · 12:00 pm - 11:00 pm", polleria);
        Negocio gordito = negocio("Salchipapas El Gordito", "Salchipapas", "Salchipapas y alitas para llevar",
                "Av. Abancay 410, Cercado de Lima", -12.0510, -77.0300, 10, "Mar a Dom · 5:00 pm - 11:00 pm", salchi);

        Producto royal = producto(donLucho, categorias.get("Hamburguesas"), "Hamburguesa royal", "Carne, huevo, queso y plátano", 12.0);
        producto(donLucho, categorias.get("Hamburguesas"), "Hamburguesa clásica", "Carne, lechuga, tomate y papas al hilo", 9.0);
        Producto doble = producto(donLucho, categorias.get("Hamburguesas"), "Hamburguesa doble", "Doble carne y doble queso", 16.0);
        producto(donLucho, categorias.get("Combos"), "Combo royal + papas + gaseosa", "Hamburguesa royal, papas medianas y gaseosa", 20.0);
        Producto papas = producto(donLucho, categorias.get("Salchipapas"), "Papas grandes", "Papas fritas crocantes", 6.0);
        Producto inca = producto(donLucho, categorias.get("Bebidas"), "Inca Kola 500 ml", "Bien helada", 4.0);
        producto(donLucho, categorias.get("Bebidas"), "Chicha morada", "Vaso grande", 3.5);

        producto(laEsquina, categorias.get("Pollo a la brasa"), "1/4 de pollo", "Con papas y ensalada", 16.0);
        producto(laEsquina, categorias.get("Pollo a la brasa"), "1/2 pollo", "Con papas y ensalada", 30.0);
        producto(laEsquina, categorias.get("Pollo a la brasa"), "Pollo entero", "Con papas familiares y ensalada", 58.0);
        producto(laEsquina, categorias.get("Bebidas"), "Gaseosa 1.5 L", "Inca Kola o Coca Cola", 9.0);

        producto(gordito, categorias.get("Salchipapas"), "Salchipapa clásica", "Papas y hot dog con cremas", 10.0);
        producto(gordito, categorias.get("Salchipapas"), "Salchipapa especial", "Con huevo, chorizo y queso", 15.0);
        producto(gordito, categorias.get("Combos"), "Alitas BBQ x6", "Con papas", 18.0);
        producto(gordito, categorias.get("Postres"), "Picarones", "Porción de 4 con miel", 7.0);

        pedido(donLucho, ana, EstadoPedido.PENDIENTE, 2, Map.of(royal, 2, papas, 1, inca, 1));
        pedido(donLucho, luis, EstadoPedido.EN_PREPARACION, 8, Map.of(doble, 1));
        pedido(donLucho, ana, EstadoPedido.RECOGIDO, 60 * 26, Map.of(royal, 1, inca, 1));
    }

    private Usuario usuario(String nombre, String apellido, String correo, String password, String telefono, String rol) {

        return usuarioRepository.findByCorreoIgnoreCase(correo).orElseGet(() -> {
            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setCorreo(correo.toLowerCase());
            usuario.setPassword(passwordEncoder.encode(password));
            usuario.setTelefono(telefono);
            usuario.setRol(rol);
            usuario.setActivo(true);
            usuario.setFechaRegistro(LocalDateTime.now());
            return usuarioRepository.save(usuario);
        });
    }

    private Negocio negocio(String nombre, String tipo, String descripcion, String direccion,
                            double latitud, double longitud, int tiempo, String horario, Usuario propietario) {

        Negocio negocio = new Negocio();
        negocio.setNombre(nombre);
        negocio.setTipoComida(tipo);
        negocio.setDescripcion(descripcion);
        negocio.setDireccion(direccion);
        negocio.setLatitud(latitud);
        negocio.setLongitud(longitud);
        negocio.setTiempoEstimado(tiempo);
        negocio.setHorario(horario);
        negocio.setTelefono(propietario.getTelefono());
        negocio.setPropietario(propietario);
        negocio.setActivo(true);
        negocio.setAbierto(true);
        return negocioRepository.save(negocio);
    }

    private Producto producto(Negocio negocio, Categoria categoria, String nombre, String descripcion, double precio) {

        Producto producto = new Producto();
        producto.setNegocio(negocio);
        producto.setCategoria(categoria);
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setDisponible(true);
        return productoRepository.save(producto);
    }

    private void pedido(Negocio negocio, Usuario cliente, EstadoPedido estado, int minutosAtras, Map<Producto, Integer> items) {

        LocalDateTime fecha = LocalDateTime.now().minusMinutes(minutosAtras);

        Pedido pedido = new Pedido();
        pedido.setNegocio(negocio);
        pedido.setUsuario(cliente);
        pedido.setFecha(fecha);
        pedido.setEstado(estado);
        pedido.setTiempoPreparacion(negocio.getTiempoEstimado());

        if (estado != EstadoPedido.PENDIENTE) {
            pedido.setFechaAceptado(fecha.plusMinutes(1));
        }

        if (estado == EstadoPedido.RECOGIDO) {
            pedido.setFechaListo(fecha.plusMinutes(15));
            pedido.setFechaRecogido(fecha.plusMinutes(20));
        }

        double total = 0;

        for (Map.Entry<Producto, Integer> item : items.entrySet()) {
            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(item.getKey());
            detalle.setCantidad(item.getValue());
            detalle.setPrecioUnitario(item.getKey().getPrecio());
            detalle.setSubtotal(item.getKey().getPrecio() * item.getValue());
            pedido.getDetalles().add(detalle);
            total += detalle.getSubtotal();
        }

        pedido.setTotal(total);
        Pedido guardado = pedidoRepository.save(pedido);

        Pago pago = new Pago();
        pago.setPedido(guardado);
        pago.setMonto(total);
        pago.setMetodo("YAPE");
        pago.setFecha(fecha);
        pago.setEstado(EstadoPago.PAGADO);
        pagoRepository.save(pago);

        if (estado == EstadoPedido.PENDIENTE) {
            Notificacion notificacion = new Notificacion();
            notificacion.setUsuario(negocio.getPropietario());
            notificacion.setPedido(guardado);
            notificacion.setTitulo("Nuevo pedido #" + guardado.getId());
            notificacion.setMensaje("Tienes un pedido nuevo de " + cliente.getNombre());
            notificacion.setLeida(false);
            notificacion.setFecha(fecha);
            notificacionRepository.save(notificacion);
        }
    }
}
