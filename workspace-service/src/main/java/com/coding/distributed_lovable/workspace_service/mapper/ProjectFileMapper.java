package com.coding.distributed_lovable.workspace_service.mapper;

import com.coding.distributed_lovable.common_lib.dto.FileNode;
import com.coding.distributed_lovable.workspace_service.entity.ProjectFile;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectFileMapper {

  List<FileNode> convertListOfProjectFilesToFileNodes(List<ProjectFile> projectFiles);
}
