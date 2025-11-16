package com.footbook.service.external.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.footbook.exception.ImageUploadException;
import com.footbook.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageUploadService {
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"};

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String folder) {
        validateFile(file);

        try {
            String publicId = folder + "/" + UUID.randomUUID().toString();

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", folder,
                    "resource_type", "image",
                    "quality", "auto:good"
                )
            );

            String imageUrl = (String) uploadResult.get("secure_url");
            log.info("Image uploaded successfully to Cloudinary: {}", imageUrl);
            return imageUrl;
        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new ImageUploadException(ErrorMessages.UPLOAD_FAILED, e);
        }
    }

    public void deleteImage(String imageUrl) {
        try {
            String publicId = extractPublicId(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Image deleted successfully from Cloudinary: {}", publicId);
            }
        } catch (IOException e) {
            log.error("Failed to delete image from Cloudinary", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(ErrorMessages.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        boolean isValidType = false;
        for (String allowedType : ALLOWED_TYPES) {
            if (allowedType.equals(contentType)) {
                isValidType = true;
                break;
            }
        }

        if (!isValidType) {
            throw new IllegalArgumentException(ErrorMessages.INVALID_FILE_TYPE);
        }
    }

    private String extractPublicId(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return null;
        }

        try {
            String[] parts = imageUrl.split("/upload/");
            if (parts.length == 2) {
                String path = parts[1];

                int versionEnd = path.indexOf('/');
                if (versionEnd > 0) {
                    path = path.substring(versionEnd + 1);
                }

                int extensionStart = path.lastIndexOf('.');
                if (extensionStart > 0) {
                    path = path.substring(0, extensionStart);
                }

                return path;
            }
        } catch (Exception e) {
            log.warn("Failed to extract public_id from URL: {}", imageUrl, e);
        }

        return null;
    }
}
