package com.pidecerca.backend.controller;

import com.pidecerca.backend.dto.LoginRequest;
import com.pidecerca.backend.dto.LoginResponse;
import com.pidecerca.backend.dto.RegistroRequest;
import com.pidecerca.backend.dto.UsuarioResponse;
import com.pidecerca.backend.entity.Usuario;
import com.pidecerca.backend.service.AuthService;
import com.pidecerca.backend.service.UsuarioActualService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final UsuarioActualService usuarioActualService;

    public AuthController(AuthService authService, UsuarioActualService usuarioActualService) {
        this.authService = authService;
        this.usuarioActualService = usuarioActualService;
    }

    @GetMapping("/salud")
    public Map<String, String> salud() {
        return Map.of("estado", "ok", "servicio", "PideCerca API");
    }

    @PostMapping("/auth/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse registrar(@RequestBody RegistroRequest request) {

        Usuario usuario = authService.registrar(request);
        String token = authService.login(usuario.getCorreo(), request.password());

        return new LoginResponse(token, UsuarioResponse.de(usuario));
    }

    @PostMapping("/auth/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        String token = authService.login(
                request.getCorreo(),
                request.getPassword()
        );

        Usuario usuario = authService.obtenerUsuario(request.getCorreo());

        return new LoginResponse(token, UsuarioResponse.de(usuario));
    }

    @GetMapping("/auth/me")
    public UsuarioResponse yo() {
        return UsuarioResponse.de(usuarioActualService.obtener());
    }
}
