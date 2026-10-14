package com.pidecerca.backend.service;

import com.pidecerca.backend.entity.Categoria;
import com.pidecerca.backend.entity.Negocio;
import com.pidecerca.backend.entity.Producto;
import com.pidecerca.backend.entity.Usuario;
import com.pidecerca.backend.exception.ApiException;
import com.pidecerca.backend.repository.CategoriaRepository;
import com.pidecerca.backend.repository.DetallePedidoRepository;
import com.pidecerca.backend.repository.NegocioRepository;
import com.pidecerca.backend.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final NegocioRepository negocioRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final NegocioService negocioService;
    private final UsuarioActualService usuarioActualService;

    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            NegocioRepository negocioRepository,
            DetallePedidoRepository detallePedidoRepository,
            NegocioService negocioService,
            UsuarioActualService usuarioActualService) {

        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.negocioRepository = negocioRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.negocioService = negocioService;
        this.usuarioActualService = usuarioActualService;
    }

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public List<Producto> listarPorNegocio(Long negocioId, boolean soloDisponibles) {

        if (soloDisponibles) {
            return productoRepository.findByNegocioIdAndDisponibleTrueOrderByNombreAsc(negocioId);
        }

        return productoRepository.findByNegocioIdOrderByNombreAsc(negocioId);
    }

    public List<Producto> listarMios(Usuario usuario) {
        Negocio negocio = negocioService.obtenerDelPropietario(usuario);
        return productoRepository.findByNegocioIdOrderByNombreAsc(negocio.getId());
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("Producto no encontrado"));
    }

    public Producto crear(Usuario usuario, Producto datos) {

        Negocio negocio;

        if (usuarioActualService.esAdmin(usuario)) {
            if (datos.getNegocio() == null || datos.getNegocio().getId() == null) {
                throw ApiException.solicitudInvalida("Indica el negocio del producto");
            }
            negocio = negocioRepository.findById(datos.getNegocio().getId())
                    .orElseThrow(() -> ApiException.noEncontrado("Negocio no encontrado"));
        } else {
            negocio = negocioService.obtenerDelPropietario(usuario);
        }

        Producto producto = new Producto();
        producto.setNegocio(negocio);
        copiarDatos(datos, producto, negocio);
        producto.setDisponible(datos.getDisponible() == null || datos.getDisponible());

        return productoRepository.save(producto);
    }

    public Producto actualizar(Usuario usuario, Long id, Producto datos) {

        Producto producto = obtenerPorId(id);
        validarPermiso(usuario, producto);
        copiarDatos(datos, producto, producto.getNegocio());

        if (datos.getDisponible() != null) {
            producto.setDisponible(datos.getDisponible());
        }

        return productoRepository.save(producto);
    }

    public Producto cambiarDisponible(Usuario usuario, Long id, boolean disponible) {

        Producto producto = obtenerPorId(id);
        validarPermiso(usuario, producto);
        producto.setDisponible(disponible);

        return productoRepository.save(producto);
    }

    public Map<String, String> eliminar(Usuario usuario, Long id) {

        Producto producto = obtenerPorId(id);
        validarPermiso(usuario, producto);

        if (detallePedidoRepository.existsByProductoId(id)) {
            producto.setDisponible(false);
            productoRepository.save(producto);
            return Map.of(
                    "resultado", "DESACTIVADO",
                    "mensaje", "El producto ya tiene pedidos, así que se marcó como no disponible");
        }

        productoRepository.delete(producto);
        return Map.of("resultado", "ELIMINADO", "mensaje", "Producto eliminado");
    }

    private void validarPermiso(Usuario usuario, Producto producto) {

        if (usuarioActualService.esAdmin(usuario)) {
            return;
        }

        Negocio negocio = negocioService.obtenerDelPropietario(usuario);

        if (producto.getNegocio() == null || !producto.getNegocio().getId().equals(negocio.getId())) {
            throw ApiException.prohibido("Este producto no pertenece a tu negocio");
        }
    }

    private void copiarDatos(Producto origen, Producto destino, Negocio negocio) {

        String nombre = AuthService.texto(origen.getNombre());

        if (nombre == null) {
            throw ApiException.solicitudInvalida("El nombre del producto es obligatorio");
        }

        if (origen.getPrecio() == null || origen.getPrecio() <= 0) {
            throw ApiException.solicitudInvalida("El precio debe ser mayor que 0");
        }

        destino.setNombre(nombre);
        destino.setDescripcion(AuthService.texto(origen.getDescripcion()));
        destino.setPrecio(Math.round(origen.getPrecio() * 100.0) / 100.0);
        destino.setImagen(AuthService.texto(origen.getImagen()));

        if (origen.getCategoria() == null || origen.getCategoria().getId() == null) {
            destino.setCategoria(null);
            return;
        }

        Categoria categoria = categoriaRepository.findById(origen.getCategoria().getId())
                .orElseThrow(() -> ApiException.noEncontrado("Categoría no encontrada"));

        if (!categoria.esGeneral() && !categoria.getNegocio().getId().equals(negocio.getId())) {
            throw ApiException.prohibido("Esa categoría pertenece a otro negocio");
        }

        destino.setCategoria(categoria);
    }
}
