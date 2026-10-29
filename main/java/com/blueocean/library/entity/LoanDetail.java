package com.blueocean.library.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "loan_details",
    uniqueConstraints = @UniqueConstraint(columnNames = {"loan_id", "book_copy_id"}),
    indexes = @Index(name = "idx_detail_status_due", columnList = "status,due_date"))
public class LoanDetail extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  public Loan loan;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  public BookCopy bookCopy;

  @Column(name = "due_date", nullable = false)
  public java.time.LocalDate dueDate;

  public java.time.LocalDateTime returnedAt;
  public String conditionOnReturn;

  @Column(nullable = false)
  public String status = "BORROWED";

  @Column(nullable = false, precision = 14, scale = 2)
  public java.math.BigDecimal dailyRate;

  @Column(nullable = false, precision = 14, scale = 2)
  public java.math.BigDecimal lateFee = java.math.BigDecimal.ZERO;

  @Column(nullable = false, precision = 14, scale = 2)
  public java.math.BigDecimal compensation = java.math.BigDecimal.ZERO;

  public String compensationReason;

  @Column(nullable = false, precision = 14, scale = 2)
  public java.math.BigDecimal fineTotal = java.math.BigDecimal.ZERO;

  public java.time.LocalDateTime finePaidAt;

  @ManyToOne(fetch = FetchType.LAZY)
  public Account paidBy;

  @ManyToOne(fetch = FetchType.LAZY)
  public Account returnedBy;
}
