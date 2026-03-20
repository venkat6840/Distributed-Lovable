package com.coding.distributed_lovable.Intelligent_service.repository;

import com.coding.distributed_lovable.Intelligent_service.entity.UsageLog;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsageLogRepository extends JpaRepository<UsageLog, Long> {
  Optional<UsageLog> findAllByUserIdAndDate(Long userId, LocalDate today);
}
