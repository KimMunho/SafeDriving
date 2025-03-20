package hello.safedrivingback.member;

import hello.safedrivingback.exception.JoinException;
import hello.safedrivingback.exception.LoginException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void join(Member member) {
        String hashedPassword = passwordEncoder.encode(member.getPassword());
        member.setPassword(hashedPassword);
        memberRepository.save(member);
    }

    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    public void joinAuthenticate(Member member) {
        String username = member.getUsername();
        String email = member.getEmail();

        if(memberRepository.findByUsername(username).isPresent()) {
            throw new JoinException("이미 가입된 아이디입니다.");
        }

        if(memberRepository.findByEmail(email).isPresent()) {
            throw new JoinException("이미 가입된 이메일입니다.");
        }
    }

    public void loginAuthenticate(String username, String password) {
        Optional<Member> findMember = memberRepository.findByUsername(username);
        String hashedPassword = findMember.get().getPassword();

        if (findMember.isEmpty()) {
            throw new LoginException("아이디가 존재하지 않습니다.");
        }

        if(!passwordEncoder.matches(password, hashedPassword)){
            throw new LoginException("비밀번호가 틀립니다");
        }
    }
}
