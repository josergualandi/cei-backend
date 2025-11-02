package br.com.ceidigital.service.impl;

import br.com.ceidigital.repository.UsuarioRepository;
import br.com.ceidigital.service.UsuarioService;
import br.com.ceidigital.web.dto.DtoMapper;
import br.com.ceidigital.web.dto.response.UsuarioDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Implementação do serviço de Usuário. */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioServiceImpl(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDto> listar() {
        return repository.findAll().stream().map(DtoMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioDto> buscarPorId(long id) {
        return repository.findById(id).map(DtoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioDto> buscarPorEmail(String email) {
        return repository.findByEmail(email).map(DtoMapper::toDto);
    }
}
