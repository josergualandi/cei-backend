package br.com.ceidigital.web.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class KitPromocionalDto {
    private Long idKit;
    private Long idEmpresa;
    private String nomeKit;
    private String descricao;
    private BigDecimal precoTotal;
    private Boolean ativo;
    private String criadoEm;
    private List<KitProdutoDto> produtos;
    // Getters e Setters
}
