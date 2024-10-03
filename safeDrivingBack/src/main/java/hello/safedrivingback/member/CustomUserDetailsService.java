package hello.safedrivingback.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service    // LoginFilter 의 authenticationManager 가 사용자 인증을 하는데 사용하는 서비스 (지금은 Form login 방식만 국한)
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<Member> findMember = memberRepository.findByUsername(username);

        if (findMember.isPresent()) {

            return new CustomUserDetails(findMember.get());
        }

        return null;
    }
}
