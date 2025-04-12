package skepn.script.service.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import skepn.script.service.model.User;
import skepn.script.service.model.UserRole;
import skepn.script.service.model.UserRoleType;
import skepn.script.service.repository.RoleRepository;
import skepn.script.service.repository.UserRepository;
import skepn.script.service.security.service.UserDetailsImpl;
import skepn.script.service.util.GeoUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public User createUser(String username, String password, String platform, String regIp) {
        String counryCode = "NO";
        GeoUtil.GeoData geoData = GeoUtil.getGeo(regIp);
        if (geoData != null && !StringUtils.isBlank(counryCode)) {
            counryCode = geoData.getCountryCode().equals("N/A") ? "NO" : geoData.getCountryCode();
        }

        User user = new User(username, password, regIp, platform, counryCode);

        UserRole userRole = roleRepository.findByName(UserRoleType.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("User role " + UserRoleType.ROLE_USER + " not found in repository"));

        Set<UserRole> roles = new HashSet<>();
        roles.add(userRole);

        user.setUserRoles(roles);

        userRepository.save(user);

        return user;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUser(Authentication authentication) {
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated()) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userRepository.findById(userDetails.getId()).orElse(null);
            //return userRepository.findByUsername(authentication.getName()).orElse(null);
        }

        return null;
    }

    public void deleteUser(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);
    }
}
