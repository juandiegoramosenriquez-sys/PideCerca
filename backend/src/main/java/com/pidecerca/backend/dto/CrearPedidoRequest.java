package com.pidecerca.backend.dto;

import java.util.List;

public record CrearPedidoRequest(
        Long negocioId,
        String notas,
        String metodoPago,
        List<ItemPedidoRequest> items) {
}
