package com.coding.distributed_lovable.account_service.repository;

import com.coding.distributed_lovable.account_service.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
  Optional<User> findByUsernameIgnoreCase(String email);
}
