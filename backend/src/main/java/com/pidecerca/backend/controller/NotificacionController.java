package com.pidecerca.backend.controller;

import com.pidecerca.backend.entity.Notificacion;
import com.pidecerca.backend.service.NotificacionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping
    public List<Notificacion> listar() {
        return notificacionService.listar();
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Notificacion> listarPorUsuario(
            @PathVariable Long usuarioId) {

        return notificacionService.listarPorUsuario(usuarioId);
    }

    @GetMapping("/pedido/{pedidoId}")
    public List<Notificacion> listarPorPedido(
            @PathVariable Long pedidoId) {

        return notificacionService.listarPorPedido(pedidoId);
    }

    @GetMapping("/{id}")
    public Notificacion obtenerPorId(@PathVariable Long id) {
        return notificacionService.obtenerPorId(id);
    }

    @PostMapping
    public Notificacion guardar(@RequestBody Notificacion notificacion) {
        return notificacionService.guardar(notificacion);
    }

    @PutMapping("/{id}")
    public Notificacion actualizar(
            @PathVariable Long id,
            @RequestBody Notificacion notificacion) {

        notificacion.setId(id);
        return notificacionService.guardar(notificacion);
    }

    @PutMapping("/{id}/leer")
    public void marcarComoLeida(@PathVariable Long id) {
        notificacionService.marcarComoLeida(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        notificacionService.eliminar(id);
    }
}