package hello.safedrivingback.config;

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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;

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
//                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())) // CSRF 토큰을 쿠키에 저장
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/member/login", "/member/join").permitAll() // 접근 허용 URL
                        .anyRequest().authenticated() // 인증 필요 URL
                )

        // memberController 에서 로그인 URL 설정을 다 해줬기 때문에 필터(Spring security)에서 해줄필요 X
//                .formLogin(form -> form
//                        .loginPage("/login") // 커스텀 로그인 페이지
//                        .permitAll() // 모든 사용자 접근 허용
//                )
                .logout(logout -> logout
                        .permitAll() // 모든 사용자 로그아웃 허용
                );

        //LogFilter 추가
        http
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration)), UsernamePasswordAuthenticationFilter.class);

        // Jwt 토큰은 세션을 stateless 상태로 관리하기 때문
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build(); // 보안 필터 체인 빌드
    }
}

// postman으로 username password 요청했을때 해당 필터가 가로채는지 확인