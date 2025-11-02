package br.com.ceidigital.web;

import br.com.ceidigital.service.PermissaoService;
import br.com.ceidigital.web.dto.response.PermissaoDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints de leitura para Permissão. */
@RestController
@RequestMapping("/api/permissoes")
public class PermissaoController {

    private final PermissaoService service;

    public PermissaoController(PermissaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<PermissaoDto> list() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PermissaoDto> get(@PathVariable long id) {
        return service.buscarPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
