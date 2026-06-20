package com.knewit.backend.comment.repository;

import com.knewit.backend.comment.entity.Comment;
import com.knewit.backend.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository
        extends JpaRepository<Comment, Long> {

    List<Comment> findByPost(Post post);

    List<Comment> findByParentComment(Comment parentComment);
}