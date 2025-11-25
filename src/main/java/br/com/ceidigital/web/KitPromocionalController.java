package br.com.ceidigital.web;
import br.com.ceidigital.domain.KitPromocional;
import br.com.ceidigital.domain.Usuario;
import br.com.ceidigital.repository.KitPromocionalRepository;
import br.com.ceidigital.repository.KitProdutoRepository;
import br.com.ceidigital.repository.ProdutoRepository;
import br.com.ceidigital.repository.UsuarioRepository;
import br.com.ceidigital.web.dto.KitPromocionalDto;
import br.com.ceidigital.web.dto.KitProdutoDto;
import br.com.ceidigital.domain.KitProduto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/kits")
public class KitPromocionalController {
    private final KitPromocionalRepository kitRepository;
    private final KitProdutoRepository kitProdutoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;

    public KitPromocionalController(KitPromocionalRepository kitRepository, KitProdutoRepository kitProdutoRepository, ProdutoRepository produtoRepository, UsuarioRepository usuarioRepository) {
        this.kitRepository = kitRepository;
        this.kitProdutoRepository = kitProdutoRepository;
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    public ResponseEntity<KitPromocionalDto> create(@Valid @RequestBody KitPromocionalDto payload) {
        var current = getCurrentUser();
        if (current.isEmpty() || current.get().getEmpresa() == null) {
            return ResponseEntity.status(403).build();
        }

        KitPromocional kit = new KitPromocional();
        kit.setEmpresa(current.get().getEmpresa());
        kit.setNomeKit(payload.getNomeKit());
        kit.setDescricao(payload.getDescricao());
        kit.setAtivo(payload.getAtivo());
        kit.setCriadoEm(java.time.LocalDateTime.now());
        kit.setPrecoTotal(java.math.BigDecimal.ZERO);

        // Calcular preo total e montar lista de produtos do kit
        BigDecimal precoTotal = java.math.BigDecimal.ZERO;
        List<KitProdutoDto> produtosDto = payload.getProdutos();
        List<KitProduto> produtosKit = new java.util.ArrayList<>();
        if (produtosDto != null) {
            for (KitProdutoDto prodDto : produtosDto) {
                if (prodDto.getIdProduto() == null) {
                    System.out.println("Produto do kit ignorado: idProduto nulo");
                    continue;
                }
                var produtoOpt = produtoRepository.findById(prodDto.getIdProduto());
                if (produtoOpt.isPresent()) {
                    var produto = produtoOpt.get();
                    var kitProduto = new br.com.ceidigital.domain.KitProduto();
                    kitProduto.setKit(kit);
                    kitProduto.setProduto(produto);
                    kitProduto.setQuantidade(prodDto.getQuantidade());
                    produtosKit.add(kitProduto);
                    precoTotal = precoTotal.add(
                        produto.getPrecoUnitario().multiply(java.math.BigDecimal.valueOf(prodDto.getQuantidade()))
                    );
                }
            }
            kit.setProdutos(produtosKit);
        }
        kit.setPrecoTotal(precoTotal);
        KitPromocional savedKit = kitRepository.saveAndFlush(kit);
        // Salva explicitamente os produtos do kit
        if (produtosKit.size() > 0) {
            for (KitProduto kp : produtosKit) {
                kp.setKit(savedKit); // garante id correto
            }
            kitProdutoRepository.saveAll(produtosKit);
        }

        var location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(savedKit.getIdKit())
            .toUri();

        KitPromocionalDto dto = new KitPromocionalDto();
        dto.setIdKit(savedKit.getIdKit());
        dto.setNomeKit(savedKit.getNomeKit());
        dto.setDescricao(savedKit.getDescricao());
        dto.setAtivo(savedKit.getAtivo());
        dto.setPrecoTotal(savedKit.getPrecoTotal());
        dto.setProdutos(produtosDto); // Retorna os produtos recebidos
        return ResponseEntity.created(location).body(dto);
    }

    @GetMapping
    public ResponseEntity<java.util.List<KitPromocionalDto>> list() {
        var current = getCurrentUser();
        if (current.isEmpty() || current.get().getEmpresa() == null) {
            return ResponseEntity.status(403).build();
        }
        var kits = kitRepository.findByEmpresa(current.get().getEmpresa());
        var dtos = kits.stream().map(kit -> {
            KitPromocionalDto dto = new KitPromocionalDto();
            dto.setIdKit(kit.getIdKit());
            dto.setNomeKit(kit.getNomeKit());
            dto.setDescricao(kit.getDescricao());
            dto.setAtivo(kit.getAtivo());
            dto.setPrecoTotal(kit.getPrecoTotal());
            // Monta lista de produtos do kit
           List<KitProdutoDto> produtosDto = kit.getProdutos().stream().map(kp -> {
                KitProdutoDto kpd = new KitProdutoDto();
                kpd.setIdProduto(kp.getProduto().getIdProduto());
                kpd.setQuantidade(kp.getQuantidade());
                return kpd;
            }).toList();
            dto.setProdutos(produtosDto);
            return dto;
        }).toList();
        return ResponseEntity.ok(dtos);
    }

    private Optional<Usuario> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? Objects.toString(auth.getName(), null) : null;
        if (email == null) return Optional.empty();
        return usuarioRepository.findByEmail(email.trim().toLowerCase());
    }
}
