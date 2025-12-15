package com.petlog.social.service.impl;

import com.petlog.social.service.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class S3ImageService implements ImageService {

    @Override
    public String upload(MultipartFile file) {
        // TODO: 추후 AWS S3 업로드 로직 구현
        // amazonS3.putObject(...);
        return null;
    }

    @Override
    public String getImageUrl(String filename) {
        // TODO: S3 URL 반환 로직 구현
        // return amazonS3.getUrl(bucket, filename).toString();
        // 예시:
        return "https://s3.ap-northeast-2.amazonaws.com/my-bucket/" + filename;
    }
}