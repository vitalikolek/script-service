package skepn.script.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import skepn.script.service.security.jwt.AuthEntryPointJwt;
import skepn.script.service.security.jwt.AuthTokenFilter;
import skepn.script.service.security.service.UserDetailsServiceImpl;
import skepn.script.service.security.utils.NoOpPasswordEncoder;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity()
public class WebSecurityConfig {

  @Autowired
  private UserDetailsServiceImpl userDetailsService;

  @Autowired
  private AuthEntryPointJwt unauthorizedHandler;

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());

    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new NoOpPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.headers(headers ->
            headers.xssProtection(
                    xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
            ));

    http.cors(withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> {
              exception.authenticationEntryPoint(unauthorizedHandler);
            }).sessionManagement(session -> {
              session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            }).authorizeHttpRequests(auth -> {
              auth.requestMatchers("/").authenticated()
                      .requestMatchers("/scripts").authenticated()
                      .requestMatchers("/api/v1/scripts/**").authenticated()
                      .requestMatchers("/api/admin-panel/**").authenticated()
                      .requestMatchers("/admin-panel/**").authenticated()
                      .anyRequest().permitAll();
            });

    http .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));


            http.authenticationProvider(authenticationProvider());

    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
