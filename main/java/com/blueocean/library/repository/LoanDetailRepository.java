package com.blueocean.library.repository;

import com.blueocean.library.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface LoanDetailRepository extends JpaRepository<LoanDetail, Long> {
  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from LoanDetail e where e.id=:id")
  java.util.Optional<LoanDetail> lockById(@Param("id") Long id);

  long countByLoanReaderIdAndStatus(Long readerId, String status);

  @EntityGraph(attributePaths = {"bookCopy"})
  java.util.List<LoanDetail> findByLoanId(Long id);

  @EntityGraph(attributePaths = {"loan", "loan.reader", "bookCopy"})
  org.springframework.data.domain.Page<LoanDetail> findByStatusAndDueDateBefore(
      String status, java.time.LocalDate date, org.springframework.data.domain.Pageable page);
}
