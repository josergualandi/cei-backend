package br.com.ceidigital.web.auth;

import br.com.ceidigital.security.JwtService;
import br.com.ceidigital.service.UsuarioService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    public AuthController(AuthenticationManager authManager, JwtService jwtService, UsuarioService usuarioService) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
    }

    /**
     * Realiza login com email e senha e retorna um token JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest body) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(body.email(), body.senha())
        );
        // Gerar token a partir do email (username)
        String email = authentication.getName();
        String token = jwtService.generateToken(email);
        long expiresIn = jwtService.extractExpiration(token).getTime() / 1000 - System.currentTimeMillis() / 1000;

        // Retornar roles do usuário (perfis)
    var usuarioDto = usuarioService.buscarPorEmail(email).orElseThrow();
    Set<String> roles = usuarioDto.perfis().stream().map(p -> p.nome()).collect(Collectors.toSet());

        return ResponseEntity.ok(new LoginResponse("Bearer", token, expiresIn, roles));
    }

    // DTOs enxutos
    public static record LoginRequest(@NotBlank @Email String email, @NotBlank String senha) {}
    public static record LoginResponse(String tokenType, String accessToken, long expiresIn, Set<String> roles) {}
}
