package br.com.ceidigital.web.dto.request;

import java.time.LocalDate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** DTO de criação/entrada para Empresa (request payload). */
public record EmpresaCreateDto(
        @NotBlank(message = "tipoPessoa.obrigatorio") String tipoPessoa,
        @NotBlank(message = "numeroDocumento.obrigatorio") String numeroDocumento,
        @NotBlank(message = "nomeRazaoSocial.obrigatorio") String nomeRazaoSocial,
        String nome,
        String cnpj,
        String nomeFantasia,
        String tipoAtividade,
        String cnae,
        LocalDate dataAbertura,
        String situacao,
        String endereco,
        String cidade,
        String estado,
        String telefone,
        @Email(message = "email.invalido") String email
) {}
