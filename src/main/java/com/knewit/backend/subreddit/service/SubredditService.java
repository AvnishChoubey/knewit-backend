
package com.knewit.backend.subreddit.service;

import com.knewit.backend.subreddit.entity.Subreddit;
import com.knewit.backend.subreddit.entity.SubredditMember;
import com.knewit.backend.subreddit.entity.SubredditModerator;
import com.knewit.backend.subreddit.entity.User;
import com.knewit.backend.subreddit.repository.SubredditMemberRepository;
import com.knewit.backend.subreddit.repository.SubredditModeratorRepository;
import com.knewit.backend.subreddit.repository.SubredditRepository;
import com.knewit.backend.subreddit.repository.UserRepository;
import com.knewit.backend.subreddit.request.CreateSubredditRequest;
import com.knewit.backend.subreddit.response.SubredditResponse;
import com.knewit.backend.subreddit.transformer.SubredditTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubredditService {

    private final SubredditRepository subredditRepository;
    private final UserRepository userRepository;
    private final SubredditMemberRepository subredditMemberRepository;
    private final SubredditModeratorRepository subredditModeratorRepository;

    public SubredditResponse createSubreddit(
            Long userId,
            CreateSubredditRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Subreddit subreddit = Subreddit.builder()
                .name(request.getName())
                .description(request.getDescription())
                .bannerUrl(request.getBannerUrl())
                .iconUrl(request.getIconUrl())
                .createdBy(user)
                .visibility(request.getVisibility())
                .topic(request.getTopic())
                .build();

        subreddit = subredditRepository.save(subreddit);

        SubredditMember member = new SubredditMember();
        member.setUser(user);
        member.setSubreddit(subreddit);

        subredditMemberRepository.save(member);

        SubredditModerator moderator = new SubredditModerator();
        moderator.setUser(user);
        moderator.setSubreddit(subreddit);
        moderator.setRole(
                SubredditModerator.ModeratorRole.OWNER
        );

        subredditModeratorRepository.save(moderator);

        return SubredditTransformer.toResponse(
                subreddit,
                subredditMemberRepository.countBySubreddit(subreddit)
        );
    }

    public SubredditResponse getSubreddit(
            Long subredditId
    ) {

        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() ->
                        new RuntimeException("Subreddit not found"));

        return SubredditTransformer.toResponse(
                subreddit,
                subredditMemberRepository.countBySubreddit(subreddit)
        );
    }

    public List<SubredditResponse> getAllSubreddits() {

        return subredditRepository.findAll()
                .stream()
                .map(subreddit ->
                        SubredditTransformer.toResponse(
                                subreddit,
                                subredditMemberRepository
                                        .countBySubreddit(subreddit)
                        )
                )
                .toList();
    }

    public void joinSubreddit(
            Long userId,
            Long subredditId
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() ->
                        new RuntimeException("Subreddit not found"));

        boolean alreadyJoined =
                subredditMemberRepository
                        .existsByUserAndSubreddit(
                                user,
                                subreddit
                        );

        if (alreadyJoined) {
            throw new RuntimeException(
                    "User already in subreddit"
            );
        }

        SubredditMember member = new SubredditMember();

        member.setUser(user);
        member.setSubreddit(subreddit);

        subredditMemberRepository.save(member);
    }

    public void leaveSubreddit(
            Long userId,
            Long subredditId
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() ->
                        new RuntimeException("Subreddit not found"));

        SubredditMember member =
                subredditMemberRepository
                        .findByUserAndSubreddit(
                                user,
                                subreddit
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Membership not found"
                                ));

        subredditMemberRepository.delete(member);
    }
}