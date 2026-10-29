package com.blueocean.library.repository;

import com.blueocean.library.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from BookCopy e where e.id=:id")
  java.util.Optional<BookCopy> lockById(@Param("id") Long id);

  @EntityGraph(attributePaths = {"bookTitle"})
  org.springframework.data.domain.Page<BookCopy> findByBookTitleId(
      Long id, org.springframework.data.domain.Pageable page);

  long countByBookTitleIdAndStatus(Long id, BookCopy.CopyStatus status);
}
