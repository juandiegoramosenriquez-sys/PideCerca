package com.pidecerca.backend.dto;

public class UsuarioResponse {

    private Long id;
    private String nombre;
    private String apellido;
    private String correo;
    private String telefono;
    private String rol;
    private Boolean activo;

    public UsuarioResponse() {
    }

    public UsuarioResponse(
            Long id,
            String nombre,
            String apellido,
            String correo,
            String telefono,
            String rol,
            Boolean activo) {

        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.telefono = telefono;
        this.rol = rol;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getRol() {
        return rol;
    }

    public Boolean getActivo() {
        return activo;
    }
}