package skepn.script.service.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import skepn.script.service.model.User;
import skepn.script.service.service.UserService;
import skepn.script.service.service.WorkerService;

import java.util.List;

@RestController
@RequestMapping("/api/admin-panel")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private WorkerService workerService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/set-worker")
    public ResponseEntity<String> setWorkerRole(@RequestParam String username) {
        workerService.setWorkerRole(username);
        return ResponseEntity.ok("Worker role set for user: " + username);
    }

    @DeleteMapping("/delete-user")
    public ResponseEntity<String> deleteUser(@RequestParam String username) {
        userService.deleteUser(username);
        return ResponseEntity.ok("User deleted: " + username);
    }
}
