package com.pidecerca.backend.controller;

import com.pidecerca.backend.dto.CambiarPasswordRequest;
import com.pidecerca.backend.dto.UsuarioResponse;
import com.pidecerca.backend.entity.Usuario;
import com.pidecerca.backend.service.UsuarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {

        return usuarioService.listar()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtenerPorId(@PathVariable Long id) {

        return convertirRespuesta(
                usuarioService.obtenerPorId(id)
        );
    }

    @PostMapping
    public UsuarioResponse guardar(@RequestBody Usuario usuario) {

        return convertirRespuesta(
                usuarioService.guardar(usuario)
        );
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(
            @PathVariable Long id,
            @RequestBody Usuario usuario) {

        usuario.setId(id);

        return convertirRespuesta(
                usuarioService.guardar(usuario)
        );
    }

    @PutMapping("/{id}/password")
    public void cambiarPassword(
            @PathVariable Long id,
            @RequestBody CambiarPasswordRequest request) {

        usuarioService.cambiarPassword(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
    }

    private UsuarioResponse convertirRespuesta(Usuario usuario) {

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getRol(),
                usuario.getActivo()
        );
    }
}