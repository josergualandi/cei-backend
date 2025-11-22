package br.com.ceidigital.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class KitProduto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idKitProduto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kit", nullable = false)
    private KitPromocional kit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private Integer quantidade;

    // Getters e Setters
}
