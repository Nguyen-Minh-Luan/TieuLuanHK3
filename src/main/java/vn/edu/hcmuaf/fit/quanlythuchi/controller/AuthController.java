package vn.edu.hcmuaf.fit.quanlythuchi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
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
                user.getUsername(), user.getPassword(), user.getFullName(), user.getEmail());
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

    @DeleteMapping("/user/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        boolean isDeleted = authService.deleteUser(id);
        if (isDeleted) {
            return ApiResponse.ok(null, "Xóa tài khoản thành công");
        } else {
            return ApiResponse.error("Không tìm thấy tài khoản với ID: " + id, "USER_NOT_FOUND");
        }
    }

    @PatchMapping("/user/{id}")
    public ResponseEntity<ApiResponse<Void>> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
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
            @RequestParam(defaultValue = "1")        int page,
            @RequestParam(defaultValue = "10")       int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc")      String sortDir) {
        return ApiResponse.ok(
                PagedResponseDTO.from(
                        authService.getAllUsers(keyword, role, page, size, sortBy, sortDir)));
    }
}

