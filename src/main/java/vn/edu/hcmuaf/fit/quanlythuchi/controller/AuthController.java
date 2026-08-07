package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import vn.edu.hcmuaf.fit.quanlythuchi.config.ApiResponse;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.PagedResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.UserResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;
import vn.edu.hcmuaf.fit.quanlythuchi.service.auth.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/user")
    public ResponseEntity<ApiResponse<Long>> register(@RequestBody User user) {
        Long id = authService.createUser(
                user.getUsername(), user.getPassword(), user.getFullName(), user.getEmail(), user.getRole(), user.getStatus());
        return ApiResponse.created(id, "Tạo tài khoản thành công");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponseDTO>> checkLogin(@RequestBody User u) {
        try {
            UserResponseDTO result = authService.checkLogin(u.getUsername(), u.getPassword());
            return ApiResponse.ok(result, "Đăng nhập thành công");
        } catch (RuntimeException e) {
            return ApiResponse.unauthorized(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ApiResponse.ok(null, "Đăng xuất thành công");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody Map<String, String> body) {
        authService.forgotPassword(body.get("email"));
        return ApiResponse.ok(null, "Nếu email tồn tại trong hệ thống, chúng tôi đã gửi hướng dẫn khôi phục.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody Map<String, String> body) {
        try {
            String newPassword = authService.resetPassword(body.get("token"));
            return ApiResponse.ok(newPassword, "Mật khẩu mới đã được tạo");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage(), "RESET_FAILED");
        }
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        boolean isDeleted = authService.deleteUser(id);
        if (isDeleted) {
            return ApiResponse.ok(null, "Xóa tài khoản thành công");
        } else {
            return ApiResponse.error("Không tìm thấy tài khoản với ID: " + id, "USER_NOT_FOUND");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getMe() {
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            UserResponseDTO profile = authService.getCurrentUserProfile(username);
            return ApiResponse.ok(profile, "Lấy thông tin cá nhân thành công");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage(), "UNAUTHORIZED");
        }
    }

    @PatchMapping("/user/{id}")
    public ResponseEntity<ApiResponse<Void>> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = auth.getName();
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin) {
                UserResponseDTO currentUser = authService.getCurrentUserProfile(currentUsername);
                if (!currentUser.getId().equals(id)) {
                    return ApiResponse.error("Bạn không có quyền cập nhật người dùng khác", "FORBIDDEN");
                }
                // Người dùng thường không được sửa role và status
                user.setRole(null);
                user.setStatus(null);
            }

            authService.updateUser(id, user);
            return ApiResponse.ok(null, "Cập nhật tài khoản thành công");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage(), "UPDATE_FAILED");
        }
    }

    /**
     * GET /auth/user — Lấy danh sách người dùng có phân trang (chỉ ADMIN).
     */
    @GetMapping("/user")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponseDTO<UserResponseDTO>>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1")        int page,
            @RequestParam(defaultValue = "10")       int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc")      String sortDir) {
        return ApiResponse.ok(
                PagedResponseDTO.from(
                        authService.getAllUsers(keyword, role, status, page, size, sortBy, sortDir)));
    }
}

