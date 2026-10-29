package com.blueocean.library.controller;

import com.blueocean.library.dto.Requests.ReaderInput;
import com.blueocean.library.service.ReaderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/readers")
public class ReaderController {
  private final ReaderService s;

  public ReaderController(ReaderService s) {
    this.s = s;
  }

  @GetMapping
  public Object list(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return s.list(page, size);
  }

  @PostMapping
  public Object add(@Valid @RequestBody ReaderInput r) {
    return s.save(null, r);
  }

  @PutMapping("/{id}")
  public Object edit(@PathVariable Long id, @Valid @RequestBody ReaderInput r) {
    return s.save(id, r);
  }

  @DeleteMapping("/{id}")
  public void deactivate(@PathVariable Long id) {
    s.deactivate(id);
  }
}
