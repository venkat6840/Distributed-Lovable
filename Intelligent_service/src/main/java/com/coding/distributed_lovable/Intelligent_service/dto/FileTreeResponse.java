package com.coding.distributed_lovable.Intelligent_service.dto;

import com.coding.distributed_lovable.common_lib.dto.FileNode;

import java.util.List;

public record FileTreeResponse(List<FileNode> files) {}
