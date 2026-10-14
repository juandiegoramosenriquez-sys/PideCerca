package com.pidecerca.backend.service;

import com.pidecerca.backend.dto.RegistroRequest;
import com.pidecerca.backend.entity.Rol;
import com.pidecerca.backend.entity.Usuario;
import com.pidecerca.backend.exception.ApiException;
import com.pidecerca.backend.repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public Usuario registrar(RegistroRequest request) {
        return crearUsuario(request, Rol.CLIENTE);
    }

    public Usuario crearUsuario(RegistroRequest request, String rol) {

        if (request == null) {
            throw ApiException.solicitudInvalida("Faltan los datos del usuario");
        }

        if (texto(request.nombre()) == null) {
            throw ApiException.solicitudInvalida("El nombre es obligatorio");
        }

        String correo = normalizarCorreo(request.correo());

        if (correo == null || !correo.contains("@")) {
            throw ApiException.solicitudInvalida("Ingresa un correo válido");
        }

        if (request.password() == null || request.password().length() < 6) {
            throw ApiException.solicitudInvalida("La contraseña debe tener al menos 6 caracteres");
        }

        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw ApiException.conflicto("El correo ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(texto(request.nombre()));
        usuario.setApellido(texto(request.apellido()));
        usuario.setCorreo(correo);
        usuario.setTelefono(texto(request.telefono()));
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setFechaRegistro(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    public String login(String correo, String password) {

        String correoNormalizado = normalizarCorreo(correo);

        if (correoNormalizado == null || password == null) {
            throw ApiException.solicitudInvalida("Ingresa tu correo y contraseña");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        correoNormalizado,
                        password
                )
        );

        Usuario usuario = obtenerUsuario(correoNormalizado);

        return jwtService.generarToken(usuario.getCorreo(), usuario.getRol());
    }

    public Usuario obtenerUsuario(String correo) {

        return usuarioRepository.findByCorreoIgnoreCase(normalizarCorreo(correo))
                .orElseThrow(() ->
                        ApiException.noEncontrado("Usuario no encontrado"));
    }

    static String normalizarCorreo(String correo) {
        String valor = texto(correo);
        return valor == null ? null : valor.toLowerCase();
    }

    static String texto(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
