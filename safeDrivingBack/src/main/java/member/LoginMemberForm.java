package hello.safedrivingback.member;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

//LogFilter 로 인증 수행하기 때문에 사용 X
@Data
public class LoginMemberForm {

    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "password is required")
    private String password;

}
