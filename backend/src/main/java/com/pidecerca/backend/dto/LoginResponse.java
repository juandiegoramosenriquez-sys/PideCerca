package com.pidecerca.backend.dto;

public class LoginResponse {

    private String token;
    private UsuarioResponse usuario;

    public LoginResponse() {
    }

    public LoginResponse(String token, UsuarioResponse usuario) {
        this.token = token;
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public UsuarioResponse getUsuario() {
        return usuario;
    }
}
