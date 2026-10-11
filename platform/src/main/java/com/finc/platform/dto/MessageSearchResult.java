package com.finc.platform.dto;

import java.time.LocalDateTime;

public record MessageSearchResult(
        Long id,
        Long channelId,
        String channelName,
        String sender,
        String content,
        LocalDateTime timestamp) {
}