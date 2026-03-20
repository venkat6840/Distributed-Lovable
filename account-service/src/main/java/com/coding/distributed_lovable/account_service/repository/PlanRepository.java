package com.coding.distributed_lovable.account_service.repository;

import java.util.Optional;

import com.coding.distributed_lovable.account_service.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    Optional<Plan> findByStripePriceId(String id);
}
