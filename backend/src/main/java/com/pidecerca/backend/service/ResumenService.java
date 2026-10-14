package com.pidecerca.backend.service;

import com.pidecerca.backend.entity.EstadoPedido;
import com.pidecerca.backend.entity.Rol;
import com.pidecerca.backend.repository.NegocioRepository;
import com.pidecerca.backend.repository.PedidoRepository;
import com.pidecerca.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ResumenService {

    private final UsuarioRepository usuarioRepository;
    private final NegocioRepository negocioRepository;
    private final PedidoRepository pedidoRepository;

    public ResumenService(
            UsuarioRepository usuarioRepository,
            NegocioRepository negocioRepository,
            PedidoRepository pedidoRepository) {

        this.usuarioRepository = usuarioRepository;
        this.negocioRepository = negocioRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public Map<String, Object> obtener() {

        LocalDateTime inicioDelDia = LocalDate.now().atStartOfDay();

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("totalUsuarios", usuarioRepository.count());
        resumen.put("totalClientes", usuarioRepository.countByRol(Rol.CLIENTE));
        resumen.put("totalDuenos", usuarioRepository.countByRol(Rol.NEGOCIO));
        resumen.put("totalAdmins", usuarioRepository.countByRol(Rol.ADMIN));
        resumen.put("totalNegocios", negocioRepository.count());
        resumen.put("negociosActivos", negocioRepository.countByActivoTrue());
        resumen.put("totalPedidos", pedidoRepository.count());
        resumen.put("pedidosHoy", pedidoRepository.countByFechaAfter(inicioDelDia));
        resumen.put("ventasHoy", pedidoRepository.sumarVentasDesde(inicioDelDia));
        resumen.put("ventasTotales", pedidoRepository.sumarVentasTotales());

        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoPedido estado : EstadoPedido.values()) {
            porEstado.put(estado.name(), pedidoRepository.countByEstado(estado));
        }
        resumen.put("pedidosPorEstado", porEstado);

        return resumen;
    }
}
