package br.com.ceidigital.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProduto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @Column(length = 150, nullable = false)
    private String nomeProduto;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal precoUnitario;

    @Column(name = "preco_compra", precision = 10, scale = 2)
    private BigDecimal precoCompra;

    @Column(nullable = false)
    private Boolean consignado;

    @Column(nullable = false)
    private Integer quantidadeEstoque;

    @Column(nullable = false)
    private Boolean ativo;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @Column
    private LocalDateTime atualizadoEm;

    public Long getIdProduto() {
        return idProduto;
    }
    public void setIdProduto(Long idProduto) {
        this.idProduto = idProduto;
    }
    public Empresa getEmpresa() {
        return empresa;
    }
    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
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

    public BigDecimal getPrecoCompra() {
        return precoCompra;
    }

    public void setPrecoCompra(BigDecimal precoCompra) {
        this.precoCompra = precoCompra;
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
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
