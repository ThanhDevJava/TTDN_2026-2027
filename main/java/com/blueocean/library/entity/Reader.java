package com.blueocean.library.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "readers")
public class Reader extends BaseEntity {
  @Column(nullable = false, unique = true, length = 40)
  public String code;

  @Column(nullable = false)
  public String fullName;

  public String email;
  public String phone;
  public boolean active = true;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(unique = true)
  public Account account;
}
