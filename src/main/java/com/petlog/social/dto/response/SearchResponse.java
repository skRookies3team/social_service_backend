package com.petlog.social.dto.response;

import com.petlog.social.dto.client.UserClientResponse;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;
import java.util.List;

@Getter
@Builder
public class SearchResponse {
    private List<UserClientResponse> users; // 유저 검색 결과
    private Slice<FeedResponse.GetFeedDto> feeds; // 피드(해시태그) 검색 결과

    public static SearchResponse of(List<UserClientResponse> users, Slice<FeedResponse.GetFeedDto> feeds) {
        return SearchResponse.builder()
                .users(users)
                .feeds(feeds)
                .build();
    }
}