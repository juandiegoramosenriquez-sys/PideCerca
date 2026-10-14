package com.pidecerca.backend.controller;

import com.pidecerca.backend.dto.CrearPedidoRequest;
import com.pidecerca.backend.dto.MotivoRequest;
import com.pidecerca.backend.dto.TiempoRequest;
import com.pidecerca.backend.entity.EstadoPedido;
import com.pidecerca.backend.entity.Pedido;
import com.pidecerca.backend.service.PedidoService;
import com.pidecerca.backend.service.UsuarioActualService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final UsuarioActualService usuarioActualService;

    public PedidoController(PedidoService pedidoService, UsuarioActualService usuarioActualService) {
        this.pedidoService = pedidoService;
        this.usuarioActualService = usuarioActualService;
    }

    @GetMapping
    public List<Pedido> listar(@RequestParam(required = false) EstadoPedido estado) {
        return pedidoService.listarTodos(estado);
    }

    @GetMapping("/mios")
    public List<Pedido> listarMios() {
        return pedidoService.listarDelCliente(usuarioActualService.obtener());
    }

    @GetMapping("/negocio")
    public List<Pedido> listarDelNegocio(@RequestParam(defaultValue = "false") boolean activos) {
        return pedidoService.listarDelNegocio(usuarioActualService.obtener(), activos);
    }

    @GetMapping("/{id}")
    public Pedido obtenerPorId(@PathVariable Long id) {
        return pedidoService.obtener(usuarioActualService.obtener(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pedido crear(@RequestBody CrearPedidoRequest request) {
        return pedidoService.crear(usuarioActualService.obtener(), request);
    }

    @PostMapping("/{id}/aceptar")
    public Pedido aceptar(@PathVariable Long id, @RequestBody(required = false) TiempoRequest request) {
        return pedidoService.aceptar(
                usuarioActualService.obtener(), id, request == null ? null : request.minutos());
    }

    @PostMapping("/{id}/preparar")
    public Pedido iniciarPreparacion(@PathVariable Long id) {
        return pedidoService.iniciarPreparacion(usuarioActualService.obtener(), id);
    }

    @PostMapping("/{id}/listo")
    public Pedido marcarListo(@PathVariable Long id) {
        return pedidoService.marcarListo(usuarioActualService.obtener(), id);
    }

    @PostMapping("/{id}/recogido")
    public Pedido marcarRecogido(@PathVariable Long id) {
        return pedidoService.marcarRecogido(usuarioActualService.obtener(), id);
    }

    @PostMapping("/{id}/rechazar")
    public Pedido rechazar(@PathVariable Long id, @RequestBody(required = false) MotivoRequest request) {
        return pedidoService.rechazar(
                usuarioActualService.obtener(), id, request == null ? null : request.motivo());
    }

    @PostMapping("/{id}/cancelar")
    public Pedido cancelar(@PathVariable Long id) {
        return pedidoService.cancelar(usuarioActualService.obtener(), id);
    }

    @PutMapping("/{id}/tiempo")
    public Pedido actualizarTiempo(@PathVariable Long id, @RequestBody TiempoRequest request) {
        return pedidoService.actualizarTiempo(
                usuarioActualService.obtener(), id, request == null ? null : request.minutos());
    }
}
