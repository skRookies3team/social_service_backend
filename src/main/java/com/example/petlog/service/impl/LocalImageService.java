package com.example.petlog.service.impl;

import com.example.petlog.exception.BusinessException;
import com.example.petlog.exception.ErrorCode;
import com.example.petlog.service.ImageService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Primary // ✅ 현재 활성화된 구현체
public class LocalImageService implements ImageService {

    // 프로젝트 루트 경로 아래 uploads 폴더 사용
    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // 폴더가 없으면 생성
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 파일명 중복 방지 (UUID)
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.lastIndexOf(".") != -1) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String savedFilename = UUID.randomUUID().toString() + extension;

            // 파일 저장
            Path path = Paths.get(uploadDir + savedFilename);
            Files.copy(file.getInputStream(), path);

            return savedFilename;

        } catch (IOException e) {
            // ErrorCode.FILE_UPLOAD_FAILED 또는 INTERNAL_SERVER_ERROR 사용
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "이미지 저장 실패");
        }
    }

    @Override
    public String getImageUrl(String filename) {
        // 로컬 정적 리소스 경로 반환 (WebConfig 설정 필요)
        return "/images/" + filename;
    }
}