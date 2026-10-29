package com.blueocean.library.repository;

import com.blueocean.library.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ReaderRepository extends JpaRepository<Reader, Long> {
  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from Reader e where e.id=:id")
  java.util.Optional<Reader> lockById(@Param("id") Long id);
}
