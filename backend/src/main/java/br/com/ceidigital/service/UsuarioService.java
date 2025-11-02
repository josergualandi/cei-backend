package br.com.ceidigital.service;

import br.com.ceidigital.web.dto.response.UsuarioDto;

import java.util.List;
import java.util.Optional;

/** Serviço para regras relacionadas a Usuário. */
public interface UsuarioService {
    List<UsuarioDto> listar();
    Optional<UsuarioDto> buscarPorId(long id);
    /** Busca por e-mail (para autenticação/roles). */
    Optional<UsuarioDto> buscarPorEmail(String email);
}
