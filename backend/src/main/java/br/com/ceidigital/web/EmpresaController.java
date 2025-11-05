package br.com.ceidigital.web;

import br.com.ceidigital.service.EmpresaService;
import br.com.ceidigital.web.dto.request.EmpresaCreateDto;
import br.com.ceidigital.web.dto.response.EmpresaDto;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para recursos de Empresa.
 * Disponibiliza endpoints CRUD em /api/empresas.
 */
@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService service;

    public EmpresaController(EmpresaService service) {
        this.service = service;
    }

    /**
     * Lista todas as empresas.
     */
    @GetMapping
    public List<EmpresaDto> list() {
        return service.listar();
    }

    /**
     * Busca uma empresa por id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmpresaDto> get(@PathVariable long id) {
        return service.buscarPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca uma empresa pelo CNPJ informado (aceita com ou sem máscara).
     */
    @GetMapping("/search")
    public ResponseEntity<EmpresaDto> byCnpj(@RequestParam String cnpj) {
        return service.buscarPorCnpj(cnpj).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Verifica existência por tipoPessoa (CPF/CNPJ) e número de documento (com ou sem máscara).
     * Retorna JSON { "exists": true|false }.
     */
    @GetMapping("/exists")
    public ResponseEntity<ExistsResponse> exists(@RequestParam String tipoPessoa, @RequestParam String numeroDocumento) {
        String tp = (tipoPessoa == null ? "" : tipoPessoa.trim().toUpperCase());
        String digits = numeroDocumento == null ? "" : numeroDocumento.replaceAll("[^0-9]", "");
        if (tp.isBlank() || digits.isBlank()) {
            return ResponseEntity.ok(new ExistsResponse(false));
        }
        boolean found = service
                .listar()
                .stream()
                .anyMatch(e -> tp.equalsIgnoreCase(e.tipoPessoa()) && digits.equals(e.numeroDocumento()));
        return ResponseEntity.ok(new ExistsResponse(found));
    }

    public record ExistsResponse(boolean exists) {}

    /**
     * Cria uma nova empresa.
     * Usa validação de bean (@Valid) para garantir obrigatoriedade de campos.
     * Retorna 201 Created com cabeçalho Location apontando para o recurso criado.
     */
    @PostMapping
    public ResponseEntity<EmpresaDto> create(@Valid @RequestBody EmpresaCreateDto payload) {
        EmpresaDto created = service.criar(payload);
        // Monta a URI do novo recurso: /api/empresas/{id}
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Atualiza uma empresa existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EmpresaDto> update(@PathVariable long id, @Valid @RequestBody EmpresaCreateDto payload) {
        return service.atualizar(id, payload)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Exclui uma empresa por id.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        boolean removed = service.excluirPorId(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
