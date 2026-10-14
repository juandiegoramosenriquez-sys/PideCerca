package com.pidecerca.backend.service;

import com.pidecerca.backend.dto.CambiarPasswordRequest;
import com.pidecerca.backend.entity.Usuario;
import com.pidecerca.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }

    public void cambiarPassword(
            Long id,
            CambiarPasswordRequest request) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        boolean coincide = passwordEncoder.matches(
                request.getPasswordActual(),
                usuario.getPassword()
        );

        if (!coincide) {
            throw new RuntimeException(
                    "La contraseña actual es incorrecta");
        }

        usuario.setPassword(
                passwordEncoder.encode(
                        request.getNuevaPassword()
                )
        );

        usuarioRepository.save(usuario);
    }
}