package com.pidecerca.backend.service;

import com.pidecerca.backend.entity.Categoria;
import com.pidecerca.backend.entity.Negocio;
import com.pidecerca.backend.entity.Usuario;
import com.pidecerca.backend.exception.ApiException;
import com.pidecerca.backend.repository.CategoriaRepository;
import com.pidecerca.backend.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final NegocioService negocioService;
    private final UsuarioActualService usuarioActualService;

    public CategoriaService(
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository,
            NegocioService negocioService,
            UsuarioActualService usuarioActualService) {

        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.negocioService = negocioService;
        this.usuarioActualService = usuarioActualService;
    }

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public List<Categoria> listarGenerales() {
        return categoriaRepository.findByNegocioIsNullOrderByNombreAsc();
    }

    public List<Categoria> listarParaNegocio(Long negocioId) {
        return categoriaRepository.findDisponiblesParaNegocio(negocioId);
    }

    public List<Categoria> listarMias(Usuario usuario) {
        Negocio negocio = negocioService.obtenerDelPropietario(usuario);
        return categoriaRepository.findDisponiblesParaNegocio(negocio.getId());
    }

    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("Categoría no encontrada"));
    }

    public Categoria crear(Usuario usuario, Categoria datos) {

        Categoria categoria = new Categoria();
        copiarDatos(datos, categoria);
        categoria.setActivo(datos.getActivo() == null || datos.getActivo());

        if (!usuarioActualService.esAdmin(usuario)) {
            categoria.setNegocio(negocioService.obtenerDelPropietario(usuario));
        }

        return categoriaRepository.save(categoria);
    }

    public Categoria actualizar(Usuario usuario, Long id, Categoria datos) {

        Categoria categoria = obtenerPorId(id);
        validarPermiso(usuario, categoria);
        copiarDatos(datos, categoria);

        if (datos.getActivo() != null) {
            categoria.setActivo(datos.getActivo());
        }

        return categoriaRepository.save(categoria);
    }

    public void eliminar(Usuario usuario, Long id) {

        Categoria categoria = obtenerPorId(id);
        validarPermiso(usuario, categoria);

        if (productoRepository.existsByCategoriaId(id)) {
            throw ApiException.conflicto(
                    "Hay productos en esta categoría. Muévelos a otra antes de eliminarla");
        }

        categoriaRepository.delete(categoria);
    }

    private void validarPermiso(Usuario usuario, Categoria categoria) {

        if (usuarioActualService.esAdmin(usuario)) {
            return;
        }

        if (categoria.esGeneral()) {
            throw ApiException.prohibido(
                    "Las categorías generales solo las puede cambiar el administrador");
        }

        Negocio negocio = negocioService.obtenerDelPropietario(usuario);

        if (!categoria.getNegocio().getId().equals(negocio.getId())) {
            throw ApiException.prohibido("Esta categoría no pertenece a tu negocio");
        }
    }

    private void copiarDatos(Categoria origen, Categoria destino) {

        String nombre = AuthService.texto(origen.getNombre());

        if (nombre == null) {
            throw ApiException.solicitudInvalida("El nombre de la categoría es obligatorio");
        }

        destino.setNombre(nombre);
        destino.setDescripcion(AuthService.texto(origen.getDescripcion()));
    }
}
