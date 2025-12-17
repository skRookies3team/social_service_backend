package com.petlog.social.dto.response;

import com.petlog.social.dto.client.UserClientResponse;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;
import java.util.List;

@Getter
@Builder
public class SearchResponse {
    private List<UserClientResponse> users;
    private Slice<FeedResponse.GetFeedDto> feeds;

    public static SearchResponse of(List<UserClientResponse> users, Slice<FeedResponse.GetFeedDto> feeds) {
        return SearchResponse.builder().users(users).feeds(feeds).build();
    }
}