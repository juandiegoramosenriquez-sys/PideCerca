package com.pidecerca.backend.controller;

import com.pidecerca.backend.entity.Categoria;
import com.pidecerca.backend.service.CategoriaService;
import com.pidecerca.backend.service.UsuarioActualService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final UsuarioActualService usuarioActualService;

    public CategoriaController(CategoriaService categoriaService, UsuarioActualService usuarioActualService) {
        this.categoriaService = categoriaService;
        this.usuarioActualService = usuarioActualService;
    }

    @GetMapping
    public List<Categoria> listar(@RequestParam(defaultValue = "false") boolean todas) {

        if (todas && usuarioActualService.esAdmin(usuarioActualService.obtener())) {
            return categoriaService.listarTodas();
        }

        return categoriaService.listarGenerales();
    }

    @GetMapping("/mias")
    public List<Categoria> listarMias() {
        return categoriaService.listarMias(usuarioActualService.obtener());
    }

    @GetMapping("/negocio/{negocioId}")
    public List<Categoria> listarPorNegocio(@PathVariable Long negocioId) {
        return categoriaService.listarParaNegocio(negocioId);
    }

    @GetMapping("/{id}")
    public Categoria obtenerPorId(@PathVariable Long id) {
        return categoriaService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Categoria crear(@RequestBody Categoria categoria) {
        return categoriaService.crear(usuarioActualService.obtener(), categoria);
    }

    @PutMapping("/{id}")
    public Categoria actualizar(@PathVariable Long id, @RequestBody Categoria categoria) {
        return categoriaService.actualizar(usuarioActualService.obtener(), id, categoria);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        categoriaService.eliminar(usuarioActualService.obtener(), id);
    }
}
