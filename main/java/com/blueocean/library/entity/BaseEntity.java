package com.blueocean.library.entity;

import jakarta.persistence.*;

@MappedSuperclass
public abstract class BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Version public long version;
}
