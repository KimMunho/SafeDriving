package hello.safedrivingback.temp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@Slf4j
@Controller
@RequestMapping("/temp")
public class UploadController {

    @GetMapping("/upload")
    public String uploadFrom() {
        return "temp/upload";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        try {
            File uploadedFile = new File("C:/Users/djgns/바탕 화면/school/project/SafeDriving/safeDrivingBack/src/main/java/hello/safedrivingback/temp/uploadedFile/" + file.getOriginalFilename());    // 본인 환경에 맞춰서 수정해야됨
            file.transferTo(uploadedFile);

            //String result = callPythonModel(uploadedFile);        //임시 주석

            //test
            String result = "20";

            return "redirect:/temp/result?fault=" + result;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("파일 업로드에 문제가 발생했음");
            return "/temp/upload";
        }
    }

    private String callPythonModel(File file) {
        try {
            // 파이썬 스크립트를 실행하여 AI 모델에 파일을 넘기고 결과 받기
            ProcessBuilder processBuilder = new ProcessBuilder("python3", "path/to/your/python_script.py", file.getAbsolutePath()); //본인 환경에 맞춰서 수정해야됨
            Process process = processBuilder.start();
            process.waitFor();

            // 파이썬 스크립트의 출력 결과를 받음
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            return reader.readLine(); // 과실 비율 반환

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("AI 모델 실행 시 문제 발생");
            return "/temp/upload"; // 오류 메시지
        }
    }
}
