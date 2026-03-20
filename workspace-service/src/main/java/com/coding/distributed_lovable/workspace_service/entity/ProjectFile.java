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
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "project_files")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectFile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = false)
  Project project;

  @Column(nullable = false)
  String path;

  String minioObjectKey;

  @CreationTimestamp Instant createdAt;

  @UpdateTimestamp Instant updatedAt;
}
