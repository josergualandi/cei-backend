package br.com.ceidigital.web.dto.response;

/** DTO de leitura para Permissao. */
public record PermissaoDto(
        Long id,
        String nome,
        String descricao,
        String rota
) {}