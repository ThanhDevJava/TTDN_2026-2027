package com.blueocean.library.service;

import static com.blueocean.library.exception.Errors.*;

import com.blueocean.library.dto.Requests.*;
import com.blueocean.library.entity.*;
import com.blueocean.library.repository.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CatalogService {
  private final BookTitleRepository books;
  private final BookCopyRepository copies;
  private final CategoryRepository categories;
  private final AuthorRepository authors;
  private final PublisherRepository publishers;

  public CatalogService(
      BookTitleRepository b,
      BookCopyRepository c,
      CategoryRepository ca,
      AuthorRepository a,
      PublisherRepository p) {
    books = b;
    copies = c;
    categories = ca;
    authors = a;
    publishers = p;
  }

  public static Pageable page(int p, int size) {
    return PageRequest.of(
        Math.max(0, p), Math.max(1, Math.min(100, size)), Sort.by("id").descending());
  }

  @Transactional(readOnly = true)
  public Page<Map<String, Object>> search(String q, Long category, int p, int size) {
    return books.search(q, category, page(p, size)).map(Views::book);
  }

  @Transactional(readOnly = true)
  public Map<String, Object> get(Long id) {
    return Views.book(required(books.findById(id)));
  }

  public Map<String, Object> save(Long id, Book r) {
    BookTitle b = id == null ? new BookTitle() : required(books.findById(id));
    b.code = r.code().trim();
    b.title = r.title().trim();
    b.category = required(categories.findById(r.categoryId()));
    b.publisher = required(publishers.findById(r.publisherId()));
    b.authors = new LinkedHashSet<>(authors.findAllById(r.authorIds()));
    check(b.authors.size() == r.authorIds().size(), "Tác giả không tồn tại");
    b.publicationYear = r.publicationYear();
    return Views.book(books.save(b));
  }

  public void retire(Long id) {
    BookTitle b = required(books.findById(id));
    b.active = false;
  }

  public Map<String, Object> addCopy(Long bookId, Copy r) {
    BookTitle b = required(books.findById(bookId));
    check(b.active, "Đầu sách đã ngừng sử dụng");
    BookCopy c = new BookCopy();
    c.bookTitle = b;
    c.code = r.code().trim();
    c.location = r.location().trim();
    return Views.copy(copies.save(c));
  }

  public Map<String, Object> editCopy(Long id, CopyEdit r) {
    BookCopy c = required(copies.lockById(id));
    check(c.status != BookCopy.CopyStatus.BORROWED, "Không sửa trạng thái sách đang mượn");
    c.location = r.location().trim();
    c.status = BookCopy.CopyStatus.valueOf(r.status());
    return Views.copy(c);
  }

  @Transactional(readOnly = true)
  public Page<Map<String, Object>> copies(Long id, int p, int size) {
    return copies.findByBookTitleId(id, page(p, size)).map(Views::copy);
  }

  @Transactional(readOnly = true)
  public Map<String, Long> stock(Long id) {
    required(books.findById(id));
    Map<String, Long> result = new LinkedHashMap<>();
    for (var s : BookCopy.CopyStatus.values())
      result.put(s.name(), copies.countByBookTitleIdAndStatus(id, s));
    return result;
  }
}
