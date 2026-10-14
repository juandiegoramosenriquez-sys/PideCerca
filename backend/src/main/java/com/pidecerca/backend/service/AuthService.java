package com.pidecerca.backend.service;

import com.pidecerca.backend.entity.Usuario;
import com.pidecerca.backend.repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public Usuario registrar(Usuario usuario) {

        if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        usuario.setPassword(
                passwordEncoder.encode(usuario.getPassword())
        );

        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    public String login(String correo, String password) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        correo,
                        password
                )
        );

        return jwtService.generarToken(correo);
    }

    public Usuario obtenerUsuario(String correo) {

        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));
    }
}