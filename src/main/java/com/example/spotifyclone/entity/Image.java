package com.example.spotifyclone.entity;

import com.example.spotifyclone.enums.ImageType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    private String publicId;

    private Integer ord;

    @Enumerated(EnumType.STRING)
    private ImageType type;

    private LocalDateTime createdAt = LocalDateTime.now();


    // Pokud budeš mít i profilovku:
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    /*@OneToOne(mappedBy = "avatarId")
    private User user;*/
}


