package com.coding.distributed_lovable.workspace_service.entity;

import com.coding.distributed_lovable.common_lib.enums.ProjectRole;
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
@Table(name = "project_members")
public class ProjectMember {

  @EmbeddedId ProjectMemberId id;

  @ManyToOne
  @MapsId("projectId")
  Project project;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  ProjectRole projectRole;

  Long invitedBy;

  @CreationTimestamp
  Instant invitedAt;

  @UpdateTimestamp
  Instant acceptedAt;
}
