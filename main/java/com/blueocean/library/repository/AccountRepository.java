package com.blueocean.library.repository;

import com.blueocean.library.entity.*;
import org.springframework.data.jpa.repository.*;

public interface AccountRepository extends JpaRepository<Account, Long> {
  java.util.Optional<Account> findByUsername(String username);
}
