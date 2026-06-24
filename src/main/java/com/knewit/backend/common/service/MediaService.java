package com.knewit.backend.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.knewit.backend.common.dto.MediaUploadResponse;
import com.knewit.backend.common.exception.KnewitException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

@Service
public class MediaService {

    @Autowired private
    Cloudinary cloudinary;

    public MediaUploadResponse uploadFile(
            MultipartFile file,
            String folder
    ) {

        try {

            String contentType = file.getContentType();

            Map<?, ?> result;

            if (contentType != null &&
                    contentType.startsWith("video")) {

                File tempFile = File.createTempFile(
                        "video-upload-",
                        ".tmp"
                );

                file.transferTo(tempFile);

                try {

                    result = cloudinary.uploader().uploadLarge(
                            tempFile,
                            ObjectUtils.asMap(
                                    "folder", folder,
                                    "resource_type", "video"
                            )
                    );

                } finally {

                    tempFile.delete();
                }

            } else {

                result = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", folder,
                                "resource_type", "image"
                        )
                );
            }

            return MediaUploadResponse.builder()
                    .url(
                            result.get("secure_url")
                                    .toString()
                    )
                    .publicId(
                            result.get("public_id")
                                    .toString()
                    )
                    .build();

        } catch (Exception e) {

            throw new KnewitException(
                    "FILE_UPLOAD_FAILED",
                    "Failed to upload file",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }
}