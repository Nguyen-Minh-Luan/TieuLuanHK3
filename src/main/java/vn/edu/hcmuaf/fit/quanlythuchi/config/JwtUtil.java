package vn.edu.hcmuaf.fit.quanlythuchi.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expiration}")
    private int jwtExpiration;

    public Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // tạo JWT từ username
    public String generateToken(User u) {
        return Jwts.builder().setSubject(u.getUsername())
                .claim("id", u.getId())
                .claim("role", u.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public User getUserFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        String username = claims.getSubject();
        Integer role = claims.get("role", Integer.class);
        Long id = claims.get("id", Long.class);
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        return user;
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            System.out.println("Token không đúng định dạng JWT: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.out.println("Token đã hết hạn: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("Token không được hỗ trợ: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Chuỗi token bị trống: " + e.getMessage());
        }
        return false;
    }
}
