package br.com.ceidigital.security;

import br.com.ceidigital.domain.Perfil;
import br.com.ceidigital.domain.Usuario;
import br.com.ceidigital.repository.PerfilRepository;
import br.com.ceidigital.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * Inicializa dados essenciais em desenvolvimento: cria usuário ADMIN caso não exista.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           PerfilRepository perfilRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Garante perfil ADMIN
        Perfil adminPerfil = perfilRepository.findByNome("ADMIN").orElseGet(() -> {
            Perfil p = new Perfil();
            p.setNome("ADMIN");
            p.setDescricao("Perfil de administrador do sistema");
            return perfilRepository.save(p);
        });

        // Garante usuário admin
        usuarioRepository.findByEmail(adminEmail).ifPresentOrElse(
                u -> log.info("Usuário ADMIN já existente: {}", adminEmail),
                () -> {
                    Usuario u = new Usuario();
                    u.setNome("Administrador");
                    u.setEmail(adminEmail);
                    u.setSenha(passwordEncoder.encode(adminPassword));
                    u.setAtivo(true);
                    u.setPerfis(Set.of(adminPerfil));
                    usuarioRepository.save(u);
                    log.warn("Usuário ADMIN criado: {} (altere a senha em produção)", adminEmail);
                }
        );
    }
}
