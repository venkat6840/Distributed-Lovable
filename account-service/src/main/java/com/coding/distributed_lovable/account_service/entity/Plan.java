package com.coding.distributed_lovable.account_service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * @Author - Venkatesh G
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Plan {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  String name;

  @Column(unique = true)
  String stripeCustomerId;

  @Column(unique = true)
  String stripePriceId;
  Integer maxProjects;
  Integer maxTokensPerDay;
  Integer maxPreviews;
  Boolean unlimitedAi;
  Boolean active;
}
