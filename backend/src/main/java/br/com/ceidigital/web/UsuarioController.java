package br.com.ceidigital.web;

import br.com.ceidigital.service.UsuarioService;
import br.com.ceidigital.web.dto.response.UsuarioDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints de leitura para Usuário. */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<UsuarioDto> list() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDto> get(@PathVariable long id) {
        return service.buscarPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
