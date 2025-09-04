package com.example.spotifyclone.utils.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.spotifyclone.dto.auth.RegisterResponse;
import com.example.spotifyclone.dto.ideas.CreateIdeaResponse;
import com.example.spotifyclone.dto.image.UploadedImageDto;
import com.example.spotifyclone.entity.Idea;
import com.example.spotifyclone.entity.Image;
import com.example.spotifyclone.entity.User;
import com.example.spotifyclone.enums.ImageType;
import com.example.spotifyclone.mapper.IdeaMapper;
import com.example.spotifyclone.mapper.UserMapper;
import com.example.spotifyclone.repository.IdeaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {


    private final Cloudinary cloudinary;
    private final IdeaRepository ideaRepository;
    private final IdeaMapper ideaMapper;

    public UploadedImageDto uploadImage(MultipartFile file, String folderName) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folderName,       // složka dynamicky
                            "resource_type", "image"
                    )
            );

            String url = uploadResult.get("secure_url").toString();
            String publicId = uploadResult.get("public_id").toString();

            return new UploadedImageDto(url, publicId);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }


    public Image buildProfileImage(UploadedImageDto dto, User currentUser) {

        return Image.builder()
                .imageUrl(dto.getUrl())
                .publicId(dto.getPublicId())
                .ord(0)
                .type(ImageType.PROFILE)
                .user(currentUser)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void deleteImageByPublicId(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image with public_id: " + publicId, e);
        }
    }

    public List<UploadedImageDto> uploadAllIdeaImages(List<MultipartFile> files, String filename) {
        List<UploadedImageDto> uploaded = new ArrayList<>();
        if (files == null || files.isEmpty()) return uploaded;

        for (MultipartFile f : files) {
            if (f != null && !f.isEmpty()) {
                try {
                    uploaded.add(uploadImage(f, filename));
                } catch (Exception e) {
                    log.error("Failed to upload image to Cloudinary", e);
                    throw new RuntimeException("Image upload failed: " + e.getMessage(), e);
                }
            }
        }
        return uploaded;
    }
}
