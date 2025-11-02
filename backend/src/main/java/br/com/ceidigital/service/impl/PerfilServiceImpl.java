package br.com.ceidigital.service.impl;

import br.com.ceidigital.repository.PerfilRepository;
import br.com.ceidigital.service.PerfilService;
import br.com.ceidigital.web.dto.DtoMapper;
import br.com.ceidigital.web.dto.response.PerfilDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Implementação do serviço de Perfil. */
@Service
public class PerfilServiceImpl implements PerfilService {

    private final PerfilRepository repository;

    public PerfilServiceImpl(PerfilRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerfilDto> listar() {
        return repository.findAll().stream().map(DtoMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PerfilDto> buscarPorId(long id) {
        return repository.findById(id).map(DtoMapper::toDto);
    }
}
