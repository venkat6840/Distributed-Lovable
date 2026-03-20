package com.coding.distributed_lovable.common_lib.event;

public record FileStoreRequestEvent(
    Long projectId, String sagaId, String filepath, String content, Long userId) {}
