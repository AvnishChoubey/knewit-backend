package com.knewit.backend.subreddit.repository;


import com.knewit.backend.subreddit.entity.SubredditMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubredditMemberRepository extends JpaRepository<SubredditMember, SubredditMember.SubredditMemberId> {
    Optional<SubredditMember> findBySubredditIdAndUserId(Long subredditId, Long userId);
    boolean existsBySubredditIdAndUserIdAndMemberState(Long subredditId, Long userId, Long memberState);
}