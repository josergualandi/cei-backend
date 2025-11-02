package br.com.ceidigital.service;

import br.com.ceidigital.web.dto.response.PermissaoDto;

import java.util.List;
import java.util.Optional;

/** Serviço para regras relacionadas a Permissão. */
public interface PermissaoService {
    List<PermissaoDto> listar();
    Optional<PermissaoDto> buscarPorId(long id);
}
