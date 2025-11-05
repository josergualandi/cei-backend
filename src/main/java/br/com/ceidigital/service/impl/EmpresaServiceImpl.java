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
    public EmpresaDto criarBloqueada(EmpresaCreateDto payload) {
        Empresa entity = DtoMapper.fromCreate(payload);
        entity.setId(null);
        entity.setBloqueada(true);
        Empresa saved = repository.save(entity);
        return DtoMapper.toDto(saved);
    }

    @Override
    @Transactional
    public Optional<EmpresaDto> atualizar(long id, EmpresaCreateDto payload) {
        return repository.findById(id).map(existing -> {
            boolean locked = existing.isBloqueada();
            // Atualiza somente campos informados
            if (!locked) {
                if (payload.cnpj() != null && !payload.cnpj().isBlank()) {
                    existing.setCnpj(payload.cnpj());
                }
                if (payload.tipoPessoa() != null && !payload.tipoPessoa().isBlank()) {
                    existing.setTipoPessoa(payload.tipoPessoa());
                }
                if (payload.numeroDocumento() != null && !payload.numeroDocumento().isBlank()) {
                    existing.setNumeroDocumento(payload.numeroDocumento().replaceAll("[^0-9]", ""));
                }
            }
            if (payload.nomeRazaoSocial() != null && !payload.nomeRazaoSocial().isBlank()) {
                existing.setNomeRazaoSocial(payload.nomeRazaoSocial());
            }
            if (payload.nome() != null && !payload.nome().isBlank()) {
                existing.setNome(payload.nome());
            }
            existing.setNomeFantasia(payload.nomeFantasia());
            existing.setTipoAtividade(payload.tipoAtividade());
            existing.setCnae(payload.cnae());
            existing.setDataAbertura(payload.dataAbertura());
            existing.setSituacao(payload.situacao());
            existing.setEndereco(payload.endereco());
            existing.setCidade(payload.cidade());
            existing.setEstado(payload.estado());
            existing.setTelefone(payload.telefone());
            existing.setEmail(payload.email());

            Empresa saved = repository.save(existing);
            return DtoMapper.toDto(saved);
        });
    }

    @Override
    @Transactional
    public boolean excluirPorId(long id) {
        return repository.findById(id).map(e -> {
            if (e.isBloqueada()) {
                return false; // bloqueada: não exclui
            }
            repository.deleteById(id);
            return true;
        }).orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTipoPessoaAndNumeroDocumento(String tipoPessoa, String numeroDocumentoDigits) {
        if (tipoPessoa == null || numeroDocumentoDigits == null) return false;
        String tp = tipoPessoa.trim().toUpperCase();
        String digits = numeroDocumentoDigits.replaceAll("[^0-9]", "");
        if (tp.isBlank() || digits.isBlank()) return false;
        return repository.findByTipoPessoaAndNumeroDocumento(tp, digits).isPresent();
    }

    @Override
    @Transactional
    public boolean excluirPorIdForce(long id) {
        return repository.findById(id).map(e -> {
            repository.deleteById(id);
            return true;
        }).orElse(false);
    }
}
