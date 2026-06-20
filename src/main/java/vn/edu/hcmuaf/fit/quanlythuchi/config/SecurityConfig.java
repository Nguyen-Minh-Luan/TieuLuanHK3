package vn.edu.hcmuaf.fit.quanlythuchi.config;

import jakarta.servlet.FilterChain;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(request -> {
            var corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
            corsConfiguration.setAllowedOrigins(java.util.List.of("http://localhost:5173", "http://localhost:3000"));
            corsConfiguration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            corsConfiguration.setAllowedHeaders(java.util.List.of("*"));
            corsConfiguration.setAllowCredentials(true);
            return corsConfiguration;
        }))
        .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/user").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/auth/user/*").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/auth/user/*").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/pdf/transactions/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/pdf/reports/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/user").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/debts").permitAll()
                        .requestMatchers(HttpMethod.POST, "/debts").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/debts").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/debts").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/debts").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
