package br.com.ceidigital.service;

import br.com.ceidigital.web.dto.response.PerfilDto;

import java.util.List;
import java.util.Optional;

/** Serviço para regras relacionadas a Perfil. */
public interface PerfilService {
    List<PerfilDto> listar();
    Optional<PerfilDto> buscarPorId(long id);
}
