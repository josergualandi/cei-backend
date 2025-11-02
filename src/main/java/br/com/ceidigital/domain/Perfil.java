package br.com.ceidigital.domain;

import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Perfil de acesso (ex.: ADMIN, COMUM), com conjunto de permissões.
 */
@Entity
@Table(name = "perfil")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil")
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String nome; // Ex.: ADMIN, COMUM

    @Column(length = 150)
    private String descricao;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "perfil_permissao",
            joinColumns = @JoinColumn(name = "id_perfil"),
            inverseJoinColumns = @JoinColumn(name = "id_permissao")
    )
    private Set<Permissao> permissoes = new LinkedHashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Set<Permissao> getPermissoes() { return permissoes; }
    public void setPermissoes(Set<Permissao> permissoes) { this.permissoes = permissoes; }
}
