package hello.safedrivingback.fileUpload;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    @Value("${file.dir}")
    private String fileDir;
    private final FileRepository fileRepository;

    @PostConstruct
    public void init() {
        fileDir = relativePathToAbsolutePath(fileDir);
    }

    public String getFullPath(String fileName) {
        return fileDir + "\\" + fileName;
    }

    //파일저장
    @Transactional
    public FileEntity storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            log.warn("요청받은 파일이 없습니다");
            return null;
        }

        String fileName = getStoredFileName(Objects.requireNonNull(file.getOriginalFilename()));
        file.transferTo(new File(getFullPath(fileName))); // 파일 경로에 저장
        log.info("파일경로에 저장완료 : {}", getFullPath(fileName));

        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileName(fileName);
        fileRepository.save(fileEntity); // db에 저장
        log.info("db에 파일 저장 완료 " + " file Name: {}", fileEntity.getFileName());

        return fileEntity;
    }

    //파일 다운로드
    public File downloadFile(Long fileId) {
        Optional<FileEntity> findFileEntity = fileRepository.findById(fileId);
        if (findFileEntity.isPresent()) {
            String fileName = findFileEntity.get().getFileName();
            return new File(getFullPath(fileName));
        }

        return null;
    }

    // db에 저장할 fileName 만들기
    private String getStoredFileName(String fileName) {
        String uuid = UUID.randomUUID().toString();

        assert fileName != null;
        int pos = fileName.lastIndexOf(".");
        String ext = fileName.substring(pos);

        return uuid + ext;
    }

    //상대경로에서 절대경로 변환
    private String relativePathToAbsolutePath(String fileDir) {
        assert fileDir != null;
        return Paths.get(fileDir).toAbsolutePath().normalize().toString();
    }
}