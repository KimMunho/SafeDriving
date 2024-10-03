package hello.safedrivingback.jwt;

import hello.safedrivingback.member.CustomUserDetails;
import hello.safedrivingback.member.Member;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            log.info("Jwt token 이 존재하지 않는다.");
            filterChain.doFilter(request, response);
            return;
        }

        token = token.substring(7);


        boolean tokenExpired = jwtUtil.isTokenExpired(token);

        if (tokenExpired) {    // 토큰이 만료됐으면
            log.info("Jwt token expired");
            filterChain.doFilter(request, response);
            return;
        }

        log.info("authorization start");

        String username = jwtUtil.extractUsername(token);

        Member member = new Member(username, "tempPassword", "tempEmail");
        CustomUserDetails customUserDetails = new CustomUserDetails(member);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);  // 요청한 고객에 대한 세션을 잠깐 열어줌 (세션에 사용자 등록)
        filterChain.doFilter(request, response);
    }
}
