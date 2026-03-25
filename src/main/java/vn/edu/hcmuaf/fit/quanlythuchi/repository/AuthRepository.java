package vn.edu.hcmuaf.fit.quanlythuchi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;

import java.util.Optional;

@Repository
//nói thẳng ra Repository nó chính là DAO ( Data Access Object), có chức năng đi lấy, thêm, xoá, sửa dữ liệu từ Database
//1.Lý do tại sao JPARepository là 1 interface tại sao lại dùng extends mà không dùng impleents?
//Vì đó là luật của JAVA vì AuthRepository là 1 interface và JPARepository cũng là 1 interface
//để 2 interface có thể chơi với nhau thì phải dùng extends
//2.Tại sao AuthRepository lại khai báo là 1 interface mà không phải là class ?
//Vì nếu khai báo class thì ta phải override lại tất cả các hàm có trong interface JPARepository
//rất phức tạp vì JPARepository đã cung cấp đầy đủ CRUD cho ta hết rồi không cần phải viết lại cái gì nữa
public interface AuthRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
