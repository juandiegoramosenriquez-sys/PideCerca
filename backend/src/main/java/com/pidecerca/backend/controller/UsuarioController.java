package com.pidecerca.backend.controller;

import com.pidecerca.backend.dto.CambiarPasswordRequest;
import com.pidecerca.backend.dto.PerfilRequest;
import com.pidecerca.backend.dto.UsuarioRequest;
import com.pidecerca.backend.dto.UsuarioResponse;
import com.pidecerca.backend.service.UsuarioActualService;
import com.pidecerca.backend.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioActualService usuarioActualService;

    public UsuarioController(UsuarioService usuarioService, UsuarioActualService usuarioActualService) {
        this.usuarioService = usuarioService;
        this.usuarioActualService = usuarioActualService;
    }

    @GetMapping("/me")
    public UsuarioResponse miPerfil() {
        return UsuarioResponse.de(usuarioActualService.obtener());
    }

    @PutMapping("/me")
    public UsuarioResponse actualizarMiPerfil(@RequestBody PerfilRequest request) {
        return UsuarioResponse.de(
                usuarioService.actualizarPerfil(usuarioActualService.obtener(), request));
    }

    @PutMapping("/me/password")
    public void cambiarMiPassword(@RequestBody CambiarPasswordRequest request) {
        usuarioService.cambiarPassword(usuarioActualService.obtener(), request);
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar().stream().map(UsuarioResponse::de).toList();
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtenerPorId(@PathVariable Long id) {
        return UsuarioResponse.de(usuarioService.obtenerPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@RequestBody UsuarioRequest request) {
        return UsuarioResponse.de(usuarioService.crear(request));
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @RequestBody UsuarioRequest request) {
        return UsuarioResponse.de(
                usuarioService.actualizar(id, request, usuarioActualService.obtener()));
    }

    @PutMapping("/{id}/activo")
    public UsuarioResponse cambiarActivo(@PathVariable Long id, @RequestParam boolean valor) {
        return UsuarioResponse.de(
                usuarioService.cambiarActivo(id, valor, usuarioActualService.obtener()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id, usuarioActualService.obtener());
    }
}
