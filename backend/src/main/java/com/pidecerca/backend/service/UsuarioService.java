package com.pidecerca.backend.service;

import com.pidecerca.backend.dto.CambiarPasswordRequest;
import com.pidecerca.backend.dto.PerfilRequest;
import com.pidecerca.backend.dto.RegistroRequest;
import com.pidecerca.backend.dto.UsuarioRequest;
import com.pidecerca.backend.entity.Rol;
import com.pidecerca.backend.entity.Usuario;
import com.pidecerca.backend.exception.ApiException;
import com.pidecerca.backend.repository.NegocioRepository;
import com.pidecerca.backend.repository.NotificacionRepository;
import com.pidecerca.backend.repository.PedidoRepository;
import com.pidecerca.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final PedidoRepository pedidoRepository;
    private final NegocioRepository negocioRepository;
    private final NotificacionRepository notificacionRepository;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuthService authService,
            PedidoRepository pedidoRepository,
            NegocioRepository negocioRepository,
            NotificacionRepository notificacionRepository) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.pedidoRepository = pedidoRepository;
        this.negocioRepository = negocioRepository;
        this.notificacionRepository = notificacionRepository;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAllByOrderByIdDesc();
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado"));
    }

    public Usuario crear(UsuarioRequest request) {

        String rol = request.rol() == null ? Rol.CLIENTE : request.rol().toUpperCase();

        if (!Rol.esValido(rol)) {
            throw ApiException.solicitudInvalida("Rol no válido");
        }

        Usuario usuario = authService.crearUsuario(
                new RegistroRequest(
                        request.nombre(),
                        request.apellido(),
                        request.correo(),
                        request.password(),
                        request.telefono()
                ),
                rol
        );

        if (Boolean.FALSE.equals(request.activo())) {
            usuario.setActivo(false);
            usuario = usuarioRepository.save(usuario);
        }

        return usuario;
    }

    public Usuario actualizar(Long id, UsuarioRequest request, Usuario actual) {

        Usuario usuario = obtenerPorId(id);

        if (AuthService.texto(request.nombre()) != null) {
            usuario.setNombre(AuthService.texto(request.nombre()));
        }

        usuario.setApellido(AuthService.texto(request.apellido()));
        usuario.setTelefono(AuthService.texto(request.telefono()));

        String correo = AuthService.normalizarCorreo(request.correo());

        if (correo != null && !correo.equalsIgnoreCase(usuario.getCorreo())) {
            if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
                throw ApiException.conflicto("El correo ya está registrado");
            }
            usuario.setCorreo(correo);
        }

        if (request.rol() != null) {
            String rol = request.rol().toUpperCase();
            if (!Rol.esValido(rol)) {
                throw ApiException.solicitudInvalida("Rol no válido");
            }
            if (usuario.getId().equals(actual.getId()) && !Rol.ADMIN.equals(rol)) {
                throw ApiException.solicitudInvalida("No puedes quitarte el rol de administrador");
            }
            usuario.setRol(rol);
        }

        if (request.activo() != null) {
            if (usuario.getId().equals(actual.getId()) && !request.activo()) {
                throw ApiException.solicitudInvalida("No puedes desactivar tu propia cuenta");
            }
            usuario.setActivo(request.activo());
        }

        if (request.password() != null && !request.password().isBlank()) {
            if (request.password().length() < 6) {
                throw ApiException.solicitudInvalida("La contraseña debe tener al menos 6 caracteres");
            }
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }

        return usuarioRepository.save(usuario);
    }

    public Usuario cambiarActivo(Long id, boolean activo, Usuario actual) {

        Usuario usuario = obtenerPorId(id);

        if (usuario.getId().equals(actual.getId()) && !activo) {
            throw ApiException.solicitudInvalida("No puedes desactivar tu propia cuenta");
        }

        usuario.setActivo(activo);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminar(Long id, Usuario actual) {

        Usuario usuario = obtenerPorId(id);

        if (usuario.getId().equals(actual.getId())) {
            throw ApiException.solicitudInvalida("No puedes eliminar tu propia cuenta");
        }

        if (pedidoRepository.existsByUsuarioId(id)
                || negocioRepository.findFirstByPropietarioId(id).isPresent()) {
            throw ApiException.conflicto(
                    "El usuario tiene pedidos o un negocio. Desactívalo en lugar de eliminarlo");
        }

        notificacionRepository.deleteAll(notificacionRepository.findByUsuarioIdOrderByFechaDesc(id));
        usuarioRepository.delete(usuario);
    }

    public Usuario actualizarPerfil(Usuario actual, PerfilRequest request) {

        if (AuthService.texto(request.nombre()) != null) {
            actual.setNombre(AuthService.texto(request.nombre()));
        }

        actual.setApellido(AuthService.texto(request.apellido()));
        actual.setTelefono(AuthService.texto(request.telefono()));

        return usuarioRepository.save(actual);
    }

    public void cambiarPassword(Usuario usuario, CambiarPasswordRequest request) {

        if (request.getPasswordActual() == null
                || !passwordEncoder.matches(request.getPasswordActual(), usuario.getPassword())) {
            throw ApiException.solicitudInvalida("La contraseña actual es incorrecta");
        }

        if (request.getNuevaPassword() == null || request.getNuevaPassword().length() < 6) {
            throw ApiException.solicitudInvalida("La nueva contraseña debe tener al menos 6 caracteres");
        }

        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
        usuarioRepository.save(usuario);
    }
}
