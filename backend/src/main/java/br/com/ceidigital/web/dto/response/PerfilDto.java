package br.com.ceidigital.web.dto.response;

import java.util.List;

/** DTO de leitura para Perfil. */
public record PerfilDto(
        Long id,
        String nome,
        String descricao,
        List<PermissaoDto> permissoes
) {}