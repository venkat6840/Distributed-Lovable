package com.coding.distributed_lovable.account_service.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @Author - Venkatesh G
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  String username;
  String password;

  @Column(nullable = false)
  String name;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  Instant createdAt;

  @UpdateTimestamp
  @Column(nullable = false, updatable = false)
  Instant updatedAt;

  Instant deletedAt;

  @Column(unique = true)
  String stripeCustomerId;

}
