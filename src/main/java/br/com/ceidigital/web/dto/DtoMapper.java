package br.com.ceidigital.web.dto;

import br.com.ceidigital.domain.Empresa;
import br.com.ceidigital.domain.Perfil;
import br.com.ceidigital.domain.Permissao;
import br.com.ceidigital.domain.Usuario;
import br.com.ceidigital.web.dto.request.EmpresaCreateDto;
import br.com.ceidigital.web.dto.response.EmpresaDto;
import br.com.ceidigital.web.dto.response.PerfilDto;
import br.com.ceidigital.web.dto.response.PerfilResumoDto;
import br.com.ceidigital.web.dto.response.PermissaoDto;
import br.com.ceidigital.web.dto.response.UsuarioDto;

import java.util.List;

/** Mapeadores simples entre entidades e DTOs. */
public final class DtoMapper {
    // Produto
    public static ProdutoDto toDto(br.com.ceidigital.domain.Produto p) {
        if (p == null) return null;
        ProdutoDto dto = new ProdutoDto();
        dto.setIdProduto(p.getIdProduto());
        dto.setIdEmpresa(p.getEmpresa() != null ? p.getEmpresa().getId() : null);
        dto.setNomeProduto(p.getNomeProduto());
        dto.setDescricao(p.getDescricao());
        dto.setPrecoUnitario(p.getPrecoUnitario());
        dto.setConsignado(p.getConsignado());
        dto.setQuantidadeEstoque(p.getQuantidadeEstoque());
        dto.setAtivo(p.getAtivo());
        dto.setCriadoEm(p.getCriadoEm() != null ? p.getCriadoEm().toString() : null);
        dto.setAtualizadoEm(p.getAtualizadoEm() != null ? p.getAtualizadoEm().toString() : null);
        return dto;
    }
    private DtoMapper() {}

    // Empresa
    public static EmpresaDto toDto(Empresa e) {
        if (e == null) return null;
        return new EmpresaDto(
                e.getId(),
                e.getTipoPessoa(),
                e.getNumeroDocumento(),
                e.getNomeRazaoSocial(),
                e.getNomeFantasia(),
                e.getTipoAtividade(),
                e.getCnae(),
                e.getDataAbertura(),
                e.getSituacao(),
                e.getEndereco(),
                e.getCidade(),
                e.getEstado(),
                e.getTelefone(),
                e.getEmail(),
                e.getCriadoEm(),
                e.getAtualizadoEm(),
                    e.isBloqueada()
        );
    }

    public static Empresa fromCreate(EmpresaCreateDto dto) {
        if (dto == null) return null;
        Empresa e = new Empresa();
        if (dto.cnpj() != null && !dto.cnpj().isBlank()) {
            e.setCnpj(dto.cnpj());
        }
        if (dto.tipoPessoa() != null && !dto.tipoPessoa().isBlank()) e.setTipoPessoa(dto.tipoPessoa());
        if (dto.numeroDocumento() != null && !dto.numeroDocumento().isBlank()) e.setNumeroDocumento(dto.numeroDocumento());
        if (dto.nomeRazaoSocial() != null && !dto.nomeRazaoSocial().isBlank()) e.setNomeRazaoSocial(dto.nomeRazaoSocial());
        if (dto.nome() != null && !dto.nome().isBlank()) e.setNome(dto.nome());
        e.setNomeFantasia(dto.nomeFantasia());
        e.setTipoAtividade(dto.tipoAtividade());
        e.setCnae(dto.cnae());
        e.setDataAbertura(dto.dataAbertura());
        e.setSituacao(dto.situacao());
        e.setEndereco(dto.endereco());
        e.setCidade(dto.cidade());
        e.setEstado(dto.estado());
        e.setTelefone(dto.telefone());
        e.setEmail(dto.email());
        return e;
    }

    // Permissao
    public static PermissaoDto toDto(Permissao p) {
        if (p == null) return null;
        return new PermissaoDto(p.getId(), p.getNome(), p.getDescricao(), p.getRota());
    }

    // Perfil
    public static PerfilDto toDto(Perfil p) {
        if (p == null) return null;
        List<PermissaoDto> perms = p.getPermissoes().stream().map(DtoMapper::toDto).toList();
        return new PerfilDto(p.getId(), p.getNome(), p.getDescricao(), perms);
    }

    // Usuario (sem senha)
    public static UsuarioDto toDto(Usuario u) {
        if (u == null) return null;
        var perfis = u.getPerfis().stream().map(pe -> new PerfilResumoDto(pe.getId(), pe.getNome())).toList();
        return new UsuarioDto(u.getId(), u.getNome(), u.getEmail(), u.isAtivo(), perfis, u.getCriadoEm(), u.getAtualizadoEm());
    }
}
