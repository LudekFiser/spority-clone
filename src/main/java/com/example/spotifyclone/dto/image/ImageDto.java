package com.example.spotifyclone.dto.image;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageDto {

    private String url;
    private String publicId;
    private Integer ord;
}
