package br.com.ceidigital.service.impl;

import br.com.ceidigital.domain.Produto;
import br.com.ceidigital.repository.ProdutoRepository;
import br.com.ceidigital.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoServiceImpl implements ProdutoService {
    @Autowired
    private ProdutoRepository produtoRepository;

    @Override
    public Produto salvar(Produto produto) {
        return produtoRepository.save(produto);
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findById(id);
    }

    @Override
    public List<Produto> listarPorEmpresa(Long idEmpresa) {
        return produtoRepository.findAll().stream()
                .filter(p -> p.getEmpresa() != null && p.getEmpresa().getId().equals(idEmpresa))
                .toList();
    }

    @Override
    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    @Override
    public void remover(Long id) {
        produtoRepository.deleteById(id);
    }
}
