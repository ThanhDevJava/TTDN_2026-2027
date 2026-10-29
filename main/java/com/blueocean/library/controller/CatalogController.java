package com.blueocean.library.controller;

import com.blueocean.library.dto.Requests.*;
import com.blueocean.library.service.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CatalogController {
  private final CatalogService service;

  public CatalogController(CatalogService s) {
    service = s;
  }

  @GetMapping("/books")
  public Object list(
      @RequestParam(defaultValue = "") String q,
      @RequestParam(required = false) Long category,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return service.search(q, category, page, size);
  }

  @GetMapping("/books/{id}")
  public Object get(@PathVariable Long id) {
    return service.get(id);
  }

  @PostMapping("/books")
  public Object create(@Valid @RequestBody Book r) {
    return service.save(null, r);
  }

  @PutMapping("/books/{id}")
  public Object update(@PathVariable Long id, @Valid @RequestBody Book r) {
    return service.save(id, r);
  }

  @DeleteMapping("/books/{id}")
  public void retire(@PathVariable Long id) {
    service.retire(id);
  }

  @PostMapping("/books/{id}/copies")
  public Object copy(@PathVariable Long id, @Valid @RequestBody Copy r) {
    return service.addCopy(id, r);
  }

  @GetMapping("/books/{id}/copies")
  public Object copies(
      @PathVariable Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return service.copies(id, page, size);
  }

  @GetMapping("/books/{id}/stock")
  public Object stock(@PathVariable Long id) {
    return service.stock(id);
  }

  @PatchMapping("/copies/{id}")
  public Object edit(@PathVariable Long id, @Valid @RequestBody CopyEdit r) {
    return service.editCopy(id, r);
  }
}
