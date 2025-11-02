package br.com.ceidigital.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Entidade de domínio que representa uma Empresa.
 * Mapeada para a tabela "empresa" (singular) com as colunas do schema SQL.
 * Mantém campos transitórios de compatibilidade (nome, cnpj) para não quebrar usos existentes.
 */
@Entity
@Table(name = "empresa",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_empresa_tipo_doc", columnNames = {"tipo_pessoa", "numero_documento"})
        },
        indexes = {
                @Index(name = "ix_empresa_nome_razao", columnList = "nome_razao_social"),
                @Index(name = "ix_empresa_nome_fantasia", columnList = "nome_fantasia"),
                @Index(name = "ix_empresa_cidade", columnList = "cidade"),
                @Index(name = "ix_empresa_estado", columnList = "estado")
        }
)
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private Long id;

    @Column(name = "tipo_pessoa", nullable = false, length = 10)
    private String tipoPessoa; // 'CNPJ', 'CPF', 'MEI'

    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento; // documento sem máscara

    @Column(name = "nome_razao_social", nullable = false, length = 150)
    private String nomeRazaoSocial;

    @Column(name = "nome_fantasia", length = 150)
    private String nomeFantasia;

    @Column(name = "tipo_atividade", length = 100)
    private String tipoAtividade;

    @Column(length = 20)
    private String cnae;

    @Column(name = "data_abertura")
    private LocalDate dataAbertura;

    @Column(length = 20)
    private String situacao; // 'Ativa', 'Inativa'

    @Column(length = 255)
    private String endereco;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String estado;

    @Column(length = 20)
    private String telefone;

    @Column(length = 100)
    private String email;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();

    // Compatibilidade: aceita 'cnpj' e 'nome' (transientes) para não quebrar clientes antigos
    @Transient
    private String cnpj;

    @Transient
    private String nome; // alias para nome fantasia/razão social

    @PrePersist
    public void prePersist() {
        if (criadoEm == null) criadoEm = Instant.now();
        if (numeroDocumento != null) numeroDocumento = numeroDocumento.replaceAll("[^0-9]", "");
        if (cnpj != null && (tipoPessoa == null || tipoPessoa.isBlank())) {
            this.tipoPessoa = "CNPJ";
            this.numeroDocumento = cnpj.replaceAll("[^0-9]", "");
        }
        // Preenche nomeRazaoSocial a partir de nome (se informado)
        if ((nomeRazaoSocial == null || nomeRazaoSocial.isBlank()) && nome != null && !nome.isBlank()) {
            nomeRazaoSocial = nome;
        }
    }

    @PreUpdate
    public void preUpdate() { this.atualizadoEm = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoPessoa() { return tipoPessoa; }
    public void setTipoPessoa(String tipoPessoa) { this.tipoPessoa = tipoPessoa; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getNomeRazaoSocial() { return nomeRazaoSocial; }
    public void setNomeRazaoSocial(String nomeRazaoSocial) { this.nomeRazaoSocial = nomeRazaoSocial; }

    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }

    public String getTipoAtividade() { return tipoAtividade; }
    public void setTipoAtividade(String tipoAtividade) { this.tipoAtividade = tipoAtividade; }

    public String getCnae() { return cnae; }
    public void setCnae(String cnae) { this.cnae = cnae; }

    public LocalDate getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDate dataAbertura) { this.dataAbertura = dataAbertura; }

    public String getSituacao() { return situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }

    public Instant getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }

    // Compat: cnpj como alias de numeroDocumento quando tipoPessoa=CNPJ
    public String getCnpj() {
        if ("CNPJ".equalsIgnoreCase(this.tipoPessoa) && this.numeroDocumento != null) {
            return this.numeroDocumento;
        }
        return cnpj;
    }
    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
        if (cnpj != null) {
            this.tipoPessoa = "CNPJ";
            this.numeroDocumento = cnpj.replaceAll("[^0-9]", "");
        }
    }

    // Compat: nome alias
    public String getNome() {
        if (this.nome != null && !this.nome.isBlank()) return this.nome;
        if (this.nomeFantasia != null && !this.nomeFantasia.isBlank()) return this.nomeFantasia;
        return this.nomeRazaoSocial;
    }
    public void setNome(String nome) { this.nome = nome; }
}
