package com.pidecerca.backend.service;

import com.pidecerca.backend.entity.Notificacion;
import com.pidecerca.backend.repository.NotificacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    public List<Notificacion> listar() {
        return notificacionRepository.findAll();
    }

    public Notificacion obtenerPorId(Long id) {
        return notificacionRepository.findById(id).orElse(null);
    }

    public Notificacion guardar(Notificacion notificacion) {
        return notificacionRepository.save(notificacion);
    }

    public void eliminar(Long id) {
        notificacionRepository.deleteById(id);
    }

    public List<Notificacion> listarPorUsuario(Long usuarioId) {
        return notificacionRepository.findByUsuarioId(usuarioId);
    }

    public List<Notificacion> listarPorPedido(Long pedidoId) {
        return notificacionRepository.findByPedidoId(pedidoId);
    }

    public void marcarComoLeida(Long id) {

        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Notificación no encontrada"));

        notificacion.setLeida(true);

        notificacionRepository.save(notificacion);
    }
}