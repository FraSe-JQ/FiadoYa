package pe.fiadoya.support.api;

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
public class SupportController {
  private final Data data;
  private final Access access;
  private final OperationalClock clock;
  private final CycleService cycles;

  public SupportController(Data data, Access access, OperationalClock clock, CycleService cycles) {
    this.data = data;
    this.access = access;
    this.clock = clock;
    this.cycles = cycles;
  }

  public record TicketInput(
      @NotBlank @Size(max = 150) String subject, @NotBlank @Size(max = 4000) String message) {}

  public record ArticleInput(
      @NotBlank @Size(max = 150) String title, @NotBlank @Size(max = 10000) String content) {}

  @GetMapping("/help")
  public Object help() {
    return data.list(Help.class, "order by e.id");
  }

  @PutMapping("/help/{id}")
  public Object editHelp(@PathVariable Long id, @Valid @RequestBody ArticleInput input) {
    access.platformOnly();
    Help article = data.get(Help.class, id);
    article.title = input.title();
    article.content = input.content();
    return article;
  }

  @PostMapping("/support")
  public Object ticket(@Valid @RequestBody TicketInput input) {
    Ticket ticket = new Ticket();
    ticket.userId = access.user().id;
    ticket.subject = input.subject();
    ticket.message = input.message();
    ticket.createdAt = Instant.now();
    return data.save(ticket);
  }

  @GetMapping("/support")
  public Object tickets() {
    return access.platform()
        ? data.list(Ticket.class, "order by e.id desc")
        : data.list(Ticket.class, "where e.userId=?1 order by e.id desc", access.user().id);
  }
}
