package vn.edu.hcmuaf.fit.quanlythuchi.service.auth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.config.JwtUtil;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.auth.RegisterRequestDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.auth.UserDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.auth.UserResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.AuthRepository;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
    private final AuthRepository authRepo;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public UserDTO createUser(RegisterRequestDTO request) {
        User u = new User();
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        u.setUsername(request.getUsername());
        u.setPassword(hashedPassword);
        u.setFullName(request.getFullName());
        u.setEmail(request.getEmail());
        u.setRole(0);
        u.setIsDeleted(false);
        u.setCreated_at(new Date());
        return toDTO(authRepo.save(u));
    }
    @Override

    public UserResponseDTO login(String username,String password) {
        User user = authRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng!"));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new RuntimeException("Tài khoản đã bị vô hiệu hóa!");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Sai Mật khẩu!");
        }

        return UserResponseDTO.builder()
                .username(user.getUsername())
                .fullName(user.getFullName())
                .token(jwtUtil.generateToken(user))
                .build();
    }
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = authRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new IllegalStateException("Tài khoản này đã bị xóa trước đó!");
        }

        user.setIsDeleted(true);
        authRepo.save(user);
    }
    @Override
    @Transactional
    public UserDTO updateUser(Long id, RegisterRequestDTO request) {
        User user = authRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new IllegalStateException("Không thể cập nhật tài khoản đã bị xóa!");
        }
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            user.setEmail(request.getEmail());
        }
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return toDTO(authRepo.save(user));
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
