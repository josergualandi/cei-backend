package br.com.ceidigital.web;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapeia exceções comuns para respostas HTTP padronizadas (RFC 7807 - ProblemDetail).
 * Centraliza tratamento de erros de validação (400) e conflito de integridade (409).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata erros de validação de bean (@Valid), retornando 400 e os campos inválidos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation failed");
        // Monta um mapa campo -> mensagem de erro
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (a, b) -> a, HashMap::new));
        pd.setProperty("errors", errors);
        return pd;
    }

    /**
     * Trata violações de integridade do banco (ex.: CNPJ duplicado), retornando 409.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleConflict(DataIntegrityViolationException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Conflict");
        String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        // Mensagem amigável quando a restrição única de CNPJ é violada
        if (msg != null && msg.toLowerCase().contains("cnpj")) {
            pd.setDetail("CNPJ já existente");
        } else {
            pd.setDetail("Violação de integridade");
        }
        return pd;
    }
}
