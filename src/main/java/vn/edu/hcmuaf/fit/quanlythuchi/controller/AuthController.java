package vn.edu.hcmuaf.fit.quanlythuchi.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.config.ApiResponse;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.auth.RegisterRequestDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.auth.UserDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.auth.UserResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;
import vn.edu.hcmuaf.fit.quanlythuchi.service.auth.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/user")
    public ResponseEntity<ApiResponse<UserDTO>> register(@RequestBody RegisterRequestDTO request) {
        return ApiResponse.created(authService.createUser(request), "Tạo tài khoản thành công");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponseDTO>> login(@RequestBody String username, String password) {
        return ApiResponse.ok(authService.login(username,password), "Đăng nhập thành công");
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        authService.deleteUser(id);
        return ApiResponse.ok(null, "Xóa tài khoản thành công");
    }

    @PatchMapping("/user/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @RequestBody RegisterRequestDTO request) {
        return ApiResponse.ok(authService.updateUser(id, request), "Cập nhật tài khoản thành công");
    }
}
