package vn.edu.hcmuaf.fit.quanlythuchi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.quanlythuchi.entity.User;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<User, Long> {
}
