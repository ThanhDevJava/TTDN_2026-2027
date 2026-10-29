package com.blueocean.library.service;

import static com.blueocean.library.exception.Errors.*;

import com.blueocean.library.dto.Requests.Named;
import com.blueocean.library.entity.*;
import com.blueocean.library.repository.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReferenceService {
  private final CategoryRepository c;
  private final AuthorRepository a;
  private final PublisherRepository p;

  public ReferenceService(CategoryRepository c, AuthorRepository a, PublisherRepository p) {
    this.c = c;
    this.a = a;
    this.p = p;
  }

  private RuntimeException invalid() {
    return new org.springframework.web.server.ResponseStatusException(
        org.springframework.http.HttpStatus.NOT_FOUND, "Danh mục không tồn tại");
  }

  @Transactional(readOnly = true)
  public Object list(String type, int page, int size) {
    var pg = CatalogService.page(page, size);
    return switch (type) {
      case "categories" -> c.findAll(pg);
      case "authors" -> a.findAll(pg);
      case "publishers" -> p.findAll(pg);
      default -> throw invalid();
    };
  }

  public Object save(String type, Long id, Named r) {
    return switch (type) {
      case "categories" -> {
        Category x = id == null ? new Category() : required(c.findById(id));
        if (r.code() == null || r.code().isBlank())
          throw new org.springframework.web.server.ResponseStatusException(
              org.springframework.http.HttpStatus.BAD_REQUEST, "Mã thể loại bắt buộc");
        x.code = r.code().trim();
        x.name = r.name().trim();
        yield c.save(x);
      }
      case "authors" -> {
        Author x = id == null ? new Author() : required(a.findById(id));
        x.name = r.name().trim();
        yield a.save(x);
      }
      case "publishers" -> {
        Publisher x = id == null ? new Publisher() : required(p.findById(id));
        x.name = r.name().trim();
        yield p.save(x);
      }
      default -> throw invalid();
    };
  }

  public void delete(String type, Long id) {
    switch (type) {
      case "categories" -> c.delete(required(c.findById(id)));
      case "authors" -> a.delete(required(a.findById(id)));
      case "publishers" -> p.delete(required(p.findById(id)));
      default -> throw invalid();
    }
  }
}
