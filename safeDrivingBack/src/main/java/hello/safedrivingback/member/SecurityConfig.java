package hello.safedrivingback.member;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig{

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (주의 필요)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/member/login", "/member/logout", "/member/join").permitAll() // 접근 허용 URL
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

        return http.build(); // 보안 필터 체인 빌드
    }
}
