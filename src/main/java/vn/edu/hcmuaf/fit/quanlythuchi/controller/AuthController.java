package vn.edu.hcmuaf.fit.quanlythuchi.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.UserResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;
import vn.edu.hcmuaf.fit.quanlythuchi.service.auth.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/user")
    public Long register(@RequestBody User user) {
        Long id = authService.createUser(user.getUsername(), user.getPassword(), user.getFullName(), user.getEmail());
        return id;
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> checkLogin(@RequestBody User u) {
        ResponseEntity<UserResponseDTO> res;
        try {
            res = new ResponseEntity<>(authService.checkLogin(u.getUsername(), u.getPassword()), HttpStatus.OK);
        } catch (RuntimeException e) {
            res = new ResponseEntity<>(UserResponseDTO.builder()
                    .message(e.getMessage())
                    .build(), HttpStatus.BAD_REQUEST);
        }
        return res;
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<UserResponseDTO> deleteUser(@PathVariable Long id) {
        boolean isDeleted = authService.deleteUser(id);
        if (isDeleted) {
            return ResponseEntity.ok(
                    UserResponseDTO.builder()
                            .status("delete user")
                            .message("Đã xoá thành công!")
                            .build()
            );
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    UserResponseDTO.builder()
                            .status("delete user")
                            .message("Không tìm thấy user với ID: " + id)
                            .build()
            );
        }
    }

    @PatchMapping("/user/{id}")
    public void updateUser(@PathVariable Long id, @RequestBody User user) {
        authService.updateUser(id, user);
    }
}
