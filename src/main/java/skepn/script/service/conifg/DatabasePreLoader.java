package skepn.script.service.conifg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import skepn.script.service.model.UserRole;
import skepn.script.service.model.UserRoleType;
import skepn.script.service.repository.RoleRepository;

@Component
public class DatabasePreLoader implements ApplicationRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) {
        this.createRoles();
    }

    private void createRoles() {
        for (UserRoleType type : UserRoleType.values()) {
            if (!this.roleRepository.existsByName(type)) {
                UserRole userRole = new UserRole(type);
                this.roleRepository.save(userRole);
            }
        }
    }
}
