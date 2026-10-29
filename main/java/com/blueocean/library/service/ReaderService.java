package com.blueocean.library.service;

import static com.blueocean.library.exception.Errors.*;

import com.blueocean.library.dto.Requests.ReaderInput;
import com.blueocean.library.entity.Reader;
import com.blueocean.library.repository.ReaderRepository;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReaderService {
  private final ReaderRepository repo;

  public ReaderService(ReaderRepository r) {
    repo = r;
  }

  private Map<String, Object> view(Reader r) {
    return Map.of(
        "id",
        r.id,
        "code",
        r.code,
        "fullName",
        r.fullName,
        "active",
        r.active,
        "email",
        r.email == null ? "" : r.email,
        "phone",
        r.phone == null ? "" : r.phone);
  }

  public Object save(Long id, ReaderInput input) {
    Reader r = id == null ? new Reader() : required(repo.lockById(id));
    r.code = input.code().trim();
    r.fullName = input.fullName().trim();
    r.email = input.email();
    r.phone = input.phone();
    return view(repo.save(r));
  }

  @Transactional(readOnly = true)
  public Object list(int page, int size) {
    return repo.findAll(CatalogService.page(page, size)).map(this::view);
  }

  public void deactivate(Long id) {
    required(repo.lockById(id)).active = false;
  }
}
