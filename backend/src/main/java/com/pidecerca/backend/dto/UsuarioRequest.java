package com.pidecerca.backend.dto;

public record UsuarioRequest(
        String nombre,
        String apellido,
        String correo,
        String password,
        String telefono,
        String rol,
        Boolean activo) {
}
