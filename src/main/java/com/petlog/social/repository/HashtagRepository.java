package com.petlog.social.repository;

import com.petlog.social.dto.response.SearchHashtagResponse;
import com.petlog.social.entity.Hashtag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Optional<Hashtag> findByName(String name);

    // [수정] LIKE 문법 변경: %:query% -> CONCAT('%', :query, '%')
    @Query("SELECT new com.petlog.social.dto.response.SearchHashtagResponse(h.id, h.name, COUNT(fh)) " +
            "FROM Hashtag h " +
            "LEFT JOIN FeedHashtag fh ON h.id = fh.hashtag.id " +
            "WHERE h.name LIKE CONCAT('%', :query, '%') " +
            "GROUP BY h.id, h.name " +
            "ORDER BY COUNT(fh) DESC")
    List<SearchHashtagResponse> searchHashtagsByName(@Param("query") String query, Pageable pageable);
}