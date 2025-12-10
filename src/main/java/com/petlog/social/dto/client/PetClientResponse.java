package com.petlog.social.dto.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PetClientResponse {
    private Long petId;
    private String petName;
    private String breed;
    private String profileImage; // 프로필 이미지 필드 추가
}