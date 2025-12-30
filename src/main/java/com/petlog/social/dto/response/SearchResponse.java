package com.petlog.social.dto.response;

import com.petlog.social.dto.client.UserClientResponse;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;
import java.util.List;

@Getter
@Builder
public class SearchResponse {
    private List<UserClientResponse> users;       // 사용자 검색 결과
    private Slice<FeedResponse.GetFeedDto> feeds; // 피드 검색 결과

    // [추가] 해시태그 검색 결과
    private List<SearchHashtagResponse> hashtags;

    // 생성 메서드 수정 (사용하는 곳도 수정 필요)
    public static SearchResponse of(
            List<UserClientResponse> users,
            Slice<FeedResponse.GetFeedDto> feeds,
            List<SearchHashtagResponse> hashtags
    ) {
        return SearchResponse.builder()
                .users(users)
                .feeds(feeds)
                .hashtags(hashtags)
                .build();
    }
}