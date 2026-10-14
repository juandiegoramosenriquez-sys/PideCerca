package com.pidecerca.backend.controller;

import com.pidecerca.backend.entity.Producto;
import com.pidecerca.backend.service.ProductoService;
import com.pidecerca.backend.service.UsuarioActualService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final UsuarioActualService usuarioActualService;

    public ProductoController(ProductoService productoService, UsuarioActualService usuarioActualService) {
        this.productoService = productoService;
        this.usuarioActualService = usuarioActualService;
    }

    @GetMapping
    public List<Producto> listar() {
        return productoService.listarTodos();
    }

    @GetMapping("/mios")
    public List<Producto> listarMios() {
        return productoService.listarMios(usuarioActualService.obtener());
    }

    @GetMapping("/negocio/{negocioId}")
    public List<Producto> listarPorNegocio(
            @PathVariable Long negocioId,
            @RequestParam(defaultValue = "false") boolean soloDisponibles) {

        return productoService.listarPorNegocio(negocioId, soloDisponibles);
    }

    @GetMapping("/{id}")
    public Producto obtenerPorId(@PathVariable Long id) {
        return productoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Producto crear(@RequestBody Producto producto) {
        return productoService.crear(usuarioActualService.obtener(), producto);
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        return productoService.actualizar(usuarioActualService.obtener(), id, producto);
    }

    @PutMapping("/{id}/disponible")
    public Producto cambiarDisponible(@PathVariable Long id, @RequestParam boolean valor) {
        return productoService.cambiarDisponible(usuarioActualService.obtener(), id, valor);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> eliminar(@PathVariable Long id) {
        return productoService.eliminar(usuarioActualService.obtener(), id);
    }
}
