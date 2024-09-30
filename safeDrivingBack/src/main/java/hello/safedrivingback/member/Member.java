package hello.safedrivingback.member;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private String email;

    public Member(){

    }

    public Member(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}
