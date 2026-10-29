package com.blueocean.library.service;

import com.blueocean.library.entity.*;
import java.util.*;

public final class Views {
  @SuppressWarnings("unchecked")
  public static <T> T loaded(T entity) { return (T) org.hibernate.Hibernate.unproxy(entity); }

  public static Map<String, Object> book(BookTitle b) {
    Map<String,Object> m = new LinkedHashMap<>();
    m.put("id", b.id); m.put("code", b.code); m.put("title", b.title);
    m.put("category", loaded(b.category).name); m.put("categoryId", loaded(b.category).id);
    m.put("publisher", loaded(b.publisher).name); m.put("publisherId", loaded(b.publisher).id);
    m.put("authors", b.authors.stream().map(x -> x.name).toList());
    m.put("authorIds", b.authors.stream().map(x -> x.id).toList());
    m.put("publicationYear", b.publicationYear); m.put("active", b.active);
    return m;
  }

  public static Map<String, Object> copy(BookCopy c) {
    return Map.of(
        "id",
        c.id,
        "code",
        c.code,
        "bookId",
        loaded(c.bookTitle).id,
        "location",
        c.location,
        "status",
        c.status);
  }

  public static Map<String, Object> loan(Loan l) {
    return Map.of(
        "id",
        l.id,
        "readerId",
        loaded(l.reader).id,
        "readerName",
        loaded(l.reader).fullName,
        "createdBy",
        loaded(l.createdBy).username,
        "borrowedAt",
        l.borrowedAt,
        "status",
        l.status);
  }

  public static Map<String, Object> detail(LoanDetail d) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", d.id);
    m.put("copyId", loaded(d.bookCopy).id);
    m.put("dueDate", d.dueDate);
    m.put("returnedAt", d.returnedAt);
    m.put("status", d.status);
    m.put("lateFee", d.lateFee);
    m.put("compensation", d.compensation);
    m.put("fineTotal", d.fineTotal);
    m.put("finePaidAt", d.finePaidAt);
    return m;
  }
}
