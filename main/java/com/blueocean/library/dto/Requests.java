package com.blueocean.library.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;

public final class Requests {
  public record Named(@Size(max = 40) String code, @NotBlank @Size(max = 200) String name) {}

  public record Book(
      @NotBlank @Size(max = 40) String code,
      @NotBlank @Size(max = 200) String title,
      @NotNull Long categoryId,
      @NotNull Long publisherId,
      @NotEmpty Set<@NotNull Long> authorIds,
      @Min(1) @Max(2100) Integer publicationYear) {}

  public record Copy(
      @NotBlank @Size(max = 40) String code, @NotBlank @Size(max = 200) String location) {}

  public record CopyEdit(
      @NotBlank @Size(max = 200) String location,
      @NotBlank @Pattern(regexp = "AVAILABLE|DAMAGED|LOST|RETIRED") String status) {}

  public record ReaderInput(
      @NotBlank @Size(max = 40) String code,
      @NotBlank @Size(max = 200) String fullName,
      @Email String email,
      String phone) {}

  public record Borrow(
      @NotNull Long readerId, @NotEmpty @Size(max = 50) List<@NotNull Long> copyIds) {}

  public record ReturnBook(
      @NotBlank @Pattern(regexp = "GOOD|DAMAGED|LOST") String condition,
      @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal compensation,
      @Size(max = 500) String reason) {}
}
