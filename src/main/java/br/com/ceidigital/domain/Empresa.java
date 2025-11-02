package br.com.ceidigital.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.AssertTrue;
import java.time.Instant;

/**
 * Entidade de domínio que representa uma Empresa.
 * Mapeada para a tabela "empresas" com índice único para o campo CNPJ.
 */
@Entity
@Table(name = "empresas", indexes = {
        @Index(name = "idx_empresas_cnpj", columnList = "cnpj", unique = true)
})
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Identificador único da empresa (chave primária)
    private Long id;

    @NotBlank(message = "{empresa.nome.notblank}")
    @Column(nullable = false)
    // Nome fantasia ou razão social da empresa
    private String nome;

    @NotBlank(message = "{empresa.cnpj.notblank}")
    @Column(nullable = false, unique = true, length = 18)
    // CNPJ da empresa. Persistimos apenas dígitos (14), mas permitimos entrada com máscara.
    private String cnpj;

    @Column(nullable = false, updatable = false)
    // Data/hora de criação do registro (UTC)
    private Instant createdAt;

    // Executado automaticamente antes do primeiro persist() do JPA
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (cnpj != null) {
            // Normaliza o CNPJ removendo qualquer caractere que não seja dígito
            cnpj = cnpj.replaceAll("[^0-9]", "");
        }
    }

    public Empresa() {}

    public Empresa(String nome, String cnpj) {
        this.nome = nome;
        this.cnpj = cnpj;
    }

    /**
     * Valida o CNPJ após normalização: deve conter exatamente 14 dígitos.
     * Observação: esta validação é apenas de tamanho, não verifica dígitos verificadores.
     */
    @AssertTrue(message = "{empresa.cnpj.invalido}")
    private boolean isCnpjValido() {
        if (cnpj == null) return false;
        String digits = cnpj.replaceAll("\\D", "");
        return digits.length() == 14;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
