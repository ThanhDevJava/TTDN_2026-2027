package com.blueocean.library.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.blueocean.library.dto.Requests.*;
import com.blueocean.library.entity.*;
import com.blueocean.library.repository.*;
import com.blueocean.library.service.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiTest {
  @Autowired MockMvc mvc;
  @Autowired CatalogService catalog;
  @Autowired LoanService service;
  @Autowired AccountRepository accounts;
  @Autowired ReaderRepository readers;
  @Autowired CategoryRepository categories;
  @Autowired PublisherRepository publishers;
  @Autowired AuthorRepository authors;
  @Autowired BookTitleRepository titles;
  @Autowired BookCopyRepository copies;
  @Autowired LoanRepository loans;
  @Autowired LoanDetailRepository details;
  @Autowired EntityManager em;
  @Autowired PasswordEncoder encoder;
  Reader reader;
  BookTitle title;
  BookCopy c1, c2;

  @BeforeEach
  void seed() {
    Account a = new Account();
    a.username = "staff";
    a.role = "LIBRARIAN";
    a.passwordHash = encoder.encode("Test-password-123");
    accounts.save(a);
    reader = new Reader();
    reader.code = "DG01";
    reader.fullName = "Reader";
    readers.save(reader);
    Category ca = new Category();
    ca.code = "IT";
    ca.name = "CNTT";
    categories.save(ca);
    Publisher p = new Publisher();
    p.name = "NXB";
    publishers.save(p);
    Author au = new Author();
    au.name = "Author";
    authors.save(au);
    title = new BookTitle();
    title.code = "JAVA";
    title.title = "Java";
    title.category = ca;
    title.publisher = p;
    title.authors.add(au);
    titles.save(title);
    c1 = new BookCopy();
    c1.code = "C1";
    c1.location = "A1";
    c1.bookTitle = title;
    copies.save(c1);
    c2 = new BookCopy();
    c2.code = "C2";
    c2.location = "A1";
    c2.bookTitle = title;
    copies.save(c2);
    em.flush();
  }

  @Test
  void anonymousDenied() throws Exception {
    mvc.perform(get("/api/books")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "READER")
  void readerCannotWrite() throws Exception {
    mvc.perform(post("/api/books").with(csrf()).contentType("application/json").content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "LIBRARIAN")
  void csrfRequired() throws Exception {
    mvc.perform(post("/api/books").contentType("application/json").content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "READER")
  void readCatalog() throws Exception {
    mvc.perform(get("/api/books"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].code").value("JAVA"));
  }

  @Test
  @WithMockUser(roles = "LIBRARIAN")
  void invalidBook() throws Exception {
    mvc.perform(post("/api/books").with(csrf()).contentType("application/json").content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void fullBorrowReturn() {
    var result = service.borrow(new Borrow(reader.id, List.of(c1.id, c2.id)), "staff");
    Long id = (Long) result.get("id");
    var items = details.findByLoanId(id);
    service.returnBook(id, items.get(0).id, new ReturnBook("GOOD", BigDecimal.ZERO, null), "staff");
    assertEquals("OPEN", loans.findById(id).orElseThrow().status);
    service.returnBook(id, items.get(1).id, new ReturnBook("GOOD", BigDecimal.ZERO, null), "staff");
    assertEquals("CLOSED", loans.findById(id).orElseThrow().status);
    assertEquals(2, copies.countByBookTitleIdAndStatus(title.id, BookCopy.CopyStatus.AVAILABLE));
  }

  @Test
  void queryCountDoesNotGrowPerBook() {
    for (int i = 0; i < 10; i++) {
      BookTitle b = new BookTitle();
      b.code = "B" + i;
      b.title = "Book" + i;
      b.category = title.category;
      b.publisher = title.publisher;
      b.authors.addAll(title.authors);
      titles.save(b);
    }
    em.flush();
    em.clear();
    var stats =
        em.getEntityManagerFactory().unwrap(org.hibernate.SessionFactory.class).getStatistics();
    stats.clear();
    var page = catalog.search("", null, 0, 10);
    assertEquals(10, page.getNumberOfElements());
    assertTrue(
        stats.getPrepareStatementCount() <= 4,
        "Unexpected N+1: " + stats.getPrepareStatementCount());
  }

  @Test
  void loginLogoutSession() throws Exception {
    var result =
        mvc.perform(
                post("/login")
                    .with(csrf())
                    .param("username", "staff")
                    .param("password", "Test-password-123"))
            .andExpect(status().isNoContent())
            .andReturn();
    var session =
        (org.springframework.mock.web.MockHttpSession) result.getRequest().getSession(false);
    assertNotNull(session);
    mvc.perform(get("/api/books").session(session)).andExpect(status().isOk());
    mvc.perform(post("/logout").session(session).with(csrf())).andExpect(status().isNoContent());
    assertTrue(session.isInvalid());
  }

  @Test
  void wrongPasswordRejected() throws Exception {
    mvc.perform(post("/login").with(csrf()).param("username", "staff").param("password", "wrong"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(username = "someone-else", roles = "READER")
  void personalHistoryCannotReadOthers() throws Exception {
    service.borrow(new Borrow(reader.id, List.of(c1.id)), "staff");
    mvc.perform(get("/api/me/loans"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(0));
    mvc.perform(get("/api/loans")).andExpect(status().isForbidden());
  }
}
