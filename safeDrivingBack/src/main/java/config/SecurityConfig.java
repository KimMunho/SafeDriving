package hello.safedrivingback.config;

import hello.safedrivingback.jwt.JwtFilter;
import hello.safedrivingback.jwt.JwtTokenBlackList;
import hello.safedrivingback.jwt.JwtUtil;
import hello.safedrivingback.jwt.LoginFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final JwtTokenBlackList jwtTokenBlackList;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                //.formLogin((auth) -> auth.disable())
                .httpBasic((auth) -> auth.disable())
//                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())) // CSRF 토큰을 쿠키에 저장

                // 나중에 다시 설정해야됨
//                .authorizeHttpRequests(authorize -> authorize
//                        .requestMatchers("/", "/member/login", "/member/join").permitAll()
//                        .anyRequest().authenticated() // 인증 필요 URL
//                )

                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll())  // 개발모드에서는 모든 요청 허용 (추후 다시 설정)

                .logout(logout -> logout
                        .permitAll() // 모든 사용자 로그아웃 허용
                );

        http    // JwtFilter 추가
                .addFilterBefore(new JwtFilter(jwtUtil, jwtTokenBlackList), LoginFilter.class);

        // 잠시 주석처리 ( 로그인 경로의 웹 페이지 만들기 위해 )
        //LogFilter 추가
        //http
        //      .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // Jwt 토큰은 세션을 stateless 상태로 관리하기 때문
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build(); // 보안 필터 체인 빌드
    }
}