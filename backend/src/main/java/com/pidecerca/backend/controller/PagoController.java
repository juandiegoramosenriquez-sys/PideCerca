package com.pidecerca.backend.controller;

import com.pidecerca.backend.entity.Pago;
import com.pidecerca.backend.service.PagoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping
    public List<Pago> listar() {
        return pagoService.listar();
    }

    @GetMapping("/{id}")
    public Pago obtenerPorId(@PathVariable Long id) {
        return pagoService.obtenerPorId(id);
    }

    @GetMapping("/pedido/{pedidoId}")
    public Pago obtenerPorPedido(@PathVariable Long pedidoId) {
        return pagoService.obtenerPorPedido(pedidoId);
    }
}
