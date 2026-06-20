package com.knewit.backend.subreddit.controller;

import com.knewit.backend.subreddit.request.CreateSubredditRequest;
import com.knewit.backend.subreddit.response.SubredditResponse;
import com.knewit.backend.subreddit.service.SubredditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subreddits")
@RequiredArgsConstructor
public class SubredditController {

    private final SubredditService subredditService;

    @PostMapping
    public SubredditResponse create(
            @RequestParam Long userId,
            @RequestBody CreateSubredditRequest request
    ){
        return subredditService.createSubreddit(
                userId,
                request
        );
    }

    @GetMapping("/{id}")
    public SubredditResponse get(
            @PathVariable Long id
    ){
        return subredditService.getSubreddit(id);
    }

    @GetMapping
    public List<SubredditResponse> getAll(){
        return subredditService.getAllSubreddits();
    }

    @PostMapping("/{id}/join")
    public void join(
            @RequestParam Long userId,
            @PathVariable Long id
    ){
        subredditService.joinSubreddit(
                userId,
                id
        );
    }

    @DeleteMapping("/{id}/leave")
    public void leave(
            @RequestParam Long userId,
            @PathVariable Long id
    ){
        subredditService.leaveSubreddit(
                userId,
                id
        );
    }

}
