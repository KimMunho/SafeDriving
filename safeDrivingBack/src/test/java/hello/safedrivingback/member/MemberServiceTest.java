package hello.safedrivingback.member;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@SpringBootTest
@Transactional  //test data rollback
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository);
    }

    @Test
    void join(){
        Member member = new Member("asdf", "asdf", "asdf@gmail.com");
        memberService.join(member);

        Optional<Member> findMember = memberService.findById(member.getId());

        Assertions.assertThat(findMember).isEqualTo(member);
    }
}