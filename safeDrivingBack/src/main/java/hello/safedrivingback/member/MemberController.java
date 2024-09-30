package hello.safedrivingback.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("member", new LoginMemberForm());
        return "login";
    }

    @PostMapping("/login")
    public String loginProcess(@ModelAttribute("member") LoginMemberForm loginForm) {

        String username = loginForm.getUsername();
        String password = loginForm.getPassword();

        boolean isAuthenticated = memberService.authenticate(username, password);

        if (isAuthenticated) {
            log.info("login success");
            return "redirect:/";
        } else{
            log.info("login failed");
            return "login";
        }
    }


    @GetMapping("/join")
    public String join() {
        return "member/join";
    }

    @PostMapping("/join")
    public String join(@RequestBody Member member) {    // view 구현후에는 ModelAttribute 사용
        memberService.join(member);
        log.info("join success");
        return "redirect:/member/login";
    }
}
