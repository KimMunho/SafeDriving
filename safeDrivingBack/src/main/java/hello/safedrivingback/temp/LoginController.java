package hello.safedrivingback.temp;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/temp")
public class LoginController {

    @GetMapping("/login")
    public String loginPage() {
        return "temp/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        if("admin".equals(username) && "admin".equals(password)) {
            return "redirect:/temp/upload";
        }

        System.out.println("로그인 안됨");
        return "temp/login";
    }
}
