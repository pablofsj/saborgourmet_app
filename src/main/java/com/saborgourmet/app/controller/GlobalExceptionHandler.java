package com.saborgourmet.app.controller;

import com.saborgourmet.app.service.ReglaNegocioException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** Convierte los errores de negocio no capturados en una página con mensaje claro. */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReglaNegocioException.class)
    public String reglaNegocio(ReglaNegocioException ex, Model model) {
        model.addAttribute("mensaje", ex.getMessage());
        return "error/negocio";
    }
}
