package vn.edu.hcmuaf.fit.quanlythuchi.service.auth;

import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.UserResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;

@Service
public interface AuthService{
     Long createUser(String username, String password, String fullName, String email);
     User createAmin(String username, String password, String fullName, String email);
     UserResponseDTO checkLogin(String username, String password);
     boolean deleteUser(Long id);
     void updateUser(Long id, User user);
}
