package br.com.ceidigital.domain;

import jakarta.persistence.*;

/**
 * Permissão granular (ex.: CADASTRAR_EMPRESA).
 */
@Entity
@Table(name = "permissao")
public class Permissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permissao")
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String nome;

    @Column(length = 200)
    private String descricao;

    @Column(length = 100)
    private String rota;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getRota() { return rota; }
    public void setRota(String rota) { this.rota = rota; }
}
