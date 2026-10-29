package com.blueocean.library.repository;

import com.blueocean.library.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface BookTitleRepository extends JpaRepository<BookTitle, Long> {
  @EntityGraph(attributePaths = {"category", "publisher"})
  @Query(
      "select b from BookTitle b where (:q='' or lower(b.title) like lower(concat('%',:q,'%')) or"
          + " b.code=:q) and (:category is null or b.category.id=:category)")
  org.springframework.data.domain.Page<BookTitle> search(
      @Param("q") String q,
      @Param("category") Long category,
      org.springframework.data.domain.Pageable page);
}
