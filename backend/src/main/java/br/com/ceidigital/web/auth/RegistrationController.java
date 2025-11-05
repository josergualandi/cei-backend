package br.com.ceidigital.web.auth;

import br.com.ceidigital.domain.Usuario;
import br.com.ceidigital.repository.PerfilRepository;
import br.com.ceidigital.repository.UsuarioRepository;
import br.com.ceidigital.service.NotificationService;
import br.com.ceidigital.service.RegistrationService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/auth/register")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final NotificationService notificationService;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(RegistrationService registrationService,
                                  NotificationService notificationService,
                                  UsuarioRepository usuarioRepository,
                                  PerfilRepository perfilRepository,
                                  PasswordEncoder passwordEncoder) {
        this.registrationService = registrationService;
        this.notificationService = notificationService;
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Solicita envio de token por e-mail e SMS (stub) */
    @PostMapping("/request-token")
    public ResponseEntity<?> requestToken(@RequestBody RequestToken body){
        String email = body.email().trim().toLowerCase();
        // Se já existe usuário com este e-mail, retornar 409 imediatamente
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new SimpleProblem("usuario.ja.existe"));
        }
        // Gera token válido por 10 min
        String token = registrationService.generateAndStoreToken(email, body.telefone(), 10);
        notificationService.sendEmail(email, "Código de confirmação CEI", "Seu código: " + token);
        if (body.telefone() != null && !body.telefone().isBlank()) {
            String msg = "CEI: seu código é " + token;
            notificationService.sendSms(body.telefone(), msg);
            // Opcionalmente, envia também por WhatsApp quando habilitado
            if (notificationService.isWhatsappEnabled()) {
                notificationService.sendWhatsapp(body.telefone(), msg);
            }
        }
        return ResponseEntity.ok().build();
    }

    /** Confirma token e cria usuário ativo */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody ConfirmRequest body){
        String email = body.email().trim().toLowerCase();
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new SimpleProblem("usuario.ja.existe"));
        }
        boolean ok = registrationService.verify(email, body.token());
        if (!ok) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new SimpleProblem("token.invalido.ou.expirado"));
        }
        Usuario u = new Usuario();
        u.setNome(body.nome());
        u.setEmail(email);
        u.setSenha(passwordEncoder.encode(body.senha()));
        u.setAtivo(true);
        // perfil padrão USER se existir
        perfilRepository.findByNome("USER").ifPresent(p -> u.getPerfis().add(p));
        usuarioRepository.save(u);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // DTOs
    public record RequestToken(@NotBlank @Email String email, @NotBlank String telefone) {}
    public record ConfirmRequest(@NotBlank String nome, @NotBlank @Email String email,
                                 @NotBlank String senha, @NotBlank String token) {}

    public record SimpleProblem(String detail) {}
}
