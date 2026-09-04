package org.example.expert.domain.todo.dto.request;

public record TodoSearchResponse(
        String title,
        Long managerCount,
        Long commentCount
) {}