package com.blueocean.library.config;

import com.blueocean.library.entity.*;
import com.blueocean.library.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("demo")
public class DemoData {
  @Bean
  CommandLineRunner seed(
      AccountRepository accounts,
      ReaderRepository readers,
      PasswordEncoder encoder,
      @Value("${DEMO_PASSWORD}") String password) {
    return args -> {
      if (password.length() < 12)
        throw new IllegalArgumentException("DEMO_PASSWORD phải có ít nhất 12 ký tự");
      for (String role : new String[] {"ADMIN", "LIBRARIAN", "READER"}) {
        String username = role.toLowerCase();
        if (accounts.findByUsername(username).isEmpty()) {
          Account a = new Account();
          a.username = username;
          a.passwordHash = encoder.encode(password);
          a.role = role;
          accounts.save(a);
          if (role.equals("READER")) {
            Reader r = new Reader();
            r.code = "DG-DEMO";
            r.fullName = "Độc giả thử nghiệm";
            r.account = a;
            readers.save(r);
          }
        }
      }
    };
  }
}
