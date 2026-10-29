package com.blueocean.library.controller;

import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {
  @GetMapping("/api/me")
  public Map<String, Object> me(org.springframework.security.core.Authentication auth) {
    return Map.of("username", auth.getName(), "roles", auth.getAuthorities().stream()
      .map(a -> a.getAuthority().replace("ROLE_", "")).toList());
  }

  @GetMapping("/api/csrf")
  public Map<String, String> csrf(CsrfToken token) {
    return Map.of(
        "token",
        token.getToken(),
        "headerName",
        token.getHeaderName(),
        "parameterName",
        token.getParameterName());
  }
}
