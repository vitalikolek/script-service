package skepn.script.service.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;
import skepn.script.service.model.User;
import skepn.script.service.payload.request.RegisterRequest;
import skepn.script.service.payload.response.UserInfoResponse;
import skepn.script.service.repository.UserRepository;
import skepn.script.service.security.jwt.JwtUtils;
import skepn.script.service.security.service.UserDetailsImpl;
import skepn.script.service.util.DataValidator;
import skepn.script.service.util.ServletUtil;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthenticationService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    public ResponseEntity<?> handleRegistration(RegisterRequest registerRequest, HttpServletRequest request) {
        if (!isValidRegisterRequest(registerRequest)) {
            return ResponseEntity.badRequest().body("validation_failed");
        }

        if (isUsernameTaken(registerRequest)) {
            return ResponseEntity.badRequest().body("email_or_username_taken");
        }

        String platform = ServletUtil.getPlatform(request);
        String regIp = ServletUtil.getIpAddress(request);

        User user = null;
        boolean emailRequiredConfirm = false;

        if (user == null) {
            user = createUser(registerRequest, platform, regIp);
        }

        if (emailRequiredConfirm) {
            return ResponseEntity.ok("email_confirm");
        }

        return authenticate(user, registerRequest.getPassword());
    }

    public ResponseEntity<UserInfoResponse> authenticate(User user, String password) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getUsername().toLowerCase(), password));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        user.setAuthCount(user.getAuthCount() + 1);

        userRepository.save(user);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new UserInfoResponse(userDetails.getId(),
                        userDetails.getUsername(),
                        roles));
    }

    private boolean isValidRegisterRequest(RegisterRequest registerRequest) {
        if (!DataValidator.isUsernameValided(registerRequest.getUsername())) {
            return false;
        }

        if (registerRequest.getPassword().length() < 8 || registerRequest.getPassword().length() > 64) {
            return false;
        }

        return true;
    }

    private boolean isUsernameTaken(RegisterRequest registerRequest) {

        if (userRepository.existsByUsernameIgnoreCase(registerRequest.getUsername())) {
            return true;
        }

        return false;
    }

    private User createUser(RegisterRequest registerRequest, String platform, String regIp) {
        return userService.createUser(registerRequest.getUsername(), registerRequest.getPassword(), platform, regIp);
    }

    private String getReferrer(HttpServletRequest request) {
        return WebUtils.getCookie(request, "referrer") == null ? "" : WebUtils.getCookie(request, "referrer").getValue();
    }
}
