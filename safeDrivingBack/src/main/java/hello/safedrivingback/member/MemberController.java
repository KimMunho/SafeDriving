package hello.safedrivingback.member;

import hello.safedrivingback.exception.JoinException;
import hello.safedrivingback.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
@Slf4j
public class MemberController {


    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("member", new LoginMemberForm());
        return "login";
    }

    // LogFilter 로 인증하기 때문에 사용 X
//    @PostMapping("/login")
//    public String loginProcess(@Validated @ModelAttribute("member") LoginMemberForm loginForm, BindingResult bindingResult, Model model) {
//
//        if(bindingResult.hasErrors()) {
//            model.addAttribute("error", bindingResult.getAllErrors());
//            model.addAttribute("member", loginForm);
//
//            bindingResult.getAllErrors().forEach(error-> {log.info("Login 유효성 검증 실패 : {}", error.getDefaultMessage());});
//            return "member/join";
//        }
//
//        String username = loginForm.getUsername();
//        String password = loginForm.getPassword();
//
//        try{
//            memberService.loginAuthenticate(username, password);
//            log.info("login success : {}", loginForm.getUsername());
//        }catch (LoginException e){
//            log.info("login failed: {}", e.getMessage());
//            model.addAttribute("error", e.getMessage());
//            return "login";
//        }
//
//        return "redirect:/";
//    }

    @PostMapping("/logout")
    public String logout(@RequestHeader("Authorization") String token){
        token = token.substring(7);
        jwtUtil.logout(token);
        log.info("logout success");
        return "redirect:/member/login";
    }

    @GetMapping("/join")
    public String join() {
        return "member/join";
    }

    @PostMapping("/join")
    public String joinProcess(@Validated @ModelAttribute Member member, BindingResult bindingResult, Model model) {

        if(bindingResult.hasErrors()) {
            model.addAttribute("error", bindingResult.getAllErrors());
            model.addAttribute("member", member);

            bindingResult.getAllErrors().forEach(error-> {log.info("Join 유효성 검증 실패 : {}", error.getDefaultMessage());});
            return "member/join";
        }

        try {
            // 회원가입 인증
            memberService.joinAuthenticate(member);

            // 회원가입 처리
            memberService.join(member);

            log.info("회원가입 성공: {}", member.getUsername());
            return "redirect:/member/login";
        } catch (JoinException e) {
            // 예외 발생 시 오류 메시지 추가
            model.addAttribute("error", e.getMessage());
            log.info("회원가입 실패: {}", e.getMessage());

            // 회원가입 페이지로 돌아감
            return "member/join";
        }
    }

}