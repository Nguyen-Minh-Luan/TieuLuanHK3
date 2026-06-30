package vn.edu.hcmuaf.fit.quanlythuchi.service.auth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.config.JwtUtil;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.UserResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.AuthRepository;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
    private final AuthRepository authRepo;
    private final JwtUtil jwt;
    private BCryptPasswordEncoder hashMachine = new BCryptPasswordEncoder();

    @Override
    public Long createUser(String username, String password, String fullName, String email, Integer role, String status) {
        System.out.println("--- Đã vào createUser serviceImpl ---");
        User u = new User();
        String hashedPassword = hashMachine.encode(password);
        u.setUsername(username);
        u.setPassword(hashedPassword);
        u.setFullName(fullName);
        u.setEmail(email);
        u.setIsDeleted(false);
        u.setRole(role != null ? role : 0);
        u.setStatus(status != null ? status : "ACTIVE");
        authRepo.save(u);
        return u.getId();
    }
    @Override
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
    @Override
    public UserResponseDTO checkLogin(String username, String password) {
        Optional<User> optionalUser = authRepo.findByUsername(username);
        if (optionalUser.isPresent()) {
            User u = optionalUser.get();
            if(u.getIsDeleted()){
                throw new RuntimeException("Tài khoản đã bị xoá");
            }
            if (username.equals(u.getUsername()) && hashMachine.matches(password, u.getPassword())) {
                return UserResponseDTO.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .fullName(u.getFullName())
                        .email(u.getEmail())
                        .role(u.getRole())
                        .status(u.getStatus())
                        .token(jwt.generateToken(u))
                        .build();
            }
        }
        throw new RuntimeException("Đăng Nhập thất bại");
    }
    @Override
    @Transactional
    public boolean deleteUser(Long id){
        Optional<User> optionalUser = authRepo.findById(id);
        if(optionalUser.isPresent()){
            User u = optionalUser.get();
            if(u.getIsDeleted()){
                return true;
            }
            u.setIsDeleted(true);
            authRepo.save(u);
            return true;
        }
        return false;
    }
    @Override
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
                if (user.getRole() != null) {
                    u.setRole(user.getRole());
                }
                if (user.getStatus() != null && !user.getStatus().trim().isEmpty()) {
                    u.setStatus(user.getStatus());
                }
                authRepo.save(u);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi cập nhật: Có thể do trùng Email hoặc Username! " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Page<UserResponseDTO> getAllUsers(String keyword, Integer role, String status,
                                            int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);
        return authRepo.searchUsers(keyword, role, status, pageable)
                       .map(this::toUserResponseDTO);
    }

    /** Helper mapper: User entity → UserResponseDTO (dùng cho danh sách admin) */
    private UserResponseDTO toUserResponseDTO(User u) {
        return UserResponseDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .role(u.getRole())
                .status(u.getStatus())
                .build();
    }
}
