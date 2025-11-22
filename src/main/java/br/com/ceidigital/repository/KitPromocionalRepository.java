package br.com.ceidigital.repository;

import br.com.ceidigital.domain.KitPromocional;
import br.com.ceidigital.domain.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KitPromocionalRepository extends JpaRepository<KitPromocional, Long> {
    List<KitPromocional> findByEmpresa(Empresa empresa);
}
