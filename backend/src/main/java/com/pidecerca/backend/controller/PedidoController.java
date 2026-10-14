package com.pidecerca.backend.controller;

import com.pidecerca.backend.entity.Pedido;
import com.pidecerca.backend.service.PedidoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<Pedido> listar() {
        return pedidoService.listar();
    }

    @GetMapping("/{id}")
    public Pedido obtenerPorId(@PathVariable Long id) {
        return pedidoService.obtenerPorId(id);
    }

    @PostMapping
    public Pedido guardar(@RequestBody Pedido pedido) {
        return pedidoService.guardar(pedido);
    }

    @PutMapping("/{id}")
    public Pedido actualizar(@PathVariable Long id, @RequestBody Pedido pedido) {
        pedido.setId(id);
        return pedidoService.guardar(pedido);
    }

    @PostMapping("/{id}/aceptar")
    public Pedido aceptar(@PathVariable Long id) {
        return pedidoService.aceptar(id);
    }

    @PostMapping("/{id}/preparar")
    public Pedido iniciarPreparacion(@PathVariable Long id) {
        return pedidoService.iniciarPreparacion(id);
    }

    @PostMapping("/{id}/listo")
    public Pedido marcarListo(@PathVariable Long id) {
        return pedidoService.marcarListo(id);
    }

    @PostMapping("/{id}/recogido")
    public Pedido marcarRecogido(@PathVariable Long id) {
        return pedidoService.marcarRecogido(id);
    }

    @PostMapping("/{id}/cancelar")
    public Pedido cancelar(@PathVariable Long id) {
        return pedidoService.cancelar(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        pedidoService.eliminar(id);
    }
}