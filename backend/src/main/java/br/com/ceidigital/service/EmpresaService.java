package br.com.ceidigital.service;

import br.com.ceidigital.web.dto.request.EmpresaCreateDto;
import br.com.ceidigital.web.dto.response.EmpresaDto;

import java.util.List;
import java.util.Optional;

/**
 * Camada de serviço para regras de negócio de Empresa.
 */
public interface EmpresaService {

    /** Lista todas as empresas como DTO. */
    List<EmpresaDto> listar();

    /** Busca por id, retornando DTO quando encontrado. */
    Optional<EmpresaDto> buscarPorId(long id);

    /** Busca por CNPJ (com ou sem máscara). */
    Optional<EmpresaDto> buscarPorCnpj(String cnpj);

    /** Cria uma nova empresa a partir do payload de criação. */
    EmpresaDto criar(EmpresaCreateDto payload);

    /** Cria uma nova empresa bloqueada (não permite excluir nem alterar tipo/doc). */
    EmpresaDto criarBloqueada(EmpresaCreateDto payload);

    /** Atualiza uma empresa existente. Retorna vazio se não encontrada. */
    java.util.Optional<EmpresaDto> atualizar(long id, EmpresaCreateDto payload);

        /** Verifica existência por tipoPessoa (CPF/CNPJ) e número de documento (apenas dígitos). */
        boolean existsByTipoPessoaAndNumeroDocumento(String tipoPessoa, String numeroDocumentoDigits);

    /** Exclui por id; retorna true se existia e foi removida. */
    boolean excluirPorId(long id);

    /** Exclui por id forçando a remoção (ignora bloqueios); uso restrito a ADMIN_MAIN. */
    boolean excluirPorIdForce(long id);
}
