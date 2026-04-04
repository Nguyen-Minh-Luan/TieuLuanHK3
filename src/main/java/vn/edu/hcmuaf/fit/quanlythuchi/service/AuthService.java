package vn.edu.hcmuaf.fit.quanlythuchi.service;

import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.UserResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;

import java.util.Date;
import java.util.Optional;

@Service
public interface AuthService{
     Long createUser(String username, String password, String fullName, String email);
     User createAmin(String username, String password, String fullName, String email);
     UserResponseDTO checkLogin(String username, String password);
     boolean deleteUser(Long id);
     void updateUser(Long id, User user);
}
