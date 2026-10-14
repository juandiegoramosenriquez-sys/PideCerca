package com.pidecerca.backend.controller;

import com.pidecerca.backend.dto.LoginRequest;
import com.pidecerca.backend.dto.LoginResponse;
import com.pidecerca.backend.dto.UsuarioResponse;
import com.pidecerca.backend.entity.Usuario;
import com.pidecerca.backend.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public UsuarioResponse registrar(@RequestBody Usuario usuario) {

        Usuario usuarioRegistrado = authService.registrar(usuario);

        return new UsuarioResponse(
                usuarioRegistrado.getId(),
                usuarioRegistrado.getNombre(),
                usuarioRegistrado.getApellido(),
                usuarioRegistrado.getCorreo(),
                usuarioRegistrado.getTelefono(),
                usuarioRegistrado.getRol(),
                usuarioRegistrado.getActivo()
        );
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        String token = authService.login(
                request.getCorreo(),
                request.getPassword()
        );

        Usuario usuario = authService.obtenerUsuario(
                request.getCorreo()
        );

        UsuarioResponse usuarioResponse = new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getRol(),
                usuario.getActivo()
        );

        return new LoginResponse(
                token,
                usuarioResponse
        );
    }
}