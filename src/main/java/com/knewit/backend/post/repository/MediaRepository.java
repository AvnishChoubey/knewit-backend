package com.knewit.backend.post.repository;

import com.mountblue.knewit.post.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRepository
        extends JpaRepository<Media, Long> {
}