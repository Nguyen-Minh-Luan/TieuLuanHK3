package vn.edu.hcmuaf.fit.quanlythuchi.service.auth;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.quanlythuchi.dto.UserResponseDTO;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;

@Service
public interface AuthService {
     Long createUser(String username, String password, String fullName, String email);
     User createAmin(String username, String password, String fullName, String email);
     UserResponseDTO checkLogin(String username, String password);
     boolean deleteUser(Long id);
     void updateUser(Long id, User user);

     /**
      * Lấy danh sách người dùng có phân trang và tìm kiếm (chỉ ADMIN).
      * @param keyword  Tìm theo username, fullName, email
      * @param role     Lọc theo role (0 = User, 1 = Admin)
      * @param page     Số trang (1-based)
      * @param size     Số phần tử mỗi trang
      * @param sortBy   Field sắp xếp (mặc định "username")
      * @param sortDir  Chiều sắp xếp: "asc" hoặc "desc"
      */
     Page<UserResponseDTO> getAllUsers(String keyword, Integer role,
                                      int page, int size, String sortBy, String sortDir);
}
