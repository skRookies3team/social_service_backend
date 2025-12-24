package com.petlog.social.controller;

import com.petlog.social.service.SocialSafetyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Social Safety", description = "신고 및 차단 API")
public class ReportBlockController {

    private final SocialSafetyService safetyService;

    @PostMapping("/reports")
    @Operation(summary = "신고하기")
    public ResponseEntity<Void> report(
            @RequestParam Long reporterId,
            @RequestParam Long targetId,
            @RequestParam String type, // FEED, USER, COMMENT
            @RequestParam String reason
    ) {
        safetyService.report(reporterId, targetId, type, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/blocks")
    @Operation(summary = "유저 차단하기")
    public ResponseEntity<Void> blockUser(
            @RequestParam Long blockerId,
            @RequestParam Long blockedId
    ) {
        safetyService.blockUser(blockerId, blockedId);
        return ResponseEntity.ok().build();
    }
}