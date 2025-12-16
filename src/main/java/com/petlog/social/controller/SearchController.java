package com.petlog.social.controller;

import com.petlog.social.dto.response.FeedResponse;
import com.petlog.social.dto.response.SearchResponse;
import com.petlog.social.service.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "검색 및 탐색 API")
public class SearchController {

    private final FeedService feedService;

    @GetMapping
    @Operation(summary = "통합 검색", description = "검색어(query)로 유저(소셜ID)와 해시태그 피드를 동시에 조회합니다.")
    public ResponseEntity<SearchResponse> searchAll(
            @RequestParam String query,
            @RequestParam(required = false) Long viewerId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(feedService.searchAll(query, viewerId, pageable));
    }

    @GetMapping("/trending")
    @Operation(summary = "인기 게시물 조회", description = "최근 7일 내 좋아요가 많은 순서대로 피드를 조회합니다.")
    public ResponseEntity<Slice<FeedResponse.GetFeedDto>> getTrending(
            @RequestParam(required = false) Long viewerId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(feedService.getTrendingFeeds(viewerId, pageable));
    }
}