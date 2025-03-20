package hello.safedrivingback.temp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/temp")
public class ResultController {

    @GetMapping("/result")
    public String result(@RequestParam("fault") String fault, Model model) {
        model.addAttribute("fault", fault);
        return "temp/result";
    }
}