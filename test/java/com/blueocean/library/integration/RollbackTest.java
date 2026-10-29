package com.blueocean.library.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.blueocean.library.dto.Requests.Borrow;
import com.blueocean.library.entity.*;
import com.blueocean.library.repository.*;
import com.blueocean.library.service.LoanService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
class RollbackTest {
  @Autowired PlatformTransactionManager tm;
  @Autowired LoanService service;
  @Autowired AccountRepository accounts;
  @Autowired ReaderRepository readers;
  @Autowired CategoryRepository categories;
  @Autowired PublisherRepository publishers;
  @Autowired BookTitleRepository titles;
  @Autowired BookCopyRepository copies;
  @Autowired LoanRepository loans;

  @Test
  void failedSecondCopyRollsBackFirst() {
    var tx = new TransactionTemplate(tm);
    Long[] ids =
        tx.execute(
            s -> {
              Account a = new Account();
              a.username = "rollback";
              a.passwordHash = "unused";
              a.role = "LIBRARIAN";
              accounts.save(a);
              Reader r = new Reader();
              r.code = "ROLLBACK";
              r.fullName = "Reader";
              readers.save(r);
              Category c = new Category();
              c.code = "RB";
              c.name = "Rollback";
              categories.save(c);
              Publisher p = new Publisher();
              p.name = "Rollback";
              publishers.save(p);
              BookTitle b = new BookTitle();
              b.code = "RB";
              b.title = "Rollback";
              b.category = c;
              b.publisher = p;
              titles.save(b);
              BookCopy first = new BookCopy();
              first.code = "RB1";
              first.bookTitle = b;
              first.location = "A";
              copies.save(first);
              BookCopy second = new BookCopy();
              second.code = "RB2";
              second.bookTitle = b;
              second.location = "A";
              second.status = BookCopy.CopyStatus.DAMAGED;
              copies.save(second);
              return new Long[] {r.id, first.id, second.id};
            });
    long before = loans.count();
    assertThrows(
        org.springframework.web.server.ResponseStatusException.class,
        () -> service.borrow(new Borrow(ids[0], List.of(ids[1], ids[2])), "rollback"));
    assertEquals(before, loans.count());
    assertEquals(BookCopy.CopyStatus.AVAILABLE, copies.findById(ids[1]).orElseThrow().status);
  }
}
