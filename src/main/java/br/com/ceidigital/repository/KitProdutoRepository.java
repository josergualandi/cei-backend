package br.com.ceidigital.repository;

import br.com.ceidigital.domain.KitProduto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KitProdutoRepository extends JpaRepository<KitProduto, Long> {
}
