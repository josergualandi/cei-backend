package br.com.ceidigital.security;

import br.com.ceidigital.repository.UsuarioRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Filtro que extrai o token JWT do header Authorization e autentica a requisição.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String email = jwtService.extractUsername(token);
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    var userOpt = usuarioRepository.findByEmail(email);
                    if (userOpt.isPresent() && userOpt.get().isAtivo() && jwtService.isTokenValid(token, email)) {
                        var usuario = userOpt.get();
                        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
                        usuario.getPerfis().forEach(perfil -> {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + perfil.getNome()));
                            perfil.getPermissoes().forEach(perm -> authorities.add(new SimpleGrantedAuthority(perm.getNome())));
                        });
                        var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception ignored) {
                // Token inválido/expirado: segue sem autenticar; endpoints protegidos serão bloqueados pelo Security
            }
        }

        filterChain.doFilter(request, response);
    }
}
