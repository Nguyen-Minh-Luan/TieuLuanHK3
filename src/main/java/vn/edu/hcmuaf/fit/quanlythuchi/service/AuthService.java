package vn.edu.hcmuaf.fit.quanlythuchi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.config.JwtUtil;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.AuthRepository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthRepository authRepo;
    private final JwtUtil jwt;
    private BCryptPasswordEncoder hashMachine = new BCryptPasswordEncoder();

    public Optional<User> findById(Long id) {
        return authRepo.findById(id);
    }

    public User createUser(String username, String password, String fullName, String email) {
        User u = new User();
        String hashedPassword = hashMachine.encode(password);
        u.setUsername(username);
        u.setPassword(hashedPassword);
        u.setFullName(fullName);
        u.setEmail(email);
        u.setRole(0);
        return authRepo.save(u);
    }

    public User createAmin(String username, String password, String fullName, String email) {
        User u = new User();
        String hashedPassword = hashMachine.encode(password);
        u.setUsername(username);
        u.setPassword(hashedPassword);
        u.setFullName(fullName);
        u.setEmail(email);
        u.setRole(1);
        u.setIsDeleted(false);
        u.setCreated_at(new Date());
        return authRepo.save(u);
    }

    public String checkLogin(String username, String password) {
        Optional<User> optionalUser = authRepo.findByUsername(username);
        if (optionalUser.isPresent()) {
            User u = optionalUser.get();
            String hashedPassword = hashMachine.encode(password);
            if (username.equals(u.getUsername()) && hashMachine.matches(password, u.getPassword())) {
                return jwt.generateToken(u);
            }
        }
        return "Đăng Nhập Thất Bại";
    }
    public boolean deleteUser(String username){
        Optional<User> optionalUser = authRepo.findByUsername(username);
        if(optionalUser.isPresent()){
            User u = optionalUser.get();
            u.setIsDeleted(true);
            authRepo.save(u);
            return true;
        }
        return false;
    }
    public void updateUser(Long id, User user) {
        Optional<User> optionalUser = authRepo.findById(id);
        try {
            if (optionalUser.isPresent()) {
                User u = optionalUser.get();
                u.setUsername(user.getUsername());
                if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                    u.setPassword(hashMachine.encode(user.getPassword()));
                }
                u.setEmail(user.getEmail());
                u.setFullName(user.getFullName());
                authRepo.save(u);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi cập nhật: Có thể do trùng Email hoặc Username! " + e.getMessage());
        }
    }
}
