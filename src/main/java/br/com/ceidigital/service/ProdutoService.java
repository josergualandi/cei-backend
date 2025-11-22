package br.com.ceidigital.service;

import br.com.ceidigital.domain.Produto;
import java.util.List;
import java.util.Optional;

public interface ProdutoService {
    Produto salvar(Produto produto);
    Optional<Produto> buscarPorId(Long id);
    List<Produto> listarPorEmpresa(Long idEmpresa);
    List<Produto> listarTodos();
    void remover(Long id);
}
