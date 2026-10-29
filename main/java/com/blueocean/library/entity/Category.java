package com.blueocean.library.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category extends BaseEntity {
  @Column(nullable = false, unique = true, length = 40)
  public String code;

  @Column(nullable = false)
  public String name;
}
