package br.com.ceidigital.web;

import org.hibernate.exception.ConstraintViolationException;
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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Mapeia exceções comuns para respostas HTTP padronizadas (RFC 7807 - ProblemDetail).
 * Centraliza tratamento de erros de validação (400) e conflito de integridade (409).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Trata erros de validação de bean (@Valid), retornando 400 e os campos inválidos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        var locale = LocaleContextHolder.getLocale();
        pd.setTitle(messageSource.getMessage("validation.failed", null, locale));
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
        var locale = LocaleContextHolder.getLocale();
        pd.setTitle(messageSource.getMessage("conflict", null, locale));
        String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();

        // Detecta violação da restrição única de documento (tipo_pessoa + numero_documento)
        boolean isDocUniqueViolation = false;
        Throwable cause = ex.getCause();
        if (cause instanceof ConstraintViolationException cve) {
            String constraint = cve.getConstraintName();
            if (constraint != null && constraint.equalsIgnoreCase("ux_empresa_tipo_doc")) {
                isDocUniqueViolation = true;
            }
        }
        if (msg != null && msg.toLowerCase().contains("ux_empresa_tipo_doc")) {
            isDocUniqueViolation = true;
        }

        if (isDocUniqueViolation) {
            String detail = messageSource.getMessage("documento.duplicado", null, locale);
            pd.setDetail(detail);
            Map<String, String> errors = new HashMap<>();
            errors.put("numeroDocumento", detail);
            pd.setProperty("errors", errors);
        } else if (msg != null && msg.toLowerCase().contains("cnpj")) {
            // Backward compatibility: mensagens antigas que mencionam CNPJ
            pd.setDetail(messageSource.getMessage("cnpj.duplicado", null, locale));
        } else {
            pd.setDetail(messageSource.getMessage("integrity.violation", null, locale));
        }
        return pd;
    }
}
