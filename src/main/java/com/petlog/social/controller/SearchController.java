package com.petlog.social.controller;

import com.petlog.social.dto.response.FeedResponse;
import com.petlog.social.dto.response.SearchHashtagResponse;
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

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Social Search", description = "검색 및 인기 게시물 API")
public class SearchController {

    private final FeedService feedService;

    @GetMapping
    // [수정] 설명 변경: 유저(social) 로 검색
    @Operation(summary = "통합 검색", description = "유저(social) 또는 해시태그(#)로 검색합니다.")
    public ResponseEntity<SearchResponse> searchAll(
            @RequestParam String query,
            @RequestParam(required = false) Long viewerId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(feedService.searchAll(query, viewerId, pageable));
    }

    @GetMapping("/hashtags")
    @Operation(summary = "해시태그 검색", description = "해시태그 이름으로 검색하여 태그 목록과 사용 횟수를 반환합니다.")
    public ResponseEntity<List<SearchHashtagResponse>> searchHashtags(@RequestParam String query) {
        return ResponseEntity.ok(feedService.searchHashtags(query));
    }
    @GetMapping("/trending")
    @Operation(summary = "인기 게시물 조회", description = "최근 7일간 좋아요를 많이 받은 순서대로 조회합니다.")
    public ResponseEntity<Slice<FeedResponse.GetFeedDto>> getTrendingFeeds(
            @RequestParam(required = false) Long viewerId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(feedService.getTrendingFeeds(viewerId, pageable));
    }
}