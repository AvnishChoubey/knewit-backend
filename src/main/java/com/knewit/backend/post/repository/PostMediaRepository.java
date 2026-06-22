package com.knewit.backend.post.repository;

import com.knewit.backend.post.entity.PostMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostMediaRepository
        extends JpaRepository<PostMedia, Long> {

    List<PostMedia> findAllByPost_Id(Long postId);
}