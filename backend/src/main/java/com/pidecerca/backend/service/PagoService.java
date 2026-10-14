package com.pidecerca.backend.service;

import com.pidecerca.backend.entity.Pago;
import com.pidecerca.backend.entity.Pedido;
import com.pidecerca.backend.repository.PagoRepository;
import com.pidecerca.backend.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;

    public PagoService(
            PagoRepository pagoRepository,
            PedidoRepository pedidoRepository) {

        this.pagoRepository = pagoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public List<Pago> listar() {
        return pagoRepository.findAll();
    }

    public Pago obtenerPorId(Long id) {
        return pagoRepository.findById(id).orElse(null);
    }

    public Pago guardar(Pago pago) {

        if (pago.getPedido() == null || pago.getPedido().getId() == null) {
            throw new RuntimeException("Debe indicar un pedido");
        }

        Pedido pedido = pedidoRepository.findById(
                pago.getPedido().getId()
        ).orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (pedido.getTotal() == null) {
            throw new RuntimeException("El pedido todavía no tiene un total");
        }

        if (pago.getMonto() == null || pago.getMonto() <= 0) {
            throw new RuntimeException("El monto del pago debe ser mayor que 0");
        }

        if (!pago.getMonto().equals(pedido.getTotal())) {
            throw new RuntimeException(
                    "El monto del pago no coincide con el total del pedido"
            );
        }

        if (pago.getId() == null && pagoRepository.existsByPedidoId(pedido.getId())) {
            throw new RuntimeException("El pedido ya tiene un pago registrado");
        }

        pago.setPedido(pedido);

        return pagoRepository.save(pago);
    }

    public void eliminar(Long id) {
        pagoRepository.deleteById(id);
    }
}