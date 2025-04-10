package skepn.script.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import skepn.script.service.model.UserRole;
import skepn.script.service.model.UserRoleType;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<UserRole, Long> {

  Optional<UserRole> findByName(UserRoleType name);

  Boolean existsByName(UserRoleType name);
}
