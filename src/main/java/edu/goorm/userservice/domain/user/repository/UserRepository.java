package edu.goorm.userservice.domain.user.repository;

import edu.goorm.userservice.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository  extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

}
