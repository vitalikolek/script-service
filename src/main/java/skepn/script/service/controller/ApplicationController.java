package skepn.script.service.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApplicationController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/folders")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_WORKER')")
    public String folders() {
        return "folders";
    }
}
