package com.saborgourmet.app.service;

/** Error de negocio con un mensaje apto para mostrar al usuario. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String message) {
        super(message);
    }
}
