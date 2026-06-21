package com.knewit.backend.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.knewit.backend.common.dto.MediaUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final Cloudinary cloudinary;

    public MediaUploadResponse uploadFile(
            MultipartFile file
    ) {

        try {

            Map<?, ?> result =
                    cloudinary.uploader().upload(
                            file.getBytes(),
                            ObjectUtils.asMap(
                                    "folder",
                                    "knewit/posts"
                            )
                    );

            return MediaUploadResponse.builder()
                    .url(result.get("secure_url").toString())
                    .publicId(result.get("public_id").toString())
                    .build();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to upload file",
                    e
            );
        }
    }
}