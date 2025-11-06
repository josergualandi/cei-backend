package br.com.ceidigital.security;

import br.com.ceidigital.domain.Perfil;
import br.com.ceidigital.domain.Empresa;
import br.com.ceidigital.domain.Usuario;
import br.com.ceidigital.repository.EmpresaRepository;
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

import java.util.HashSet;
import java.util.Set;

/**
 * Inicializa dados essenciais em desenvolvimento:
 * - Garante perfis MASTER, USER e ADMIN_MAIN na base
 * - Garante um usuário admin inicial com perfil MASTER
 */
@Component("adminUserInitializer")
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.company.tipo:CNPJ}")
    private String adminEmpresaTipo;

    @Value("${app.admin.company.documento:00000000000000}")
    private String adminEmpresaDocumento;

    @Value("${app.admin.company.nome:Empresa Admin}")
    private String adminEmpresaNome;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           PerfilRepository perfilRepository,
                           EmpresaRepository empresaRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Garante os novos perfis na tabela "perfil"
        Perfil master = ensurePerfil("MASTER", "Perfil master (dono) com acesso total à aplicação");
        ensurePerfil("USER", "Perfil de usuário padrão");
        ensurePerfil("ADMIN_MAIN", "Perfil admin legado com acesso total (compatibilidade)");

        // Garante usuário admin com perfil MASTER (sem usar lambda, evitando captura de variáveis locais)
        var optAdmin = usuarioRepository.findByEmail(adminEmail);
        if (optAdmin.isPresent()) {
            Usuario u = optAdmin.get();
            boolean hasMaster = u.getPerfis().stream()
                    .anyMatch(p -> p.getNome() != null && p.getNome().equalsIgnoreCase("MASTER"));
            if (!hasMaster) {
                Set<Perfil> novos = new HashSet<>(u.getPerfis());
                novos.add(master);
                u.setPerfis(novos);
                usuarioRepository.save(u);
                log.info("Perfil MASTER adicionado ao usuário admin: {}", adminEmail);
            }
            ensureAdminEmpresa(u);
        } else {
            Usuario u = new Usuario();
            u.setNome("Administrador");
            u.setEmail(adminEmail);
            u.setSenha(passwordEncoder.encode(adminPassword));
            u.setAtivo(true);
            u.setPerfis(Set.of(master));
            ensureAdminEmpresa(u);
            usuarioRepository.save(u);
            log.warn("Usuário ADMIN (MASTER) criado: {} (altere a senha em produção)", adminEmail);
        }
    }

    private Perfil ensurePerfil(String nome, String descricao) {
        return perfilRepository.findByNome(nome).orElseGet(() -> {
            Perfil p = new Perfil();
            p.setNome(nome);
            p.setDescricao(descricao);
            return perfilRepository.save(p);
        });
    }

    private void ensureAdminEmpresa(Usuario u) {
        if (u.getEmpresa() != null) return;
    String tipo = (adminEmpresaTipo == null || adminEmpresaTipo.isBlank()) ? "CNPJ" : adminEmpresaTipo.trim().toUpperCase();
    String doc = adminEmpresaDocumento == null ? "" : adminEmpresaDocumento.replaceAll("[^0-9]", "");
    if (doc.isEmpty()) doc = "00000000000000";
    final String fTipo = tipo;
    final String fDoc = doc;

    Empresa emp = empresaRepository.findByTipoPessoaAndNumeroDocumento(fTipo, fDoc)
                .orElseGet(() -> {
                    Empresa e = new Empresa();
            e.setTipoPessoa(fTipo);
            e.setNumeroDocumento(fDoc);
                    e.setNomeRazaoSocial(adminEmpresaNome == null || adminEmpresaNome.isBlank() ? "Empresa Admin" : adminEmpresaNome);
                    e.setBloqueada(true);
                    return empresaRepository.save(e);
                });
        u.setEmpresa(emp);
        usuarioRepository.save(u);
    log.info("Usuário admin associado à empresa padrão: {} / {}", fTipo, fDoc);
    }
}
