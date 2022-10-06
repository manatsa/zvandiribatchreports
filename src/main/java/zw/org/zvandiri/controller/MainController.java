package zw.org.zvandiri.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import zw.org.zvandiri.BaseController;

/**
 * @author :: codemaster
 * created on :: 4/10/2022
 * Package Name :: zw.org.zvandiri.controller
 */

@Controller
@RequestMapping("report")
public class MainController extends BaseController {

    @GetMapping({"/","index","index.htm"})
    public String index(Model model){
        model.addAttribute("pageTitle", APP_PREFIX + "Reports Dashboard");
        return "index";
    }
}
