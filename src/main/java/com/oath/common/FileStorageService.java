package com.oath.common;

import com.oath.common.config.UploadConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final UploadConfig uploadConfig;
    private Path rootLocation;

    @PostConstruct
    public void init() {
        try {
            rootLocation = Paths.get(uploadConfig.getRootDir());
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    /**
     * 파일을 저장하고 저장된 파일의 경로를 반환합니다.
     *
     * @param file 저장할 파일
     * @param subDir 저장할 하위 디렉토리 (예: "chat-images")
     * @return 저장된 파일의 상대 경로
     */
    public String store(MultipartFile file, String subDir) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty file.");
        }

        try {
            Path destinationDirectory = rootLocation.resolve(subDir);
            Files.createDirectories(destinationDirectory);

            // 파일명 중복을 피하기 위해 UUID 사용
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String storedFilename = UUID.randomUUID().toString() + extension;

            Path destinationFile = destinationDirectory.resolve(Paths.get(storedFilename)).normalize().toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // 데이터베이스나 클라이언트에 전달할 상대 경로 반환
            return Paths.get(subDir).resolve(storedFilename).toString().replace("\\", "/");

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }
}
