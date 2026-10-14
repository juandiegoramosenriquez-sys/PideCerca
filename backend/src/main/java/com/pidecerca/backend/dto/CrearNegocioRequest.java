package com.pidecerca.backend.dto;

import com.pidecerca.backend.entity.Negocio;

public record CrearNegocioRequest(
        Negocio negocio,
        Long propietarioId,
        RegistroRequest nuevoPropietario) {
}
