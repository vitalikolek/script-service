package skepn.script.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import skepn.script.service.model.User;
import skepn.script.service.service.UserService;

@Controller
public class ApplicationController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/scripts")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_WORKER')")
    public String folders(Model model, Authentication authentication) {

        addUserAttribute(model, authentication);
        return "scripts";
    }

    private void addUserAttribute(Model model, Authentication authentication) {
        User user = userService.getUser(authentication);

        model.addAttribute("user", user);
    }
}
