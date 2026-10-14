package com.pidecerca.backend.controller;

import com.pidecerca.backend.entity.DetallePedido;
import com.pidecerca.backend.service.DetallePedidoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detalles-pedido")
public class DetallePedidoController {

    private final DetallePedidoService detallePedidoService;

    public DetallePedidoController(DetallePedidoService detallePedidoService) {
        this.detallePedidoService = detallePedidoService;
    }

    @GetMapping
    public List<DetallePedido> listar() {
        return detallePedidoService.listar();
    }

    @GetMapping("/pedido/{pedidoId}")
    public List<DetallePedido> listarPorPedido(@PathVariable Long pedidoId) {
        return detallePedidoService.listarPorPedido(pedidoId);
    }
}
