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
    private String breed; // 품종 등 필요하면 추가
}