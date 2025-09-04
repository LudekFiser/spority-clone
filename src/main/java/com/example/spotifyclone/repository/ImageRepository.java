package com.example.spotifyclone.repository;

import com.example.spotifyclone.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;




public interface ImageRepository extends JpaRepository<Image, Long> {
}
