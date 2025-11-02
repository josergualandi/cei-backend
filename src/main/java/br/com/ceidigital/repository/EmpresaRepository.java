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
     * Busca uma empresa pelo CNPJ normalizado (apenas dígitos).
     * @param cnpj CNPJ contendo somente números
     * @return {@link Optional} com a empresa encontrada, se existir
     */
    Optional<Empresa> findByCnpj(String cnpj);
}
