package com.pidecerca.backend.service;

import com.pidecerca.backend.entity.Pago;
import com.pidecerca.backend.exception.ApiException;
import com.pidecerca.backend.repository.PagoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;

    public PagoService(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    public List<Pago> listar() {
        return pagoRepository.findAll();
    }

    public Pago obtenerPorId(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("Pago no encontrado"));
    }

    public Pago obtenerPorPedido(Long pedidoId) {
        return pagoRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> ApiException.noEncontrado("El pedido no tiene pago registrado"));
    }
}
