package com.ecocircular.ecocircular.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setType(URI.create("urn:ecocircular:validation-error"));
        pd.setTitle("Error de validación");
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        pd.setDetail(detail);
        return pd;
    }

    /** Violaciones de invariantes de dominio */
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setType(URI.create("urn:ecocircular:domain-rule-violation"));
        pd.setTitle("Regla de negocio violada");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    /** Entidad no encontrada o argumento inválido */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setType(URI.create("urn:ecocircular:not-found"));
        pd.setTitle("Recurso no encontrado");
        pd.setDetail(ex.getMessage());
        return pd;
    }

    /** Errores no esperados */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setType(URI.create("urn:ecocircular:internal-error"));
        pd.setTitle("Error interno");
        pd.setDetail("Ocurrió un error inesperado. Contacte al administrador.");
        return pd;
    }
}