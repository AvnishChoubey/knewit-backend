package com.knewit.backend.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMessageDto {

    @NotBlank(message = "Message cannot be empty")
    private String body;
}