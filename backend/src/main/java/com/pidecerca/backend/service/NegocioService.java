package com.pidecerca.backend.service;

import com.pidecerca.backend.dto.CrearNegocioRequest;
import com.pidecerca.backend.entity.Negocio;
import com.pidecerca.backend.entity.Rol;
import com.pidecerca.backend.entity.Usuario;
import com.pidecerca.backend.exception.ApiException;
import com.pidecerca.backend.repository.NegocioRepository;
import com.pidecerca.backend.repository.PedidoRepository;
import com.pidecerca.backend.repository.ProductoRepository;
import com.pidecerca.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class NegocioService {

    private final NegocioRepository negocioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final AuthService authService;

    public NegocioService(
            NegocioRepository negocioRepository,
            UsuarioRepository usuarioRepository,
            PedidoRepository pedidoRepository,
            ProductoRepository productoRepository,
            AuthService authService) {

        this.negocioRepository = negocioRepository;
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.authService = authService;
    }

    public List<Negocio> listarTodos() {
        return negocioRepository.findAllByOrderByIdDesc();
    }

    public List<Negocio> buscar(String texto, String tipo, Double latitud, Double longitud) {

        String filtro = normalizar(texto);
        String filtroTipo = normalizar(tipo);

        List<Negocio> negocios = negocioRepository.findByActivoTrue().stream()
                .filter(n -> filtro == null
                        || contiene(n.getNombre(), filtro)
                        || contiene(n.getTipoComida(), filtro)
                        || contiene(n.getDescripcion(), filtro)
                        || contiene(n.getDireccion(), filtro))
                .filter(n -> filtroTipo == null || contiene(n.getTipoComida(), filtroTipo))
                .toList();

        if (latitud != null && longitud != null) {
            negocios.forEach(n -> {
                if (n.getLatitud() != null && n.getLongitud() != null) {
                    n.setDistanciaMetros((double) Math.round(
                            distanciaMetros(latitud, longitud, n.getLatitud(), n.getLongitud())));
                }
            });

            return negocios.stream()
                    .sorted(Comparator
                            .comparing((Negocio n) -> !Boolean.TRUE.equals(n.getAbierto()))
                            .thenComparing(n -> n.getDistanciaMetros() == null
                                    ? Double.MAX_VALUE
                                    : n.getDistanciaMetros()))
                    .toList();
        }

        return negocios.stream()
                .sorted(Comparator
                        .comparing((Negocio n) -> !Boolean.TRUE.equals(n.getAbierto()))
                        .thenComparing(Negocio::getNombre, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    public Negocio obtenerPorId(Long id) {
        return negocioRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("Negocio no encontrado"));
    }

    public Negocio obtenerDelPropietario(Usuario usuario) {
        return negocioRepository.findFirstByPropietarioId(usuario.getId())
                .orElseThrow(() -> ApiException.noEncontrado(
                        "Tu cuenta todavía no tiene un negocio asignado. Pídele al administrador que lo registre"));
    }

    @Transactional
    public Negocio crear(CrearNegocioRequest request) {

        if (request == null || request.negocio() == null) {
            throw ApiException.solicitudInvalida("Faltan los datos del negocio");
        }

        Usuario propietario = null;

        if (request.propietarioId() != null) {
            propietario = usuarioRepository.findById(request.propietarioId())
                    .orElseThrow(() -> ApiException.noEncontrado("Propietario no encontrado"));

            if (negocioRepository.findFirstByPropietarioId(propietario.getId()).isPresent()) {
                throw ApiException.conflicto("Ese usuario ya tiene un negocio asignado");
            }

            if (!Rol.NEGOCIO.equals(propietario.getRol())) {
                propietario.setRol(Rol.NEGOCIO);
                propietario = usuarioRepository.save(propietario);
            }

        } else if (request.nuevoPropietario() != null) {
            propietario = authService.crearUsuario(request.nuevoPropietario(), Rol.NEGOCIO);
        }

        Negocio negocio = new Negocio();
        copiarDatos(request.negocio(), negocio);
        negocio.setPropietario(propietario);
        negocio.setActivo(request.negocio().getActivo() == null || request.negocio().getActivo());
        negocio.setAbierto(Boolean.TRUE.equals(request.negocio().getAbierto()));

        return negocioRepository.save(negocio);
    }

    @Transactional
    public Negocio actualizarComoAdmin(Long id, Negocio datos, Long propietarioId) {

        Negocio negocio = obtenerPorId(id);
        copiarDatos(datos, negocio);

        if (datos.getActivo() != null) {
            negocio.setActivo(datos.getActivo());
        }

        if (datos.getAbierto() != null) {
            negocio.setAbierto(datos.getAbierto());
        }

        if (propietarioId != null) {
            Usuario propietario = usuarioRepository.findById(propietarioId)
                    .orElseThrow(() -> ApiException.noEncontrado("Propietario no encontrado"));

            negocioRepository.findFirstByPropietarioId(propietarioId)
                    .filter(otro -> !otro.getId().equals(negocio.getId()))
                    .ifPresent(otro -> {
                        throw ApiException.conflicto("Ese usuario ya tiene otro negocio asignado");
                    });

            if (!Rol.NEGOCIO.equals(propietario.getRol())) {
                propietario.setRol(Rol.NEGOCIO);
                usuarioRepository.save(propietario);
            }

            negocio.setPropietario(propietario);
        }

        return negocioRepository.save(negocio);
    }

    public Negocio actualizarMio(Usuario usuario, Negocio datos) {

        Negocio negocio = obtenerDelPropietario(usuario);
        copiarDatos(datos, negocio);

        if (datos.getAbierto() != null) {
            negocio.setAbierto(datos.getAbierto());
        }

        return negocioRepository.save(negocio);
    }

    public Negocio cambiarAbierto(Usuario usuario, boolean abierto) {

        Negocio negocio = obtenerDelPropietario(usuario);

        if (abierto && !Boolean.TRUE.equals(negocio.getActivo())) {
            throw ApiException.prohibido("Tu negocio está deshabilitado por el administrador");
        }

        negocio.setAbierto(abierto);
        return negocioRepository.save(negocio);
    }

    public Negocio cambiarActivo(Long id, boolean activo) {

        Negocio negocio = obtenerPorId(id);
        negocio.setActivo(activo);

        if (!activo) {
            negocio.setAbierto(false);
        }

        return negocioRepository.save(negocio);
    }

    @Transactional
    public void eliminar(Long id) {

        Negocio negocio = obtenerPorId(id);

        if (pedidoRepository.existsByNegocioId(id)) {
            throw ApiException.conflicto(
                    "El negocio tiene pedidos registrados. Deshabilítalo en lugar de eliminarlo");
        }

        if (productoRepository.countByNegocioId(id) > 0) {
            throw ApiException.conflicto(
                    "El negocio tiene productos registrados. Elimínalos primero o deshabilita el negocio");
        }

        negocioRepository.delete(negocio);
    }

    private void copiarDatos(Negocio origen, Negocio destino) {

        String nombre = AuthService.texto(origen.getNombre());

        if (nombre == null && destino.getNombre() == null) {
            throw ApiException.solicitudInvalida("El nombre del negocio es obligatorio");
        }

        if (nombre != null) {
            destino.setNombre(nombre);
        }

        destino.setDescripcion(AuthService.texto(origen.getDescripcion()));
        destino.setTipoComida(AuthService.texto(origen.getTipoComida()));
        destino.setDireccion(AuthService.texto(origen.getDireccion()));
        destino.setTelefono(AuthService.texto(origen.getTelefono()));
        destino.setImagen(AuthService.texto(origen.getImagen()));
        destino.setHorario(AuthService.texto(origen.getHorario()));

        if (origen.getLatitud() != null) {
            if (origen.getLatitud() < -90 || origen.getLatitud() > 90) {
                throw ApiException.solicitudInvalida("La latitud no es válida");
            }
            destino.setLatitud(origen.getLatitud());
        }

        if (origen.getLongitud() != null) {
            if (origen.getLongitud() < -180 || origen.getLongitud() > 180) {
                throw ApiException.solicitudInvalida("La longitud no es válida");
            }
            destino.setLongitud(origen.getLongitud());
        }

        if (origen.getTiempoEstimado() != null) {
            if (origen.getTiempoEstimado() < 1 || origen.getTiempoEstimado() > 240) {
                throw ApiException.solicitudInvalida("El tiempo estimado debe estar entre 1 y 240 minutos");
            }
            destino.setTiempoEstimado(origen.getTiempoEstimado());
        }
    }

    public static double distanciaMetros(double lat1, double lon1, double lat2, double lon2) {

        double radioTierra = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return radioTierra * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static String normalizar(String valor) {
        String limpio = AuthService.texto(valor);
        return limpio == null ? null : limpio.toLowerCase(Locale.ROOT);
    }

    private static boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(filtro);
    }
}
