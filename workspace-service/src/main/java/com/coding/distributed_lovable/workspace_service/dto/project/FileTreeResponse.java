package com.coding.distributed_lovable.workspace_service.dto.project;

import com.coding.distributed_lovable.common_lib.dto.FileNode;

import java.util.List;

public record FileTreeResponse(List<FileNode> files) {}
