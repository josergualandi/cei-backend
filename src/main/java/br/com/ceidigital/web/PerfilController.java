package br.com.ceidigital.web;

import br.com.ceidigital.service.PerfilService;
import br.com.ceidigital.web.dto.response.PerfilDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints de leitura para Perfil. */
@RestController
@RequestMapping("/api/perfis")
public class PerfilController {

    private final PerfilService service;

    public PerfilController(PerfilService service) {
        this.service = service;
    }

    @GetMapping
    public List<PerfilDto> list() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerfilDto> get(@PathVariable long id) {
        return service.buscarPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
