package com.knewit.backend.search.controller;

import com.knewit.backend.common.exception.KnewitException;
import com.knewit.backend.search.dto.SearchResponseDto;
import com.knewit.backend.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    @Autowired private SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResponseDto> search(@RequestParam(value = "q", required = false) String q) {

        if (q == null || q.isBlank()) {
            throw new KnewitException("EMPTY_QUERY", "Query string is empty", HttpStatus.BAD_REQUEST);
        }
        
        SearchResponseDto response = searchService.search(q.trim());
        return ResponseEntity.ok(response);
    }
}
