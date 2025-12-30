package com.petlog.social.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchHashtagResponse {
    private Long hashtagId;
    private String name;
    private Long count; // 해당 해시태그가 사용된 게시물 수
}