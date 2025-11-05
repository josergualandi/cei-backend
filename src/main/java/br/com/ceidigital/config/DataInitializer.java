package br.com.ceidigital.config;

import br.com.ceidigital.domain.Perfil;
import br.com.ceidigital.repository.PerfilRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Inicializa perfis básicos na base de dados quando não existirem.
 * Garante a presença de "MASTER" (dono/admin geral) e "USER" (usuário comum).
 */
@Component("perfilDataInitializer")
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private final PerfilRepository perfilRepository;

    public DataInitializer(PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
    }

    @Override
    public void run(String... args) {
        ensurePerfil("MASTER", "Perfil master (dono) com acesso total à aplicação");
        ensurePerfil("USER", "Perfil de usuário padrão");
    }

    private void ensurePerfil(String nome, String descricao){
        perfilRepository.findByNome(nome).orElseGet(() -> {
            Perfil p = new Perfil();
            p.setNome(nome);
            p.setDescricao(descricao);
            return perfilRepository.save(p);
        });
    }
}
