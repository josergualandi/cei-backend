package br.com.ceidigital.web;

import br.com.ceidigital.domain.Empresa;
import br.com.ceidigital.repository.EmpresaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para recursos de Empresa.
 * Disponibiliza endpoints CRUD em /api/empresas.
 */
@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaRepository repository;

    public EmpresaController(EmpresaRepository repository) {
        this.repository = repository;
    }

    /**
     * Lista todas as empresas.
     */
    @GetMapping
    public List<Empresa> list() {
        return repository.findAll();
    }

    /**
     * Busca uma empresa por id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Empresa> get(@PathVariable long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca uma empresa pelo CNPJ informado (aceita com ou sem máscara).
     */
    @GetMapping("/search")
    public ResponseEntity<Empresa> byCnpj(@RequestParam String cnpj) {
        // Normaliza o CNPJ removendo caracteres que não são dígitos
        Optional<Empresa> opt = repository.findByCnpj(cnpj.replaceAll("[^0-9]", ""));
        return opt.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Cria uma nova empresa.
     * Usa validação de bean (@Valid) para garantir obrigatoriedade de campos.
     * Retorna 201 Created com cabeçalho Location apontando para o recurso criado.
     */
    @PostMapping
    public ResponseEntity<Empresa> create(@Valid @RequestBody Empresa body) {
        if (body.getNome() == null || body.getNome().isBlank() || body.getCnpj() == null || body.getCnpj().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        body.setId(null);
    Empresa saved = repository.save(body);
    // Monta a URI do novo recurso: /api/empresas/{id}
    var location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(saved.getId())
        .toUri();
    return ResponseEntity.created(location).body(saved);
    }

    /**
     * Exclui uma empresa por id.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
