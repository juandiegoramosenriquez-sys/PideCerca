package com.pidecerca.backend.service;

import com.pidecerca.backend.entity.DetallePedido;
import com.pidecerca.backend.entity.Pedido;
import com.pidecerca.backend.entity.Producto;
import com.pidecerca.backend.repository.DetallePedidoRepository;
import com.pidecerca.backend.repository.PedidoRepository;
import com.pidecerca.backend.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetallePedidoService {

    private final DetallePedidoRepository detallePedidoRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;

    public DetallePedidoService(
            DetallePedidoRepository detallePedidoRepository,
            ProductoRepository productoRepository,
            PedidoRepository pedidoRepository) {

        this.detallePedidoRepository = detallePedidoRepository;
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public List<DetallePedido> listar() {
        return detallePedidoRepository.findAll();
    }

    public DetallePedido obtenerPorId(Long id) {
        return detallePedidoRepository.findById(id).orElse(null);
    }

    public DetallePedido guardar(DetallePedido detallePedido) {

        if (detallePedido.getCantidad() == null || detallePedido.getCantidad() <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor que 0");
        }

        if (detallePedido.getProducto() == null || detallePedido.getProducto().getId() == null) {
            throw new RuntimeException("Debe indicar un producto");
        }

        if (detallePedido.getPedido() == null || detallePedido.getPedido().getId() == null) {
            throw new RuntimeException("Debe indicar un pedido");
        }

        Producto producto = productoRepository.findById(
                detallePedido.getProducto().getId()
        ).orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (Boolean.FALSE.equals(producto.getDisponible())) {
            throw new RuntimeException("El producto no está disponible");
        }

        Pedido pedido = pedidoRepository.findById(
                detallePedido.getPedido().getId()
        ).orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (pedido.getEstado() != com.pidecerca.backend.entity.EstadoPedido.PENDIENTE) {
            throw new RuntimeException("No se pueden agregar productos a este pedido");
        }

        detallePedido.setPrecioUnitario(producto.getPrecio());

        double subtotal =
                detallePedido.getCantidad() * producto.getPrecio();

        detallePedido.setSubtotal(subtotal);

        DetallePedido detalleGuardado =
                detallePedidoRepository.save(detallePedido);

        List<DetallePedido> detalles =
                detallePedidoRepository.findByPedidoId(pedido.getId());

        double total = 0;

        for (DetallePedido detalle : detalles) {
            total += detalle.getSubtotal();
        }

        pedido.setTotal(total);

        pedidoRepository.save(pedido);

        return detalleGuardado;
    }

    public void eliminar(Long id) {
        detallePedidoRepository.deleteById(id);
    }
}

