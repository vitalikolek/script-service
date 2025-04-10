package skepn.script.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import skepn.script.service.model.User;
import skepn.script.service.model.UserRole;
import skepn.script.service.model.UserRoleType;
import skepn.script.service.repository.RoleRepository;
import skepn.script.service.repository.UserRepository;

@Service
public class WorkerService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public void setWorkerRole(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserRole role = roleRepository.findByName(UserRoleType.ROLE_WORKER).orElseThrow();

        user.addRole(role);
        userRepository.save(user);
    }
}
