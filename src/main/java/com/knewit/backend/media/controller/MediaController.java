package com.knewit.backend.media.controller;

import com.knewit.backend.common.dto.MediaUploadResponse;
import com.knewit.backend.common.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload")
    public ResponseEntity<MediaUploadResponse> uploadFile(
            @RequestParam("file")
            MultipartFile file,
            @RequestParam(
                    defaultValue = "knewit/posts"
            )
            String folder
    ) {

        return ResponseEntity.ok(
                mediaService.uploadFile(
                        file,
                        folder
                )
        );
    }
}