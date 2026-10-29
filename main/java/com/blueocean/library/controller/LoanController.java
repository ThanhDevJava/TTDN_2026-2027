package com.blueocean.library.controller;

import com.blueocean.library.dto.Requests.*;
import com.blueocean.library.service.LoanService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LoanController {
  private final LoanService s;

  public LoanController(LoanService s) {
    this.s = s;
  }

  @PostMapping("/loans")
  public Object borrow(@Valid @RequestBody Borrow r, Principal p) {
    return s.borrow(r, p.getName());
  }

  @PostMapping("/loans/{loanId}/details/{detailId}/return")
  public Object returnBook(
      @PathVariable Long loanId,
      @PathVariable Long detailId,
      @Valid @RequestBody ReturnBook r,
      Principal p) {
    return s.returnBook(loanId, detailId, r, p.getName());
  }

  @PostMapping("/fines/{detailId}/pay")
  public Object pay(@PathVariable Long detailId, Principal p) {
    return s.payFine(detailId, p.getName());
  }

  @GetMapping("/loans")
  public Object list(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return s.list(page, size);
  }

  @GetMapping("/loans/{id}/details")
  public Object detail(@PathVariable Long id) {
    return s.detail(id);
  }

  @GetMapping("/loans/overdue")
  public Object overdue(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return s.overdue(page, size);
  }

  @GetMapping("/me/loans")
  public Object mine(
      Principal p,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return s.mine(p.getName(), page, size);
  }
}
