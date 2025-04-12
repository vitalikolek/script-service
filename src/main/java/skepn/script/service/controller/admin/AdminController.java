package skepn.script.service.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin-panel")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    @RequestMapping("/upload")
    public String upload() {
        return "/admin-panel/upload";
    }

    @RequestMapping("/scripts")
    public String folders() {
        return "/admin-panel/scrips";
    }

    @RequestMapping("/users")
    public String adminPanel() {
        return "/admin-panel/users";
    }
}
