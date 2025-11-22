package br.com.ceidigital.web.dto;

import lombok.Data;

@Data
public class KitProdutoDto {
    private Long idKitProduto;
    private Long idKit;
    private Long idProduto;
    private Integer quantidade;
    // Getters e Setters
}
