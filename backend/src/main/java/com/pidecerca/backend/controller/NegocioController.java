package com.pidecerca.backend.controller;

import com.pidecerca.backend.entity.Negocio;
import com.pidecerca.backend.service.NegocioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/negocios")
public class NegocioController {

    private final NegocioService negocioService;

    public NegocioController(NegocioService negocioService) {
        this.negocioService = negocioService;
    }

    @GetMapping
    public List<Negocio> listar() {
        return negocioService.listar();
    }

    @GetMapping("/{id}")
    public Negocio obtenerPorId(@PathVariable Long id) {
        return negocioService.obtenerPorId(id);
    }

    @PostMapping
    public Negocio guardar(@RequestBody Negocio negocio) {
        return negocioService.guardar(negocio);
    }

    @PutMapping("/{id}")
    public Negocio actualizar(@PathVariable Long id, @RequestBody Negocio negocio) {
        negocio.setId(id);
        return negocioService.guardar(negocio);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        negocioService.eliminar(id);
    }
}