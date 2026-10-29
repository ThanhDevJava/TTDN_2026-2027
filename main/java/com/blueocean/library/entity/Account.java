package com.blueocean.library.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class Account extends BaseEntity {
  @Column(nullable = false, unique = true, length = 80)
  public String username;

  @com.fasterxml.jackson.annotation.JsonIgnore
  @Column(nullable = false)
  public String passwordHash;

  @Column(nullable = false)
  public String role;

  public boolean enabled = true;
}
