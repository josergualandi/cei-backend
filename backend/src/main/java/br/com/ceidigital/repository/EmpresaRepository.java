package br.com.ceidigital.repository;

import br.com.ceidigital.domain.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositório JPA para a entidade {@link Empresa}.
 * Expõe operações de CRUD e consultas derivadas pelo Spring Data.
 */
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    /**
     * Busca por documento (normalizado) e tipo (ex.: CNPJ).
     */
    Optional<Empresa> findByTipoPessoaAndNumeroDocumento(String tipoPessoa, String numeroDocumento);
}
