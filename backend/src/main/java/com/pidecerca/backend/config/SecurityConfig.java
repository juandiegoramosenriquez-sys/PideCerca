package com.pidecerca.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String NEGOCIO = "NEGOCIO";
    private static final String CLIENTE = "CLIENTE";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final String origenesPermitidos;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Value("${pidecerca.cors.origenes}") String origenesPermitidos) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.origenesPermitidos = origenesPermitidos;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOriginPatterns(
                Arrays.stream(origenesPermitidos.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList()
        );
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("*"));
        configuracion.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuracion);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(sesion ->
                        sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint((request, response, ex) ->
                                escribirError(response, 401, "Debes iniciar sesión"))
                        .accessDeniedHandler((request, response, ex) ->
                                escribirError(response, 403, "No tienes permiso para realizar esta acción"))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/registro", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/salud").permitAll()

                        .requestMatchers("/api/admin/**").hasRole(ADMIN)

                        .requestMatchers("/api/usuarios/me", "/api/usuarios/me/**").authenticated()
                        .requestMatchers("/api/usuarios/**").hasRole(ADMIN)

                        .requestMatchers("/api/negocios/mio", "/api/negocios/mio/**").hasRole(NEGOCIO)
                        .requestMatchers(HttpMethod.GET, "/api/negocios/**").authenticated()
                        .requestMatchers("/api/negocios/**").hasRole(ADMIN)

                        .requestMatchers(HttpMethod.GET, "/api/categorias/mias").hasRole(NEGOCIO)
                        .requestMatchers(HttpMethod.GET, "/api/categorias/**").authenticated()
                        .requestMatchers("/api/categorias/**").hasAnyRole(NEGOCIO, ADMIN)

                        .requestMatchers(HttpMethod.GET, "/api/productos/mios").hasRole(NEGOCIO)
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").authenticated()
                        .requestMatchers("/api/productos/**").hasAnyRole(NEGOCIO, ADMIN)

                        .requestMatchers(HttpMethod.POST, "/api/pedidos").hasRole(CLIENTE)
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/mios").hasRole(CLIENTE)
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/negocio").hasRole(NEGOCIO)
                        .requestMatchers(HttpMethod.POST, "/api/pedidos/*/cancelar").hasAnyRole(CLIENTE, ADMIN)
                        .requestMatchers(HttpMethod.POST,
                                "/api/pedidos/*/aceptar",
                                "/api/pedidos/*/preparar",
                                "/api/pedidos/*/listo",
                                "/api/pedidos/*/recogido",
                                "/api/pedidos/*/rechazar").hasAnyRole(NEGOCIO, ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/*/tiempo").hasAnyRole(NEGOCIO, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/pedidos").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/*").authenticated()

                        .requestMatchers("/api/pagos/**").hasRole(ADMIN)
                        .requestMatchers("/api/detalles-pedido/**").hasRole(ADMIN)

                        .requestMatchers("/api/notificaciones/**").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    private static void escribirError(
            jakarta.servlet.http.HttpServletResponse response,
            int status,
            String mensaje) throws java.io.IOException {

        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"mensaje\":\"" + mensaje + "\"}");
    }
}
