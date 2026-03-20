package com.coding.distributed_lovable.workspace_service.service;


import com.coding.distributed_lovable.workspace_service.dto.deploy.DeployResponse;

public interface DeploymentService {

  DeployResponse deploy(Long projectId);
}
