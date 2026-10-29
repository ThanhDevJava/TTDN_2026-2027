package com.blueocean.library.service;

import static com.blueocean.library.exception.Errors.*;

import com.blueocean.library.dto.Requests.*;
import com.blueocean.library.entity.*;
import com.blueocean.library.repository.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LoanService {
  private final ReaderRepository readers;
  private final BookCopyRepository copies;
  private final LoanRepository loans;
  private final LoanDetailRepository details;
  private final AccountRepository accounts;
  private final FineCalculator calculator;
  private final Clock clock;
  private final int loanDays, maxBooks;
  private final BigDecimal dailyRate;

  public LoanService(
      ReaderRepository r,
      BookCopyRepository c,
      LoanRepository l,
      LoanDetailRepository d,
      AccountRepository a,
      FineCalculator f,
      Clock clock,
      @Value("${library.loan-days}") int days,
      @Value("${library.max-books}") int max,
      @Value("${library.daily-fine}") BigDecimal rate) {
    readers = r;
    copies = c;
    loans = l;
    details = d;
    accounts = a;
    calculator = f;
    this.clock = clock;
    loanDays = days;
    maxBooks = max;
    dailyRate = rate;
    if (days < 1 || max < 1 || rate.signum() < 0)
      throw new IllegalArgumentException("Cấu hình mượn/phạt không hợp lệ");
  }

  public Map<String, Object> borrow(Borrow request, String username) {
    Reader reader = required(readers.lockById(request.readerId()));
    check(reader.active, "Thẻ độc giả không hoạt động");
    List<Long> ids = request.copyIds().stream().distinct().sorted().toList();
    check(ids.size() == request.copyIds().size(), "Bản sách bị lặp");
    check(
        details.countByLoanReaderIdAndStatus(reader.id, "BORROWED") + ids.size() <= maxBooks,
        "Vượt giới hạn mượn");
    Loan loan = new Loan();
    loan.reader = reader;
    loan.createdBy = required(accounts.findByUsername(username));
    loan.borrowedAt = LocalDateTime.now(clock);
    for (Long id : ids) {
      BookCopy copy = required(copies.lockById(id));
      check(
          copy.status == BookCopy.CopyStatus.AVAILABLE && Views.loaded(copy.bookTitle).active,
          "Bản sách không thể cho mượn");
      copy.status = BookCopy.CopyStatus.BORROWED;
      LoanDetail d = new LoanDetail();
      d.loan = loan;
      d.bookCopy = copy;
      d.dueDate = LocalDate.now(clock).plusDays(loanDays);
      d.dailyRate = dailyRate;
      loan.details.add(d);
    }
    loans.saveAndFlush(loan);
    Map<String, Object> result = new LinkedHashMap<>(Views.loan(loan));
    result.put("details", loan.details.stream().map(Views::detail).toList());
    return result;
  }

  public Map<String, Object> returnBook(Long loanId, Long detailId, ReturnBook r, String username) {
    // Always lock parent first: two returns of the same loan cannot close it incorrectly.
    Loan loan = required(loans.lockById(loanId));
    LoanDetail d = required(details.lockById(detailId));
    check(Views.loaded(d.loan).id.equals(loanId), "Chi tiết không thuộc phiếu");
    check(d.status.equals("BORROWED"), "Sách đã được xử lý trả");
    check(
        !r.condition().equals("GOOD") || r.compensation().signum() == 0,
        "Sách tốt không có bồi thường");
    check(
        r.compensation().signum() == 0 || (r.reason() != null && !r.reason().isBlank()),
        "Cần lý do bồi thường");
    BookCopy copy = required(copies.lockById(Views.loaded(d.bookCopy).id));
    d.returnedAt = LocalDateTime.now(clock);
    d.conditionOnReturn = r.condition();
    d.status = "RETURNED";
    d.returnedBy = required(accounts.findByUsername(username));
    d.lateFee = calculator.lateFee(d.dueDate, d.returnedAt.toLocalDate(), d.dailyRate);
    d.compensation = r.compensation();
    d.compensationReason = r.reason();
    d.fineTotal = d.lateFee.add(d.compensation);
    copy.status =
        switch (r.condition()) {
          case "GOOD" -> BookCopy.CopyStatus.AVAILABLE;
          case "DAMAGED" -> BookCopy.CopyStatus.DAMAGED;
          case "LOST" -> BookCopy.CopyStatus.LOST;
          default -> throw new IllegalArgumentException();
        };
    if (details.findByLoanId(loanId).stream().allMatch(x -> !x.status.equals("BORROWED")))
      loan.status = "CLOSED";
    return Views.detail(d);
  }

  public Map<String, Object> payFine(Long detailId, String username) {
    LoanDetail d = required(details.lockById(detailId));
    check(!d.status.equals("BORROWED"), "Chưa chốt khoản phạt");
    check(d.fineTotal.signum() > 0, "Không có tiền phạt");
    check(d.finePaidAt == null, "Khoản phạt đã được thanh toán");
    d.finePaidAt = LocalDateTime.now(clock);
    d.paidBy = required(accounts.findByUsername(username));
    return Views.detail(d);
  }

  @Transactional(readOnly = true)
  public Page<Map<String, Object>> list(int p, int size) {
    return loans.findAll(CatalogService.page(p, size)).map(Views::loan);
  }

  @Transactional(readOnly = true)
  public Page<Map<String, Object>> mine(String user, int p, int size) {
    return loans.findByReaderAccountUsername(user, CatalogService.page(p, size)).map(Views::loan);
  }

  @Transactional(readOnly = true)
  public List<Map<String, Object>> detail(Long id) {
    required(loans.findById(id));
    return details.findByLoanId(id).stream().map(Views::detail).toList();
  }

  @Transactional(readOnly = true)
  public Page<Map<String, Object>> overdue(int p, int size) {
    return details
        .findByStatusAndDueDateBefore(
            "BORROWED", LocalDate.now(clock), CatalogService.page(p, size))
        .map(
            d -> {
              var m = Views.detail(d);
              m.put("readerId", Views.loaded(Views.loaded(d.loan).reader).id);
              m.put("readerName", Views.loaded(Views.loaded(d.loan).reader).fullName);
              m.put(
                  "estimatedFine",
                  calculator.lateFee(d.dueDate, LocalDate.now(clock), d.dailyRate));
              return m;
            });
  }
}
