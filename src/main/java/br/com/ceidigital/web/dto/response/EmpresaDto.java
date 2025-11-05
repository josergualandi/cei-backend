package br.com.ceidigital.web.dto.response;

import java.time.Instant;
import java.time.LocalDate;

/** DTO de leitura para Empresa. */
public record EmpresaDto(
        Long id,
        String tipoPessoa,
        String numeroDocumento,
        String nomeRazaoSocial,
        String nomeFantasia,
        String tipoAtividade,
        String cnae,
        LocalDate dataAbertura,
        String situacao,
        String endereco,
        String cidade,
        String estado,
        String telefone,
        String email,
        Instant criadoEm,
        Instant atualizadoEm,
        boolean bloqueada
) {}