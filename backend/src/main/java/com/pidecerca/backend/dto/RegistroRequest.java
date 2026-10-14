package com.pidecerca.backend.dto;

public record RegistroRequest(
        String nombre,
        String apellido,
        String correo,
        String password,
        String telefono) {
}
