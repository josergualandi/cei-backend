package br.com.ceidigital.service.impl;

import br.com.ceidigital.repository.PermissaoRepository;
import br.com.ceidigital.service.PermissaoService;
import br.com.ceidigital.web.dto.DtoMapper;
import br.com.ceidigital.web.dto.response.PermissaoDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Implementação do serviço de Permissão. */
@Service
public class PermissaoServiceImpl implements PermissaoService {

    private final PermissaoRepository repository;

    public PermissaoServiceImpl(PermissaoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissaoDto> listar() {
        return repository.findAll().stream().map(DtoMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PermissaoDto> buscarPorId(long id) {
        return repository.findById(id).map(DtoMapper::toDto);
    }
}
