package com.blueocean.library.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "authors")
public class Author extends BaseEntity {
  @Column(nullable = false)
  public String name;
}
