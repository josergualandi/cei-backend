package br.com.ceidigital.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Entity
@Data
public class KitPromocional {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idKit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @Column(length = 150, nullable = false)
    private String nomeKit;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal precoTotal;

    @Column(nullable = false)
    private Boolean ativo;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @OneToMany(mappedBy = "kit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KitProduto> produtos;

    // Getters e Setters
}
