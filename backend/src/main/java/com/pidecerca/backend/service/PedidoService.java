package com.pidecerca.backend.service;

import com.pidecerca.backend.dto.CrearPedidoRequest;
import com.pidecerca.backend.dto.ItemPedidoRequest;
import com.pidecerca.backend.entity.*;
import com.pidecerca.backend.exception.ApiException;
import com.pidecerca.backend.repository.NegocioRepository;
import com.pidecerca.backend.repository.PagoRepository;
import com.pidecerca.backend.repository.PedidoRepository;
import com.pidecerca.backend.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PedidoService {

    private static final Set<String> METODOS_PAGO = Set.of("YAPE", "PLIN", "TARJETA", "EFECTIVO");

    private static final Set<EstadoPedido> ESTADOS_ACTIVOS = EnumSet.of(
            EstadoPedido.PENDIENTE,
            EstadoPedido.ACEPTADO,
            EstadoPedido.EN_PREPARACION,
            EstadoPedido.LISTO_PARA_RECOGER
    );

    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;
    private final ProductoRepository productoRepository;
    private final NegocioRepository negocioRepository;
    private final NegocioService negocioService;
    private final NotificacionService notificacionService;
    private final UsuarioActualService usuarioActualService;

    public PedidoService(
            PedidoRepository pedidoRepository,
            PagoRepository pagoRepository,
            ProductoRepository productoRepository,
            NegocioRepository negocioRepository,
            NegocioService negocioService,
            NotificacionService notificacionService,
            UsuarioActualService usuarioActualService) {

        this.pedidoRepository = pedidoRepository;
        this.pagoRepository = pagoRepository;
        this.productoRepository = productoRepository;
        this.negocioRepository = negocioRepository;
        this.negocioService = negocioService;
        this.notificacionService = notificacionService;
        this.usuarioActualService = usuarioActualService;
    }

    public List<Pedido> listarTodos(EstadoPedido estado) {

        if (estado != null) {
            return pedidoRepository.findByEstadoOrderByFechaDesc(estado);
        }

        return pedidoRepository.findAllByOrderByFechaDesc();
    }

    public List<Pedido> listarDelCliente(Usuario cliente) {
        return pedidoRepository.findByUsuarioIdOrderByFechaDesc(cliente.getId());
    }

    public List<Pedido> listarDelNegocio(Usuario usuario, boolean soloActivos) {

        Negocio negocio = negocioService.obtenerDelPropietario(usuario);

        if (soloActivos) {
            return pedidoRepository.findByNegocioIdAndEstadoInOrderByFechaAsc(
                    negocio.getId(), ESTADOS_ACTIVOS);
        }

        return pedidoRepository.findByNegocioIdOrderByFechaDesc(negocio.getId());
    }

    public Pedido obtener(Usuario usuario, Long id) {

        Pedido pedido = buscar(id);

        if (usuarioActualService.esAdmin(usuario)) {
            return pedido;
        }

        if (pedido.getUsuario() != null && pedido.getUsuario().getId().equals(usuario.getId())) {
            return pedido;
        }

        if (esDuenoDelNegocio(usuario, pedido)) {
            return pedido;
        }

        throw ApiException.prohibido("No tienes acceso a este pedido");
    }

    @Transactional
    public Pedido crear(Usuario cliente, CrearPedidoRequest request) {

        if (request == null || request.negocioId() == null) {
            throw ApiException.solicitudInvalida("Selecciona un negocio");
        }

        if (request.items() == null || request.items().isEmpty()) {
            throw ApiException.solicitudInvalida("Tu carrito está vacío");
        }

        Negocio negocio = negocioRepository.findById(request.negocioId())
                .orElseThrow(() -> ApiException.noEncontrado("Negocio no encontrado"));

        if (!Boolean.TRUE.equals(negocio.getActivo())) {
            throw ApiException.solicitudInvalida("Este negocio no está disponible");
        }

        if (!Boolean.TRUE.equals(negocio.getAbierto())) {
            throw ApiException.solicitudInvalida("El negocio está cerrado en este momento");
        }

        String metodo = request.metodoPago() == null
                ? "YAPE"
                : request.metodoPago().trim().toUpperCase();

        if (!METODOS_PAGO.contains(metodo)) {
            throw ApiException.solicitudInvalida("Método de pago no válido");
        }

        Map<Long, Integer> cantidades = new LinkedHashMap<>();

        for (ItemPedidoRequest item : request.items()) {
            if (item.productoId() == null) {
                throw ApiException.solicitudInvalida("Hay un producto inválido en el carrito");
            }
            if (item.cantidad() == null || item.cantidad() <= 0 || item.cantidad() > 50) {
                throw ApiException.solicitudInvalida("La cantidad debe estar entre 1 y 50");
            }
            cantidades.merge(item.productoId(), item.cantidad(), Integer::sum);
        }

        Pedido pedido = new Pedido();
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setUsuario(cliente);
        pedido.setNegocio(negocio);
        pedido.setNotas(AuthService.texto(request.notas()));
        pedido.setTiempoPreparacion(negocio.getTiempoEstimado());

        double total = 0;

        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {

            Producto producto = productoRepository.findById(entrada.getKey())
                    .orElseThrow(() -> ApiException.noEncontrado("Un producto del carrito ya no existe"));

            if (producto.getNegocio() == null || !producto.getNegocio().getId().equals(negocio.getId())) {
                throw ApiException.solicitudInvalida("Todos los productos deben ser del mismo negocio");
            }

            if (!Boolean.TRUE.equals(producto.getDisponible())) {
                throw ApiException.solicitudInvalida(producto.getNombre() + " ya no está disponible");
            }

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(entrada.getValue());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(redondear(producto.getPrecio() * entrada.getValue()));

            pedido.getDetalles().add(detalle);
            total += detalle.getSubtotal();
        }

        pedido.setTotal(redondear(total));

        Pedido guardado = pedidoRepository.save(pedido);

        Pago pago = new Pago();
        pago.setPedido(guardado);
        pago.setMonto(guardado.getTotal());
        pago.setMetodo(metodo);
        pago.setFecha(LocalDateTime.now());
        pago.setEstado(EstadoPago.PAGADO);
        guardado.setPago(pagoRepository.save(pago));

        notificacionService.crear(
                negocio.getPropietario(),
                guardado,
                "Nuevo pedido #" + guardado.getId(),
                resumenProductos(guardado) + " · S/ " + String.format("%.2f", guardado.getTotal())
        );

        notificacionService.crear(
                cliente,
                guardado,
                "Pedido enviado",
                "Tu pedido #" + guardado.getId() + " fue enviado a " + negocio.getNombre()
                        + ". Te avisaremos cuando lo acepten"
        );

        return guardado;
    }

    @Transactional
    public Pedido aceptar(Usuario usuario, Long id, Integer minutos) {

        Pedido pedido = obtenerParaNegocio(usuario, id);
        validarEstado(pedido, EstadoPedido.PENDIENTE, "Solo puedes aceptar pedidos nuevos");

        Pago pago = pagoRepository.findByPedidoId(id).orElse(null);

        if (pago == null || pago.getEstado() != EstadoPago.PAGADO) {
            throw ApiException.solicitudInvalida("El pedido no está pagado");
        }

        if (minutos != null) {
            validarMinutos(minutos);
            pedido.setTiempoPreparacion(minutos);
        }

        if (pedido.getTiempoPreparacion() == null) {
            pedido.setTiempoPreparacion(15);
        }

        pedido.setEstado(EstadoPedido.ACEPTADO);
        pedido.setFechaAceptado(LocalDateTime.now());

        Pedido guardado = pedidoRepository.save(pedido);

        notificarCliente(guardado, "Pedido aceptado",
                "Tu pedido #" + guardado.getId() + " fue aceptado. Estará listo en unos "
                        + guardado.getTiempoPreparacion() + " minutos");

        return guardado;
    }

    @Transactional
    public Pedido iniciarPreparacion(Usuario usuario, Long id) {

        Pedido pedido = obtenerParaNegocio(usuario, id);
        validarEstado(pedido, EstadoPedido.ACEPTADO, "El pedido debe estar aceptado para prepararlo");

        pedido.setEstado(EstadoPedido.EN_PREPARACION);

        Pedido guardado = pedidoRepository.save(pedido);

        notificarCliente(guardado, "En preparación",
                "Tu pedido #" + guardado.getId() + " se está preparando");

        return guardado;
    }

    @Transactional
    public Pedido marcarListo(Usuario usuario, Long id) {

        Pedido pedido = obtenerParaNegocio(usuario, id);

        if (pedido.getEstado() != EstadoPedido.EN_PREPARACION
                && pedido.getEstado() != EstadoPedido.ACEPTADO) {
            throw ApiException.solicitudInvalida("El pedido debe estar en preparación");
        }

        pedido.setEstado(EstadoPedido.LISTO_PARA_RECOGER);
        pedido.setFechaListo(LocalDateTime.now());

        Pedido guardado = pedidoRepository.save(pedido);

        notificarCliente(guardado, "¡Tu pedido está listo!",
                "Acércate a " + guardado.getNegocio().getNombre() + " y di el número #"
                        + guardado.getId() + " para recogerlo");

        return guardado;
    }

    @Transactional
    public Pedido marcarRecogido(Usuario usuario, Long id) {

        Pedido pedido = obtenerParaNegocio(usuario, id);
        validarEstado(pedido, EstadoPedido.LISTO_PARA_RECOGER, "El pedido todavía no está listo");

        pedido.setEstado(EstadoPedido.RECOGIDO);
        pedido.setFechaRecogido(LocalDateTime.now());

        Pedido guardado = pedidoRepository.save(pedido);

        notificarCliente(guardado, "Pedido entregado",
                "Gracias por tu compra en " + guardado.getNegocio().getNombre() + ". ¡Buen provecho!");

        return guardado;
    }

    @Transactional
    public Pedido rechazar(Usuario usuario, Long id, String motivo) {

        Pedido pedido = obtenerParaNegocio(usuario, id);

        if (pedido.getEstado() != EstadoPedido.PENDIENTE
                && pedido.getEstado() != EstadoPedido.ACEPTADO) {
            throw ApiException.solicitudInvalida("Solo puedes rechazar pedidos nuevos o aceptados");
        }

        String razon = AuthService.texto(motivo);
        cancelarConReembolso(pedido, razon == null ? "Rechazado por el negocio" : razon);

        Pedido guardado = pedidoRepository.save(pedido);

        notificarCliente(guardado, "Pedido rechazado",
                "El negocio no pudo atender tu pedido #" + guardado.getId()
                        + (razon == null ? "" : " (" + razon + ")")
                        + ". Te devolveremos tu dinero");

        return guardado;
    }

    @Transactional
    public Pedido cancelar(Usuario usuario, Long id) {

        Pedido pedido = buscar(id);

        boolean esAdmin = usuarioActualService.esAdmin(usuario);
        boolean esCliente = pedido.getUsuario() != null
                && pedido.getUsuario().getId().equals(usuario.getId());

        if (!esAdmin && !esCliente) {
            throw ApiException.prohibido("No puedes cancelar este pedido");
        }

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw ApiException.solicitudInvalida(
                    "Solo puedes cancelar mientras el negocio no haya aceptado el pedido");
        }

        cancelarConReembolso(pedido, esAdmin ? "Cancelado por el administrador" : "Cancelado por el cliente");

        Pedido guardado = pedidoRepository.save(pedido);

        if (guardado.getNegocio() != null) {
            notificacionService.crear(
                    guardado.getNegocio().getPropietario(),
                    guardado,
                    "Pedido cancelado",
                    "El pedido #" + guardado.getId() + " fue cancelado"
            );
        }

        if (esAdmin) {
            notificarCliente(guardado, "Pedido cancelado",
                    "Tu pedido #" + guardado.getId() + " fue cancelado. Te devolveremos tu dinero");
        }

        return guardado;
    }

    @Transactional
    public Pedido actualizarTiempo(Usuario usuario, Long id, Integer minutos) {

        Pedido pedido = obtenerParaNegocio(usuario, id);

        if (!ESTADOS_ACTIVOS.contains(pedido.getEstado())
                || pedido.getEstado() == EstadoPedido.LISTO_PARA_RECOGER) {
            throw ApiException.solicitudInvalida("Ya no se puede cambiar el tiempo de este pedido");
        }

        validarMinutos(minutos);
        pedido.setTiempoPreparacion(minutos);

        Pedido guardado = pedidoRepository.save(pedido);

        if (guardado.getEstado() != EstadoPedido.PENDIENTE) {
            notificarCliente(guardado, "Tiempo actualizado",
                    "Tu pedido #" + guardado.getId() + " estará listo en unos " + minutos + " minutos");
        }

        return guardado;
    }

    private void cancelarConReembolso(Pedido pedido, String motivo) {

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setMotivoCancelacion(motivo);

        pagoRepository.findByPedidoId(pedido.getId()).ifPresent(pago -> {
            if (pago.getEstado() == EstadoPago.PAGADO) {
                pago.setEstado(EstadoPago.REEMBOLSADO);
                pagoRepository.save(pago);
            }
        });
    }

    private Pedido obtenerParaNegocio(Usuario usuario, Long id) {

        Pedido pedido = buscar(id);

        if (usuarioActualService.esAdmin(usuario) || esDuenoDelNegocio(usuario, pedido)) {
            return pedido;
        }

        throw ApiException.prohibido("Este pedido no pertenece a tu negocio");
    }

    private boolean esDuenoDelNegocio(Usuario usuario, Pedido pedido) {
        return pedido.getNegocio() != null
                && pedido.getNegocio().getPropietario() != null
                && pedido.getNegocio().getPropietario().getId().equals(usuario.getId());
    }

    private Pedido buscar(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("Pedido no encontrado"));
    }

    private void validarEstado(Pedido pedido, EstadoPedido esperado, String mensaje) {
        if (pedido.getEstado() != esperado) {
            throw ApiException.solicitudInvalida(mensaje);
        }
    }

    private void validarMinutos(Integer minutos) {
        if (minutos == null || minutos < 1 || minutos > 240) {
            throw ApiException.solicitudInvalida("El tiempo debe estar entre 1 y 240 minutos");
        }
    }

    private void notificarCliente(Pedido pedido, String titulo, String mensaje) {
        notificacionService.crear(pedido.getUsuario(), pedido, titulo, mensaje);
    }

    private String resumenProductos(Pedido pedido) {

        StringBuilder texto = new StringBuilder();

        for (DetallePedido detalle : pedido.getDetalles()) {
            if (!texto.isEmpty()) {
                texto.append(", ");
            }
            texto.append(detalle.getCantidad()).append("x ").append(detalle.getProducto().getNombre());
        }

        return texto.toString();
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
