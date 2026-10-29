package com.blueocean.library.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "loans",
    indexes = @Index(name = "idx_loan_reader_date", columnList = "reader_id,borrowed_at"))
public class Loan extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  public Reader reader;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  public Account createdBy;

  @Column(name = "borrowed_at", nullable = false)
  public java.time.LocalDateTime borrowedAt;

  @Column(nullable = false)
  public String status = "OPEN";

  @OneToMany(mappedBy = "loan", cascade = CascadeType.PERSIST)
  public java.util.List<LoanDetail> details = new java.util.ArrayList<>();
}
