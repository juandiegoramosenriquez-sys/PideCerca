package com.pidecerca.backend.service;

import com.pidecerca.backend.entity.Notificacion;
import com.pidecerca.backend.entity.Pedido;
import com.pidecerca.backend.entity.Usuario;
import com.pidecerca.backend.exception.ApiException;
import com.pidecerca.backend.repository.NotificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioActualService usuarioActualService;

    public NotificacionService(
            NotificacionRepository notificacionRepository,
            UsuarioActualService usuarioActualService) {

        this.notificacionRepository = notificacionRepository;
        this.usuarioActualService = usuarioActualService;
    }

    public Notificacion crear(Usuario usuario, Pedido pedido, String titulo, String mensaje) {

        if (usuario == null) {
            return null;
        }

        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setPedido(pedido);
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacion.setLeida(false);
        notificacion.setFecha(LocalDateTime.now());

        return notificacionRepository.save(notificacion);
    }

    public List<Notificacion> listarTodas() {
        return notificacionRepository.findAll();
    }

    public List<Notificacion> listarDe(Usuario usuario) {
        return notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuario.getId());
    }

    public Map<String, Long> contarNoLeidas(Usuario usuario) {
        return Map.of("noLeidas", notificacionRepository.countByUsuarioIdAndLeidaFalse(usuario.getId()));
    }

    public Notificacion marcarComoLeida(Long id, Usuario usuario) {

        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("Notificación no encontrada"));

        if (!notificacion.getUsuario().getId().equals(usuario.getId())
                && !usuarioActualService.esAdmin(usuario)) {
            throw ApiException.prohibido("Esta notificación no es tuya");
        }

        notificacion.setLeida(true);
        return notificacionRepository.save(notificacion);
    }

    @Transactional
    public void marcarTodasComoLeidas(Usuario usuario) {

        List<Notificacion> pendientes =
                notificacionRepository.findByUsuarioIdAndLeidaFalse(usuario.getId());

        pendientes.forEach(n -> n.setLeida(true));
        notificacionRepository.saveAll(pendientes);
    }

    public void eliminar(Long id, Usuario usuario) {

        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("Notificación no encontrada"));

        if (!notificacion.getUsuario().getId().equals(usuario.getId())
                && !usuarioActualService.esAdmin(usuario)) {
            throw ApiException.prohibido("Esta notificación no es tuya");
        }

        notificacionRepository.delete(notificacion);
    }
}
