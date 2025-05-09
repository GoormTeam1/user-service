package edu.goorm.userservice.domain.user.repository;

import edu.goorm.userservice.domain.user.entity.Category;
import edu.goorm.userservice.domain.user.entity.UserInterest;
import edu.goorm.userservice.domain.user.entity.UserInterestId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserInterestRepository extends JpaRepository<UserInterest, UserInterestId> {
  void deleteAllByIdUserId(Long userId);

  @Query("SELECT ui.id.category FROM UserInterest ui WHERE ui.id.userId = :userId")
  List<Category> findCategoriesByUserId(@Param("userId") Long userId);

}
