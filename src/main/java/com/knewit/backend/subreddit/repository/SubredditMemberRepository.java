package com.knewit.backend.subreddit.repository;


import com.knewit.backend.subreddit.entity.SubredditMember;
import com.knewit.backend.subreddit.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubredditMemberRepository
        extends JpaRepository<SubredditMember, Long> {

    Optional<SubredditMember> findBySubreddit_IdAndUser_Id(
            Long subredditId,
            Long userId
    );

    Optional<SubredditMember> findBySubreddit_NameAndUser_Id(
            String subredditName,
            Long userId
    );


    boolean existsBySubreddit_IdAndUser_IdAndMemberStatus(
            Long subredditId,
            Long userId,
            MemberStatus memberStatus
    );

    Optional<SubredditMember> findBySubreddit_IdAndUser_Username(
            Long subredditId,
            String username
    );
}