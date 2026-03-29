package vn.edu.hcmuaf.fit.quanlythuchi.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;
import vn.edu.hcmuaf.fit.quanlythuchi.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    @PostMapping("/user")
    public Long register(@RequestBody User user){
        Long id = authService.createUser(user.getUsername(),user.getPassword(),user.getFullName(),user.getEmail());
        return id;
    }
    @PostMapping("/login")
    public String checkLogin(@RequestBody User u){
        return authService.checkLogin(u.getUsername(),u.getPassword());
    }
    @DeleteMapping("/user/{id}")
    public void deleteUser(@PathVariable Long id){
        authService.deleteUser(id);
    }
    @PatchMapping("/user/{id}")
    public void updateUser(@PathVariable Long id, @RequestBody User user){
        authService.updateUser(id,user);
    }
}
