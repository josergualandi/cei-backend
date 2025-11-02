package br.com.ceidigital.security;

import br.com.ceidigital.repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final UsuarioRepository usuarioRepository;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, UsuarioRepository usuarioRepository) {
        this.jwtFilter = jwtFilter;
        this.usuarioRepository = usuarioRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/auth/login").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Observação: Não registramos manualmente um AuthenticationProvider.
    // O Spring Boot auto-configura um DaoAuthenticationProvider quando existem
    // beans de UserDetailsService e PasswordEncoder no contexto (o que já temos abaixo).
    // Isso evita o uso de métodos deprecated como setUserDetailsService e elimina warnings.

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            var user = usuarioRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
            if (!user.isAtivo()) {
                throw new UsernameNotFoundException("Usuário inativo");
            }
            // Constrói UserDetails com roles (ROLE_*) e permissões como authorities
            Set<String> auths = new HashSet<>();
            user.getPerfis().forEach(p -> {
                auths.add("ROLE_" + p.getNome());
                p.getPermissoes().forEach(pm -> auths.add(pm.getNome()));
            });
            var authorities = auths.stream().map(org.springframework.security.core.authority.SimpleGrantedAuthority::new).toList();
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getSenha())
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(!user.isAtivo())
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configuração de CORS para permitir o front-end Angular (localhost:4200) consumir a API.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowCredentials(true);
        cfg.addAllowedOriginPattern("http://localhost:4200");
        cfg.addAllowedHeader("Authorization");
        cfg.addAllowedHeader("Content-Type");
        cfg.addAllowedHeader("Accept");
        cfg.addAllowedMethod("GET");
        cfg.addAllowedMethod("POST");
        cfg.addAllowedMethod("PUT");
        cfg.addAllowedMethod("DELETE");
        cfg.addAllowedMethod("OPTIONS");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
