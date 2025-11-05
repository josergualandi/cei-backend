package br.com.ceidigital.web;

import br.com.ceidigital.domain.Usuario;
import br.com.ceidigital.repository.UsuarioRepository;
import br.com.ceidigital.service.EmpresaService;
import br.com.ceidigital.web.dto.DtoMapper;
import br.com.ceidigital.web.dto.request.EmpresaCreateDto;
import br.com.ceidigital.web.dto.response.EmpresaDto;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador REST para recursos de Empresa.
 * Disponibiliza endpoints CRUD em /api/empresas.
 */
@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService service;
    private final UsuarioRepository usuarioRepository;

    public EmpresaController(EmpresaService service, UsuarioRepository usuarioRepository) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Lista todas as empresas.
     */
    @GetMapping
    public List<EmpresaDto> list() {
        // MASTER vê todas; usuário comum vê apenas a própria
        if (isAdmin()) {
            return service.listar();
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? Objects.toString(auth.getName(), null) : null;
        if (email == null) return List.of();
        Optional<Usuario> u = usuarioRepository.findByEmail(email.trim().toLowerCase());
        if (u.isEmpty() || u.get().getEmpresa() == null) return List.of();
        return List.of(DtoMapper.toDto(u.get().getEmpresa()));
    }

    /**
     * Busca uma empresa por id. ADMIN_MAIN pode ver qualquer; usuário comum apenas a sua.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmpresaDto> get(@PathVariable long id) {
        if (isAdmin()) {
            return service.buscarPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        }
        var current = getCurrentUser();
        if (current.isEmpty() || current.get().getEmpresa() == null) return ResponseEntity.status(403).build();
        if (!current.get().getEmpresa().getId().equals(id)) return ResponseEntity.status(403).build();
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
        boolean found = service.existsByTipoPessoaAndNumeroDocumento(tipoPessoa, numeroDocumento);
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
    if (isAdmin()) {
        return service.atualizar(id, payload)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    var current = getCurrentUser();
    if (current.isEmpty() || current.get().getEmpresa() == null) return ResponseEntity.status(403).build();
    if (!current.get().getEmpresa().getId().equals(id)) return ResponseEntity.status(403).build();
    // Usuário comum: não pode alterar tipo/documento
    var sanitized = new EmpresaCreateDto(
        null, // tipoPessoa bloqueado
        null, // numeroDocumento bloqueado
        payload.nomeRazaoSocial(),
        null, // nome alias
        null, // cnpj alias
        payload.nomeFantasia(),
        payload.tipoAtividade(),
        payload.cnae(),
        payload.dataAbertura(),
        payload.situacao(),
        payload.endereco(),
        payload.cidade(),
        payload.estado(),
        payload.telefone(),
        payload.email()
    );
    return service.atualizar(id, sanitized)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Exclui uma empresa por id.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        if (isAdmin()) {
            boolean removed = service.excluirPorIdForce(id);
            return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        }
        // Usuário comum não pode excluir
        return ResponseEntity.status(403).build();
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? Objects.toString(auth.getName(), null) : null;
        if (email == null) return false;
        return getCurrentUser()
                .map(u -> u.getPerfis().stream().anyMatch(p -> {
                    String n = p.getNome();
                    return n != null && (n.equalsIgnoreCase("MASTER") || n.equalsIgnoreCase("ADMIN_MAIN"));
                }))
                .orElse(false);
    }

    private Optional<Usuario> getCurrentUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? Objects.toString(auth.getName(), null) : null;
        if (email == null) return Optional.empty();
        return usuarioRepository.findByEmail(email.trim().toLowerCase());
    }
}
