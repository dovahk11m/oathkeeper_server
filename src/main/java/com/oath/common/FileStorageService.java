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

/*
[FileStorageService 사용법]

1. 새로운 파일 저장 경로 추가 방법:
  - `application-local.yml` 파일에 새로운 하위 디렉토리 경로를 추가합니다.
    예시) 프로필 이미지 경로 추가
    upload:
      root-dir: ./uploads/
      chat-dir: chat-images/
      chat-file-dir: chat-file/
      profile-dir: profile-images/  # <-- 프로필 이미지 저장 경로 추가

  - `UploadConfig.java` 클래스에 해당 경로를 바인딩할 필드를 추가합니다.
    @Configuration
    @ConfigurationProperties(prefix = "upload")
    @Data
    public class UploadConfig {
        private String rootDir;
        private String chatDir;
        private String chatFileDir;
        private String profileDir; // <-- 추가된 필드
    }

2. 컨트롤러 또는 서비스에서 파일 저장 방법:
  - `FileStorageService`와 `UploadConfig`를 주입받습니다.
  - `store()` 메서드를 호출하여 파일을 저장하고, 반환된 상대 경로를 DB에 저장하거나 클라이언트에 응답합니다.

  예시) MemberController에서 프로필 이미지 업로드

  @RestController
  @RequestMapping("/api/members")
  @RequiredArgsConstructor
  public class MemberController {

      private final FileStorageService fileStorageService;
      private final UploadConfig uploadConfig;
      private final MemberService memberService;

      @PostMapping("/{memberId}/profile-image")
      public ResponseEntity<?> uploadProfileImage(
          @PathVariable Long memberId,
          @RequestParam("file") MultipartFile file
      ) {
          // 1. `uploadConfig`에서 "profile-images/" 경로를 가져옵니다.
          String subDir = uploadConfig.getProfileDir();

          // 2. `fileStorageService.store()`를 호출하여 파일을 저장하고, 저장된 상대 경로를 받습니다.
          //    (예: "profile-images/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx.jpg")
          String storedFilePath = fileStorageService.store(file, subDir);

          // 3. (필요 시) 서비스 로직을 호출하여 DB에 파일 경로를 업데이트합니다.
          memberService.updateProfileImage(memberId, storedFilePath);

          // 4. 클라이언트에 성공 응답과 함께 파일 경로를 반환합니다.
          return ResponseEntity.ok(Map.of("filePath", storedFilePath));
      }
  }
*/
