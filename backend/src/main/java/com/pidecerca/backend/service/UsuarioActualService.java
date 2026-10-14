package com.pidecerca.backend.service;

import com.pidecerca.backend.entity.Rol;
import com.pidecerca.backend.entity.Usuario;
import com.pidecerca.backend.exception.ApiException;
import com.pidecerca.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioActualService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioActualService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario obtener() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión");
        }

        return usuarioRepository.findByCorreoIgnoreCase(authentication.getName())
                .orElseThrow(() ->
                        new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    public boolean esAdmin(Usuario usuario) {
        return Rol.ADMIN.equals(usuario.getRol());
    }
}
