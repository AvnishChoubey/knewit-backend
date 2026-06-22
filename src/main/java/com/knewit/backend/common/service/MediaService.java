package com.knewit.backend.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.knewit.backend.common.dto.MediaUploadResponse;
import com.knewit.backend.common.exception.KnewitException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final Cloudinary cloudinary;

    public MediaUploadResponse uploadFile(
            MultipartFile file,
            String folder
    ) {

        long totalStart = System.currentTimeMillis();

        try {

            String contentType = file.getContentType();

            Map<?, ?> result;

            if (contentType != null &&
                    contentType.startsWith("video")) {

                System.out.println("Detected Media Type : VIDEO");

                long transferStart = System.currentTimeMillis();

                File tempFile = File.createTempFile(
                        "video-upload-",
                        ".tmp"
                );

                file.transferTo(tempFile);





                try {

                    result = cloudinary.uploader().upload(
                            tempFile,
                            ObjectUtils.asMap(
                                    "folder", folder,
                                    "resource_type", "video"
                            )
                    );
                } finally {

                    boolean deleted = tempFile.delete();

                }



            } else {

                System.out.println("Detected Media Type : IMAGE");


                result = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", folder,
                                "resource_type", "image"
                        )
                );

            }

            System.out.println(
                    "Cloudinary URL : "
                            + result.get("secure_url")
            );


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


            e.printStackTrace();

            throw new KnewitException(
                    "FILE_UPLOAD_FAILED",
                    "Failed to upload file",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }
}