package skepn.script.service.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    @RequestMapping("/upload")
    public String upload() {
        return "admin/upload";
    }

    @RequestMapping("/folders")
    public String folders() {
        return "admin/folders";
    }

    @RequestMapping("/admin-panel")
    public String adminPanel() {
        return "admin/admin-panel";
    }
}
