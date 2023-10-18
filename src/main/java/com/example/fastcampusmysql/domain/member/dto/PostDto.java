package com.example.fastcampusmysql.domain.member.dto;

import java.time.LocalDateTime;

public record PostDto(
        Long id,
        String content,
        LocalDateTime createdTime,
        Long likeCount
) {
}
