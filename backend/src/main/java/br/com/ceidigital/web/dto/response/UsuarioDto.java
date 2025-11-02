package br.com.ceidigital.web.dto.response;

import java.time.Instant;
import java.util.List;

/** DTO de leitura para Usuario (não expõe senha). */
public record UsuarioDto(
        Long id,
        String nome,
        String email,
        boolean ativo,
        List<PerfilResumoDto> perfis,
        Instant criadoEm,
        Instant atualizadoEm
) {}