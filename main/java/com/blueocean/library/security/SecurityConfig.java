package com.blueocean.library.security;

import com.blueocean.library.repository.AccountRepository;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
  @Bean
  PasswordEncoder encoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService users(AccountRepository repo) {
    return username -> {
      var a =
          repo.findByUsername(username)
              .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
      return User.withUsername(a.username)
          .password(a.passwordHash)
          .roles(a.role)
          .disabled(!a.enabled)
          .build();
    };
  }

  @Bean
  SecurityFilterChain chain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            a ->
                a.requestMatchers("/", "/index.html", "/app.js", "/style.css", "/favicon.svg", "/api/csrf", "/login", "/error")
                    .permitAll()
                    .requestMatchers("/api/me", "/api/me/**")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/books/**", "/api/catalog/**")
                    .hasAnyRole("ADMIN", "LIBRARIAN", "READER")
                    .requestMatchers(
                        "/api/books/**",
                        "/api/copies/**",
                        "/api/catalog/**",
                        "/api/readers/**",
                        "/api/loans/**",
                        "/api/fines/**")
                    .hasRole("LIBRARIAN")
                    .anyRequest()
                    .denyAll())
        .formLogin(
            f ->
                f.successHandler((q, s, a) -> s.setStatus(204))
                    .failureHandler((q, s, e) -> s.sendError(401)))
        .logout(l -> l.logoutSuccessHandler((q, s, a) -> s.setStatus(204)))
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint((q, s, x) -> s.sendError(401))
                    .accessDeniedHandler((q, s, x) -> s.sendError(403)));
    return http.build();
  }
}
