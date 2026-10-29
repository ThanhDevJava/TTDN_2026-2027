package com.blueocean.library.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.blueocean.library.dto.Requests.*;
import com.blueocean.library.entity.*;
import com.blueocean.library.repository.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {
  @Mock ReaderRepository readers;
  @Mock BookCopyRepository copies;
  @Mock LoanRepository loans;
  @Mock LoanDetailRepository details;
  @Mock AccountRepository accounts;
  LoanService service;
  Reader reader;
  BookCopy copy;
  Account staff;

  @BeforeEach
  void setup() {
    service =
        new LoanService(
            readers,
            copies,
            loans,
            details,
            accounts,
            new FineCalculator(),
            Clock.fixed(Instant.parse("2026-10-20T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh")),
            14,
            5,
            new BigDecimal("5000"));
    reader = new Reader();
    reader.id = 1L;
    reader.fullName = "Reader";
    copy = new BookCopy();
    copy.id = 2L;
    copy.bookTitle = new BookTitle();
    staff = new Account();
    staff.id = 1L;
    staff.username = "staff";
  }

  @Test
  void inactiveReader() {
    reader.active = false;
    when(readers.lockById(1L)).thenReturn(Optional.of(reader));
    assertThrows(
        org.springframework.web.server.ResponseStatusException.class,
        () -> service.borrow(new Borrow(1L, List.of(2L)), "staff"));
    verifyNoInteractions(copies, loans);
  }

  @Test
  void duplicateCopies() {
    when(readers.lockById(1L)).thenReturn(Optional.of(reader));
    assertThrows(
        org.springframework.web.server.ResponseStatusException.class,
        () -> service.borrow(new Borrow(1L, List.of(2L, 2L)), "staff"));
    verifyNoInteractions(copies);
  }

  @Test
  void limitExceeded() {
    when(readers.lockById(1L)).thenReturn(Optional.of(reader));
    when(details.countByLoanReaderIdAndStatus(1L, "BORROWED")).thenReturn(5L);
    assertThrows(
        org.springframework.web.server.ResponseStatusException.class,
        () -> service.borrow(new Borrow(1L, List.of(2L)), "staff"));
    verifyNoInteractions(copies);
  }

  @Test
  void unavailable() {
    when(readers.lockById(1L)).thenReturn(Optional.of(reader));
    when(accounts.findByUsername("staff")).thenReturn(Optional.of(staff));
    copy.status = BookCopy.CopyStatus.BORROWED;
    when(copies.lockById(2L)).thenReturn(Optional.of(copy));
    assertThrows(
        org.springframework.web.server.ResponseStatusException.class,
        () -> service.borrow(new Borrow(1L, List.of(2L)), "staff"));
    verify(loans, never()).saveAndFlush(any());
  }

  @Test
  void borrowSuccess() {
    when(readers.lockById(1L)).thenReturn(Optional.of(reader));
    when(accounts.findByUsername("staff")).thenReturn(Optional.of(staff));
    when(copies.lockById(2L)).thenReturn(Optional.of(copy));
    when(loans.saveAndFlush(any()))
        .thenAnswer(
            i -> {
              Loan l = i.getArgument(0);
              l.id = 9L;
              return l;
            });
    var result = service.borrow(new Borrow(1L, List.of(2L)), "staff");
    assertEquals(9L, result.get("id"));
    assertEquals(BookCopy.CopyStatus.BORROWED, copy.status);
    ArgumentCaptor<Loan> saved = ArgumentCaptor.forClass(Loan.class);
    verify(loans).saveAndFlush(saved.capture());
    assertEquals(LocalDate.of(2026, 11, 3), saved.getValue().details.get(0).dueDate);
  }

  @Test
  void repeatedReturnRejected() {
    Loan l = new Loan();
    l.id = 9L;
    LoanDetail d = new LoanDetail();
    d.loan = l;
    d.status = "RETURNED";
    when(loans.lockById(9L)).thenReturn(Optional.of(l));
    when(details.lockById(8L)).thenReturn(Optional.of(d));
    assertThrows(
        org.springframework.web.server.ResponseStatusException.class,
        () -> service.returnBook(9L, 8L, new ReturnBook("GOOD", BigDecimal.ZERO, null), "staff"));
    verifyNoInteractions(copies);
  }

  @Test
  void partialReturnKeepsLoanOpen() {
    Loan l = new Loan();
    l.id = 9L;
    LoanDetail d = new LoanDetail();
    d.id = 8L;
    d.loan = l;
    d.bookCopy = copy;
    d.dueDate = LocalDate.of(2026, 10, 18);
    d.dailyRate = new BigDecimal("5000");
    LoanDetail outstanding = new LoanDetail();
    when(loans.lockById(9L)).thenReturn(Optional.of(l));
    when(details.lockById(8L)).thenReturn(Optional.of(d));
    when(copies.lockById(2L)).thenReturn(Optional.of(copy));
    when(accounts.findByUsername("staff")).thenReturn(Optional.of(staff));
    when(details.findByLoanId(9L)).thenReturn(List.of(d, outstanding));
    service.returnBook(
        9L, 8L, new ReturnBook("DAMAGED", new BigDecimal("20000"), "Rách bìa"), "staff");
    assertEquals("OPEN", l.status);
    assertEquals(new BigDecimal("30000"), d.fineTotal);
    assertEquals(BookCopy.CopyStatus.DAMAGED, copy.status);
  }

  @Test
  void duplicatePaymentRejected() {
    LoanDetail d = new LoanDetail();
    d.status = "RETURNED";
    d.fineTotal = BigDecimal.TEN;
    d.finePaidAt = LocalDateTime.now();
    when(details.lockById(8L)).thenReturn(Optional.of(d));
    assertThrows(
        org.springframework.web.server.ResponseStatusException.class,
        () -> service.payFine(8L, "staff"));
  }
}
