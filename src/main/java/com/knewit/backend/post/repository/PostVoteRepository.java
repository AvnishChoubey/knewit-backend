package com.knewit.backend.post.repository;

import com.knewit.backend.common.enums.VoteType;
import com.knewit.backend.post.entity.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostVoteRepository
        extends JpaRepository<PostVote, Long> {

    Optional<PostVote> findByPost_IdAndUser_Id(
            Long postId,
            Long userId
    );

    long countByPost_IdAndVoteType(
            Long postId,
            VoteType voteType
    );

}