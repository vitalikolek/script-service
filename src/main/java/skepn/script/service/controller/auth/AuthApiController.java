package skepn.script.service.controller.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import skepn.script.service.model.User;
import skepn.script.service.payload.request.LoginRequest;
import skepn.script.service.payload.request.RegisterRequest;
import skepn.script.service.repository.UserRepository;
import skepn.script.service.security.jwt.JwtUtils;
import skepn.script.service.service.AuthenticationService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;


    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername().toLowerCase())
                .orElse(userRepository.findByUsername(loginRequest.getUsername()).orElse(null));
        if (user == null) {
            return ResponseEntity.badRequest().body("user_not_found");
        }

        if (user.isTwoFactorEnabled()) {
            String token = jwtUtils.generateTokenFromUsernameAndPassword(user.getUsername(), user.getPassword());
            return ResponseEntity.ok("jwt_two_factor: " + token);
        }

        return authenticationService.authenticate(user, loginRequest.getPassword());
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest, HttpServletRequest request) {
        return authenticationService.handleRegistration(registerRequest, request);
    }

    @GetMapping("/logout")
    public RedirectView logoutUser(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(jwtUtils.getJwtCookie())) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
        request.getSession().invalidate();
        return new RedirectView("/signin");
    }
}
