package com.petlog.social.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    /**
     * 이미지 파일을 업로드하고, 저장된 파일명(또는 키)을 반환합니다.
     */
    String upload(MultipartFile file);

    /**
     * 저장된 파일명을 기반으로 외부에서 접근 가능한 URL을 반환합니다.
     */
    String getImageUrl(String filename);
}