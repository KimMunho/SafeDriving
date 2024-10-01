package hello.safedrivingback.member;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginMemberForm {

    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "password is required")
    private String password;

}
