package pe.fiadoya.stores.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.fiadoya.iam.application.Access;
import pe.fiadoya.reporting.application.CycleService;
import pe.fiadoya.shared.application.OperationalClock;
import pe.fiadoya.shared.infrastructure.*;
import pe.fiadoya.shared.infrastructure.Model.*;

@RestController
@RequestMapping("/api")
@Transactional
public class PlatformSettingsController {
  private final Data data;
  private final Access access;
  private final OperationalClock clock;
  private final CycleService cycles;

  public PlatformSettingsController(
      Data data, Access access, OperationalClock clock, CycleService cycles) {
    this.data = data;
    this.access = access;
    this.clock = clock;
    this.cycles = cycles;
  }

  public record ClockInput(@NotNull LocalDateTime dateTime) {}

  public record LimitInput(
      @NotBlank @Pattern(regexp = "PEN|USD") String currency,
      @NotBlank @Pattern(regexp = "COMPENSATORY|PENALTY") String kind,
      @NotNull @DecimalMin("0") java.math.BigDecimal maxTeaPercent,
      @NotNull LocalDate validFrom,
      @NotNull LocalDate validUntil,
      @NotBlank @Size(max = 255) String source) {}

  @GetMapping("/clock")
  public Object clock() {
    return Map.of("dateTime", clock.now(), "demo", clock.demo());
  }

  @PostMapping("/clock")
  public Object setClock(@Valid @RequestBody ClockInput input) {
    access.platformOnly();
    clock.set(input.dateTime());
    return clock();
  }

  @GetMapping("/rate-limits")
  public Object limits() {
    return data.list(RateLimit.class, "order by e.validFrom desc");
  }

  @PostMapping("/rate-limits")
  public Object limit(@Valid @RequestBody LimitInput input) {
    access.platformOnly();
    if (input.validUntil().isBefore(input.validFrom()))
      throw new IllegalArgumentException("Vigencia invertida");
    RateLimit limit = new RateLimit();
    limit.currency = input.currency();
    limit.kind = input.kind();
    limit.maxTeaPercent = input.maxTeaPercent();
    limit.validFrom = input.validFrom();
    limit.validUntil = input.validUntil();
    limit.source = input.source();
    return data.save(limit);
  }
}
