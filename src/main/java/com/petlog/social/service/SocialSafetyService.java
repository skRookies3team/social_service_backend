package com.petlog.social.service;

import com.petlog.social.entity.Block;
import com.petlog.social.entity.Report;
import com.petlog.social.repository.BlockRepository;
import com.petlog.social.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SocialSafetyService {

    private final ReportRepository reportRepository;
    private final BlockRepository blockRepository;

    // 신고하기
    @Transactional
    public void report(Long reporterId, Long targetId, String type, String reason) {
        Report report = Report.builder()
                .reporterId(reporterId)
                .targetId(targetId)
                .type(type)
                .reason(reason)
                .build();
        reportRepository.save(report);
    }

    // 차단하기
    @Transactional
    public void blockUser(Long blockerId, Long blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new IllegalArgumentException("자기 자신은 차단할 수 없습니다.");
        }

        // 이미 차단했는지 확인
        if (blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            return;
        }

        Block block = Block.builder()
                .blockerId(blockerId)
                .blockedId(blockedId)
                .build();
        blockRepository.save(block);
    }

    // 내가 차단한 유저 ID 목록 가져오기 (피드 필터링용)
    @Transactional(readOnly = true)
    public List<Long> getBlockedUserIds(Long userId) {
        return blockRepository.findBlockedIdsByBlockerId(userId);
    }
}