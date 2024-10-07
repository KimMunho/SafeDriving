package hello.safedrivingback.fileUpload;

import lombok.Data;

@Data
public class UploadFile {

    private String uploadFileName; //사용자가 업로드 한 파일이름
    private String storedFileName; //실제 저장되는 파일이름

    public UploadFile(String uploadFileName, String storedFileName) {
        this.uploadFileName = uploadFileName;
        this.storedFileName = storedFileName;
    }
}
