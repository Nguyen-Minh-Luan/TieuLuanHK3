package vn.edu.hcmuaf.fit.quanlythuchi.service.auth;

import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.auth.RegisterRequestDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.auth.UserDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.auth.UserResponseDTO;

@Service
public interface AuthService{
     UserDTO createUser(RegisterRequestDTO request);
     UserResponseDTO login(String username,String password);
     void deleteUser(Long id);
     UserDTO updateUser(Long id, RegisterRequestDTO request);
}
