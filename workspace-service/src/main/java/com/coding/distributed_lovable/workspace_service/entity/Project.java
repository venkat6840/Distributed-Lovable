package com.coding.distributed_lovable.workspace_service.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@Table(
    name = "projects",
    indexes = {
      @Index(name = "idx_projects_updated_at_desc", columnList = "updated_at DESC, deleted_at"),
      @Index(name = "idx_projects_deleted_at", columnList = "deleted_at")
    })
public class Project {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(nullable = false)
  String name;

  Boolean isPublic = false;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  Instant createdAt;

  @UpdateTimestamp
  @Column(nullable = false, updatable = false)
  Instant updatedAt;

  Instant deletedAt;
}
