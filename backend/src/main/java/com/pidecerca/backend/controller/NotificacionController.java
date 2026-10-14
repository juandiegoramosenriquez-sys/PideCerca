package com.pidecerca.backend.controller;

import com.pidecerca.backend.entity.Notificacion;
import com.pidecerca.backend.service.NotificacionService;
import com.pidecerca.backend.service.UsuarioActualService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final UsuarioActualService usuarioActualService;

    public NotificacionController(
            NotificacionService notificacionService,
            UsuarioActualService usuarioActualService) {

        this.notificacionService = notificacionService;
        this.usuarioActualService = usuarioActualService;
    }

    @GetMapping
    public List<Notificacion> listarMias() {
        return notificacionService.listarDe(usuarioActualService.obtener());
    }

    @GetMapping("/no-leidas")
    public Map<String, Long> contarNoLeidas() {
        return notificacionService.contarNoLeidas(usuarioActualService.obtener());
    }

    @PutMapping("/{id}/leer")
    public Notificacion marcarComoLeida(@PathVariable Long id) {
        return notificacionService.marcarComoLeida(id, usuarioActualService.obtener());
    }

    @PutMapping("/leer-todas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarTodas() {
        notificacionService.marcarTodasComoLeidas(usuarioActualService.obtener());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        notificacionService.eliminar(id, usuarioActualService.obtener());
    }
}
