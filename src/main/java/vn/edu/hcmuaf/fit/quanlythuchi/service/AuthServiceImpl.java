package vn.edu.hcmuaf.fit.quanlythuchi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.config.JwtUtil;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.AuthRepository;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl {
    private final AuthRepository authRepo;
    private final JwtUtil jwt;
    private BCryptPasswordEncoder hashMachine = new BCryptPasswordEncoder();

    public Long createUser(String username, String password, String fullName, String email) {
        User u = new User();
        String hashedPassword = hashMachine.encode(password);
        u.setUsername(username);
        u.setPassword(hashedPassword);
        u.setFullName(fullName);
        u.setEmail(email);
        u.setIsDeleted(false);
        u.setRole(0);
        authRepo.save(u);
        return u.getId();
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
            if(u.getIsDeleted()){
                return "Tài khoản đã bị xoá";
            }
                String hashedPassword = hashMachine.encode(password);
                if (username.equals(u.getUsername()) && hashMachine.matches(password, u.getPassword())) {
                    return jwt.generateToken(u);
                }
        }
        return "Đăng Nhập Thất Bại";
    }
    public boolean deleteUser(Long id){
        Optional<User> optionalUser = authRepo.findById(id);
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
                if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
                    u.setUsername(user.getUsername());
                }
                if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                    u.setEmail(user.getEmail());
                }
                if (user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
                    u.setFullName(user.getFullName());
                }
                if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                    u.setPassword(hashMachine.encode(user.getPassword()));
                }
                authRepo.save(u);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi cập nhật: Có thể do trùng Email hoặc Username! " + e.getMessage());
        }
    }
}
