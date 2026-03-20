package com.coding.distributed_lovable.account_service.mapper;

import com.coding.distributed_lovable.account_service.dto.subscription.SubscriptionResponse;
import com.coding.distributed_lovable.account_service.entity.Plan;
import com.coding.distributed_lovable.account_service.entity.Subscription;
import com.coding.distributed_lovable.common_lib.dto.PlanDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionResponseMapper {

  SubscriptionResponse toSubscriptionResponseFromSubscription(Subscription subscription);

  PlanDto toPlanResponse(Plan plan);
}
