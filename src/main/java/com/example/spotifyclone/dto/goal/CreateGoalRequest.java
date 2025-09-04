package com.example.spotifyclone.dto.goal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateGoalRequest {

    @NotBlank(message = "Title is required!")
    private String title;

    @NotNull(message = "Idea reference is required!")
    private Long ideaId;
}
