package edu.goorm.userservice.domain.user.repository;

import edu.goorm.userservice.domain.user.entity.UserInterest;
import edu.goorm.userservice.domain.user.entity.UserInterestId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInterestRepository extends JpaRepository<UserInterest, UserInterestId> {
}
