package com.petlog.social.controller;

import com.petlog.social.dto.response.SearchResponse;
import com.petlog.social.dto.response.FeedResponse;
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
@Tag(name = "Social Search", description = "검색 및 탐색 API")
public class SearchController {

    private final FeedService feedService;

    @GetMapping
    @Operation(summary = "통합 검색", description = "유저(닉네임/소셜ID) 및 해시태그 검색")
    public ResponseEntity<SearchResponse> searchAll(
            @RequestParam String query,
            @RequestParam(required = false) Long viewerId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(feedService.searchAll(query, viewerId, pageable));
    }

    @GetMapping("/trending")
    @Operation(summary = "인기 게시물 조회", description = "최근 7일간 좋아요 순 랭킹")
    public ResponseEntity<Slice<FeedResponse.GetFeedDto>> getTrendingFeeds(
            @RequestParam(required = false) Long viewerId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(feedService.getTrendingFeeds(viewerId, pageable));
    }
}