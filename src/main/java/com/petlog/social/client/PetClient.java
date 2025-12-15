package com.petlog.social.client;

import com.petlog.social.dto.client.PetClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "pet-service", url = "${external.pet-service.url}")
public interface PetClient {
    // [수정] 펫 이름만 가져오는 API가 없으므로, 펫 전체 정보를 조회하는 API로 변경
    @GetMapping("/api/pets/{petId}")
    PetClientResponse getPet(@PathVariable("petId") Long petId);
}