package com.blueocean.library.repository;

import com.blueocean.library.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<Loan, Long> {
  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from Loan e where e.id=:id")
  java.util.Optional<Loan> lockById(@Param("id") Long id);

  @EntityGraph(attributePaths = {"reader", "createdBy"})
  org.springframework.data.domain.Page<Loan> findAll(org.springframework.data.domain.Pageable page);

  @EntityGraph(attributePaths = {"reader", "createdBy"})
  org.springframework.data.domain.Page<Loan> findByReaderAccountUsername(
      String username, org.springframework.data.domain.Pageable page);
}
