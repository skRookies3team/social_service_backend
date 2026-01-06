package com.petlog.social.repository;

import com.petlog.social.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 1. [전체보기용] 특정 피드의 '최상위 댓글(부모가 없는 것)'만 모두 조회
    // 대댓글은 최상위 댓글의 children 필드를 통해 가져옵니다.
    List<Comment> findAllByFeedIdAndParentIsNullOrderByCreatedAtAsc(Long feedId);

    // 2. [미리보기용] 특정 피드의 '최상위 댓글' 중 최신 3개만 조회 (Top3)
    List<Comment> findTop3ByFeedIdAndParentIsNullOrderByCreatedAtDesc(Long feedId);


    // 3. 댓글 수 카운트 (답글 포함 전체 개수)
    Long countByFeedId(Long feedId);
}
