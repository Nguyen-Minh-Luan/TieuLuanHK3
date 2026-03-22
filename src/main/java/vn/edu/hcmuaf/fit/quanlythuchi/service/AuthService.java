package vn.edu.hcmuaf.fit.quanlythuchi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;
import vn.edu.hcmuaf.fit.quanlythuchi.repository.AuthRepository;

import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private AuthRepository authRepo;
    private BCryptPasswordEncoder hashMachine = new BCryptPasswordEncoder();
    public Optional<User> findById(Long id) {
        return authRepo.findById(id);
    }
    public User createUser(String username,String password,String fullName,String email){
        User u = new User();
        String hashedPassword = hashMachine.encode(password);
        u.setUsername(username);
        u.setPassword(hashedPassword);
        u.setFullName(fullName);
        u.setEmail(email);
        return authRepo.save(u);
    }
    public boolean checkLogin(String username , String password){
        User u = (User) authRepo.findByUsername(username);
        String hashedPassword = hashMachine.encode(password);
        System.out.println(hashedPassword);
        return username.equals(u.getUsername()) && hashMachine.matches(password,u.getPassword());
    }
}
