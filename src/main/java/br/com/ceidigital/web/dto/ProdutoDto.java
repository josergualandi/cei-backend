package br.com.ceidigital.web.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProdutoDto {
    private Long idProduto;
    private Long idEmpresa;
    private String nomeProduto;
    private String descricao;
    private BigDecimal precoUnitario;
    private BigDecimal precoCompra;
    private Boolean consignado;
    private Integer quantidadeEstoque;
    private Boolean ativo;
    private String caminhoImagem;
    private String criadoEm;
    private String atualizadoEm;
        public String getCaminhoImagem() {
            return caminhoImagem;
        }

        public void setCaminhoImagem(String caminhoImagem) {
            this.caminhoImagem = caminhoImagem;
        }
    public Long getIdProduto() {
        return idProduto;
    }
    public void setIdProduto(Long idProduto) {
        this.idProduto = idProduto;
    }
    public Long getIdEmpresa() {
        return idEmpresa;
    }
    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }
    public String getNomeProduto() {
        return nomeProduto;
    }
    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }
    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }
    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public BigDecimal getPrecoCompra() {
        return precoCompra;
    }

    public void setPrecoCompra(BigDecimal precoCompra) {
        this.precoCompra = precoCompra;
    }
    public Boolean getConsignado() {
        return consignado;
    }
    public void setConsignado(Boolean consignado) {
        this.consignado = consignado;
    }
    public Integer getQuantidadeEstoque() {
        return quantidadeEstoque;
    }
    public void setQuantidadeEstoque(Integer quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }
    public Boolean getAtivo() {
        return ativo;
    }
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
    public String getCriadoEm() {
        return criadoEm;
    }
    public void setCriadoEm(String criadoEm) {
        this.criadoEm = criadoEm;
    }
    public String getAtualizadoEm() {
        return atualizadoEm;
    }
    public void setAtualizadoEm(String atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
