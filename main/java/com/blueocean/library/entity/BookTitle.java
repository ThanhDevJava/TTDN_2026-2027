package com.blueocean.library.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "book_titles")
public class BookTitle extends BaseEntity {
  @Column(nullable = false, unique = true, length = 40)
  public String code;

  @Column(nullable = false)
  public String title;

  public Integer publicationYear;
  public boolean active = true;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  public Category category;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  public Publisher publisher;

  @ManyToMany
  @JoinTable(
      name = "book_authors",
      joinColumns = @JoinColumn(name = "book_title_id"),
      inverseJoinColumns = @JoinColumn(name = "author_id"))
  public java.util.Set<Author> authors = new java.util.LinkedHashSet<>();
}
