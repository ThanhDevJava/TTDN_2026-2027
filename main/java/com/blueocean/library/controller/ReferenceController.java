package com.blueocean.library.controller;

import com.blueocean.library.dto.Requests.Named;
import com.blueocean.library.service.ReferenceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalog/{type}")
public class ReferenceController {
  private final ReferenceService s;

  public ReferenceController(ReferenceService s) {
    this.s = s;
  }

  @GetMapping
  public Object list(
      @PathVariable String type,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return s.list(type, page, size);
  }

  @PostMapping
  public Object create(@PathVariable String type, @Valid @RequestBody Named r) {
    return s.save(type, null, r);
  }

  @PutMapping("/{id}")
  public Object update(
      @PathVariable String type, @PathVariable Long id, @Valid @RequestBody Named r) {
    return s.save(type, id, r);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable String type, @PathVariable Long id) {
    s.delete(type, id);
  }
}
