package hello.safedrivingback.member;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "username is required.")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "password is required.")
    private String password;

    private String email;


    public Member(){

    }

    public Member(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}
