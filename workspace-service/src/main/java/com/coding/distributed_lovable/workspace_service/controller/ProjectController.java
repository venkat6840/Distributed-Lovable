package com.coding.distributed_lovable.workspace_service.controller;

import com.coding.distributed_lovable.workspace_service.dto.deploy.DeployResponse;
import com.coding.distributed_lovable.workspace_service.dto.project.ProjectRequest;
import com.coding.distributed_lovable.workspace_service.dto.project.ProjectResponse;
import com.coding.distributed_lovable.workspace_service.dto.project.ProjectSummaryResponse;
import com.coding.distributed_lovable.workspace_service.service.DeploymentService;
import com.coding.distributed_lovable.workspace_service.service.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @Author - Venkatesh G
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {

  private final ProjectService projectService;
  private final DeploymentService deploymentService;

  @GetMapping
  public ResponseEntity<List<ProjectSummaryResponse>> getMyProjects() {
    Long userId = 1L;
    return ResponseEntity.ok(projectService.getUserProjects());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProjectSummaryResponse> getProjectByID(@PathVariable Long id) {
    Long userId = 1L;
    return ResponseEntity.ok(projectService.getProjectDetailsById(id));
  }

  @PostMapping
  public ResponseEntity<ProjectResponse> createProject(
      @RequestBody @Valid ProjectRequest projectRequest) {
    Long userid = 1L;

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(projectService.createProject(projectRequest));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<ProjectResponse> updateProject(
      @PathVariable Long id, @RequestBody @Valid ProjectRequest request) {
    Long userId = 1L;
    return ResponseEntity.ok(projectService.updateProject(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
    Long userId = 1L;
    projectService.softDelete(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/deploy")
  public ResponseEntity<DeployResponse> deployProject(@PathVariable Long id) {
    return ResponseEntity.ok(deploymentService.deploy(id));
  }
}
