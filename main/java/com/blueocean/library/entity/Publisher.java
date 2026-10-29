package com.blueocean.library.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "publishers")
public class Publisher extends BaseEntity {
  @Column(nullable = false)
  public String name;
}
