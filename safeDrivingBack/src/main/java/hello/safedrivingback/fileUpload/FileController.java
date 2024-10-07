package hello.safedrivingback.fileUpload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {

    private final FileService fileService;
    private final FileRepository fileRepository;

    @GetMapping("/upload")
    public String upload() {
        return "upload-form";
    }

    @PostMapping("/upload")
    public String uploadPost(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            FileEntity fileEntity = fileService.storeFile(file);
            redirectAttributes.addAttribute("fileId", fileEntity.getId());
            return "redirect:/file/" + fileEntity.getId();  // 파일조회 페이지로 리다이렉트
        } catch (IOException e) {
            log.error(e.getMessage());
            return "redirect:/upload";
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> download(@PathVariable("fileId") Long fileId) {

        Optional<FileEntity> findFileEntity = fileRepository.findById(fileId);


        if (findFileEntity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        log.info("findFileName: {}", findFileEntity.get().getFileName()); //test

        try {
            File file = fileService.downloadFile(findFileEntity.get().getFileName());
            byte[] data = Files.readAllBytes(file.toPath());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
            httpHeaders.setLocation(URI.create("/file/" + fileId)); // 파일조회 페이지로 리다이렉트
            return new ResponseEntity<>(data, httpHeaders, HttpStatus.OK);
        } catch (IOException e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}