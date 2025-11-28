package com.example.petlog.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Pet 서비스의 이름이 'pet-service'라고 가정
@FeignClient(name = "pet-service", url = "${external.pet-service.url}")
public interface PetClient {
    @GetMapping("/api/pets/{petId}/name")
    String getPetName(@PathVariable("petId") Long petId);
}