package com.blueocean.library.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "book_copies")
public class BookCopy extends BaseEntity {
  @Column(nullable = false, unique = true, length = 40)
  public String code;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  public BookTitle bookTitle;

  public String location;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public CopyStatus status = CopyStatus.AVAILABLE;

  public enum CopyStatus {
    AVAILABLE,
    BORROWED,
    DAMAGED,
    LOST,
    RETIRED
  }
}
