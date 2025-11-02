package br.com.ceidigital.service.impl;

import br.com.ceidigital.domain.Empresa;
import br.com.ceidigital.repository.EmpresaRepository;
import br.com.ceidigital.service.EmpresaService;
import br.com.ceidigital.web.dto.DtoMapper;
import br.com.ceidigital.web.dto.request.EmpresaCreateDto;
import br.com.ceidigital.web.dto.response.EmpresaDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Implementação padrão da camada de serviço de Empresa. */
@Service
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository repository;

    public EmpresaServiceImpl(EmpresaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpresaDto> listar() {
        return repository.findAll().stream().map(DtoMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmpresaDto> buscarPorId(long id) {
        return repository.findById(id).map(DtoMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmpresaDto> buscarPorCnpj(String cnpj) {
        String digits = cnpj != null ? cnpj.replaceAll("[^0-9]", "") : "";
        if (digits.isEmpty()) return Optional.empty();
        return repository.findByTipoPessoaAndNumeroDocumento("CNPJ", digits).map(DtoMapper::toDto);
    }

    @Override
    @Transactional
    public EmpresaDto criar(EmpresaCreateDto payload) {
        // Conversão + normalizações encapsuladas no mapper
        Empresa entity = DtoMapper.fromCreate(payload);
        entity.setId(null);
        Empresa saved = repository.save(entity);
        return DtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public boolean excluirPorId(long id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }
}
