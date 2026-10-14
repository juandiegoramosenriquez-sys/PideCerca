package com.pidecerca.backend.service;

import com.pidecerca.backend.entity.EstadoPago;
import com.pidecerca.backend.entity.EstadoPedido;
import com.pidecerca.backend.entity.Notificacion;
import com.pidecerca.backend.entity.Pago;
import com.pidecerca.backend.entity.Pedido;
import com.pidecerca.backend.repository.PagoRepository;
import com.pidecerca.backend.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;
    private final NotificacionService notificacionService;

    public PedidoService(
            PedidoRepository pedidoRepository,
            PagoRepository pagoRepository,
            NotificacionService notificacionService) {

        this.pedidoRepository = pedidoRepository;
        this.pagoRepository = pagoRepository;
        this.notificacionService = notificacionService;
    }

    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    public Pedido obtenerPorId(Long id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    public Pedido guardar(Pedido pedido) {

        if (pedido.getId() == null) {

            pedido.setFecha(LocalDateTime.now());
            pedido.setEstado(EstadoPedido.PENDIENTE);
            pedido.setTotal(null);

        } else {

            Pedido pedidoExistente = pedidoRepository.findById(pedido.getId())
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            pedido.setFecha(pedidoExistente.getFecha());
            pedido.setEstado(pedidoExistente.getEstado());
            pedido.setTotal(pedidoExistente.getTotal());
            pedido.setUsuario(pedidoExistente.getUsuario());
            pedido.setNegocio(pedidoExistente.getNegocio());
        }

        return pedidoRepository.save(pedido);
    }

    public Pedido aceptar(Long id) {

        Pedido pedido = obtenerPorId(id);

        if (pedido == null) {
            return null;
        }

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new RuntimeException("El pedido no puede ser aceptado");
        }

        Pago pago = pagoRepository.findByPedidoId(id).orElse(null);

        if (pago == null) {
            throw new RuntimeException("El pedido no tiene un pago registrado");
        }

        if (pago.getEstado() != EstadoPago.PAGADO) {
            throw new RuntimeException("El pedido no está pagado");
        }

        pedido.setEstado(EstadoPedido.ACEPTADO);

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        crearNotificacion(
                pedidoGuardado,
                "Tu pedido ha sido aceptado"
        );

        return pedidoGuardado;
    }

    public Pedido iniciarPreparacion(Long id) {

        Pedido pedido = obtenerPorId(id);

        if (pedido == null) {
            return null;
        }

        if (pedido.getEstado() != EstadoPedido.ACEPTADO) {
            throw new RuntimeException("El pedido no puede pasar a preparación");
        }

        pedido.setEstado(EstadoPedido.EN_PREPARACION);

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        crearNotificacion(
                pedidoGuardado,
                "Tu pedido está en preparación"
        );

        return pedidoGuardado;
    }

    public Pedido marcarListo(Long id) {

        Pedido pedido = obtenerPorId(id);

        if (pedido == null) {
            return null;
        }

        if (pedido.getEstado() != EstadoPedido.EN_PREPARACION) {
            throw new RuntimeException("El pedido no puede marcarse como listo");
        }

        pedido.setEstado(EstadoPedido.LISTO_PARA_RECOGER);

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        crearNotificacion(
                pedidoGuardado,
                "Tu pedido está listo para recoger"
        );

        return pedidoGuardado;
    }

    public Pedido marcarRecogido(Long id) {

        Pedido pedido = obtenerPorId(id);

        if (pedido == null) {
            return null;
        }

        if (pedido.getEstado() != EstadoPedido.LISTO_PARA_RECOGER) {
            throw new RuntimeException("El pedido no puede marcarse como recogido");
        }

        pedido.setEstado(EstadoPedido.RECOGIDO);

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        crearNotificacion(
                pedidoGuardado,
                "Tu pedido ha sido recogido"
        );

        return pedidoGuardado;
    }

    public Pedido cancelar(Long id) {

        Pedido pedido = obtenerPorId(id);

        if (pedido == null) {
            return null;
        }

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new RuntimeException("El pedido no puede cancelarse");
        }

        pedido.setEstado(EstadoPedido.CANCELADO);

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        Pago pago = pagoRepository.findByPedidoId(id).orElse(null);

        if (pago != null && pago.getEstado() == EstadoPago.PAGADO) {
            pago.setEstado(EstadoPago.REEMBOLSADO);
            pagoRepository.save(pago);
        }

        crearNotificacion(
                pedidoGuardado,
                "Tu pedido ha sido cancelado"
        );

        return pedidoGuardado;
    }

    public void eliminar(Long id) {
        pedidoRepository.deleteById(id);
    }

    private void crearNotificacion(
            Pedido pedido,
            String mensaje) {

        if (pedido.getUsuario() == null) {
            return;
        }

        Notificacion notificacion = new Notificacion();

        notificacion.setMensaje(mensaje);
        notificacion.setLeida(false);
        notificacion.setFecha(LocalDateTime.now());
        notificacion.setUsuario(pedido.getUsuario());
        notificacion.setPedido(pedido);

        notificacionService.guardar(notificacion);
    }
}