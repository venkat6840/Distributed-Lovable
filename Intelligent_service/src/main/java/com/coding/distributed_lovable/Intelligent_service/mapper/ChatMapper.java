package com.coding.distributed_lovable.Intelligent_service.mapper;

import com.coding.distributed_lovable.Intelligent_service.dto.ChatResponse;
import com.coding.distributed_lovable.Intelligent_service.entity.ChatMessage;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatMapper {

  List<ChatResponse> fromListOfChatMessage(List<ChatMessage> chatMessages);
}
