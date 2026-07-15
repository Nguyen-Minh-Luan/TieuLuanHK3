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
            corsConfiguration.setAllowedOrigins(java.util.List.of("http://localhost:5173","http://127.0.0.1:5173", "http://localhost:3000"));
            corsConfiguration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            corsConfiguration.setAllowedHeaders(java.util.List.of("*"));
            corsConfiguration.setAllowCredentials(true);
            return corsConfiguration;
        }))
        .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/logout").authenticated()
                        .requestMatchers(HttpMethod.POST, "/auth/user").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/auth/user/*").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/auth/user/*").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/pdf/transactions/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/pdf/reports/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/user").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/debts","/debts/**").permitAll()
                        .requestMatchers("/categories","/categories/**").permitAll()

                        // ── Quỹ (Fund) ──
                        // Xem số dư: tất cả role đã login
                        .requestMatchers(HttpMethod.GET, "/funds", "/funds/**").authenticated()
                        // Tạo/sửa/xóa quỹ: chỉ Admin
                        .requestMatchers(HttpMethod.POST, "/funds").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/funds/*").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/funds/*").hasAuthority("ROLE_ADMIN")

                        // ── Giao dịch (Transaction) ──
                        // Tổng thu/tổng chi toàn hệ thống: Admin, Quỹ, Tổng hợp (không cho Kế toán Thu Chi)
                        .requestMatchers(HttpMethod.GET, "/transactions/total-income", "/transactions/total-expense")
                                .hasAnyAuthority("ROLE_ADMIN", "ROLE_THUQUY", "ROLE_TONGHOP")
                        // Tạo phiếu: Admin và Kế toán Thu Chi
                        .requestMatchers(HttpMethod.POST, "/transactions").hasAnyAuthority("ROLE_ADMIN", "ROLE_KETOAN")
                        // Các thao tác còn lại (GET list, GET/{id}, PUT, DELETE): authenticated()
                        // — ownership check thực sự được xử lý ở TransactionController
                        .requestMatchers("/transactions", "/transactions/**").authenticated()

                        // ── Chuyển quỹ (FundTransfer) ──
                        .requestMatchers(HttpMethod.POST, "/fund-transfers").hasAnyAuthority("ROLE_ADMIN", "ROLE_THUQUY")
                        // Xem lịch sử: Admin, Quỹ, Tổng hợp (bỏ Kế toán Thu Chi)
                        .requestMatchers(HttpMethod.GET, "/fund-transfers", "/fund-transfers/**")
                                .hasAnyAuthority("ROLE_ADMIN", "ROLE_THUQUY", "ROLE_TONGHOP")

                        // ── Kiểm kê quỹ (Reconciliation) ──
                        .requestMatchers(HttpMethod.POST, "/reconciliations", "/reconciliations/*/close", "/reconciliations/*/reopen")
                                .hasAnyAuthority("ROLE_ADMIN", "ROLE_THUQUY")
                        .requestMatchers(HttpMethod.PATCH, "/reconciliations/*").hasAnyAuthority("ROLE_ADMIN", "ROLE_THUQUY")
                        .requestMatchers(HttpMethod.DELETE, "/reconciliations/*").hasAnyAuthority("ROLE_ADMIN", "ROLE_THUQUY")
                        // Xem kiểm kê: Admin, Quỹ, Tổng hợp (siết lại từ authenticated())
                        .requestMatchers(HttpMethod.GET, "/reconciliations", "/reconciliations/**")
                                .hasAnyAuthority("ROLE_ADMIN", "ROLE_THUQUY", "ROLE_TONGHOP")

                        // ── Báo cáo (Report) ──
                        // Tất cả method: chỉ Admin và Kế toán Tổng hợp
                        .requestMatchers("/reports", "/reports/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TONGHOP")

                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
