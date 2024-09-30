package hello.safedrivingback.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Member join(Member member) {
        memberRepository.save(member);
        return member;
    }

    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }
}
