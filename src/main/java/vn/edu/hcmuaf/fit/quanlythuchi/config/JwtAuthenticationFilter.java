package vn.edu.hcmuaf.fit.quanlythuchi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    //Spring Security chỉ làm việc với SecurityContextHolder nó sẽ vào SecurityContextHolder để check những thứ có trong đó có hợp lệ để gọi api đó không
    // nếu hợp lệ nó sẽ cho qua
    //và để bỏ vào SecurityContextHolder thì ta cần phải chuyển về UsernamePasswordAuthenticationToken
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtil.validateJwtToken(jwt)) {

                User user = jwtUtil.getUserFromJwtToken(jwt);

                String roleName = "ROLE_USER";
                if (user.getRole() != null) {
                    switch (user.getRole()) {
                        case 1: roleName = "ROLE_ADMIN"; break;
                        case 2: roleName = "ROLE_KETOAN"; break;
                        case 3: roleName = "ROLE_THUQUY"; break;
                    }
                }
                List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(roleName));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("đã cho phép: " + user.getUsername() + " (Quyền: " + roleName + ") đi qua.");
            }
        } catch (Exception e) {
            System.out.println("Lỗi xác thực tại Filter: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
