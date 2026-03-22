package vn.edu.hcmuaf.fit.quanlythuchi.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;
import vn.edu.hcmuaf.fit.quanlythuchi.service.AuthService;

import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    AuthService authService;
    @PostMapping("/register")
    public void register(@RequestBody User user){
        authService.createUser(user.getUsername(),user.getPassword(),user.getFullName(),user.getEmail());
    }
    @PostMapping("/login")
    public boolean checkLogin(@RequestBody User u){
        return authService.checkLogin(u.getUsername(),u.getPassword());
    }
}
