package com.pidecerca.backend.entity;

public final class Rol {

    public static final String CLIENTE = "CLIENTE";
    public static final String NEGOCIO = "NEGOCIO";
    public static final String ADMIN = "ADMIN";

    private Rol() {
    }

    public static boolean esValido(String rol) {
        return CLIENTE.equals(rol) || NEGOCIO.equals(rol) || ADMIN.equals(rol);
    }
}
