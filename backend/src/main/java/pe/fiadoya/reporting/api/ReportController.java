package pe.fiadoya.reporting.api;

import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.fiadoya.iam.application.Access;
import pe.fiadoya.reporting.application.CycleService;
import pe.fiadoya.reporting.domain.EffectiveYieldCalculator;
import pe.fiadoya.shared.application.OperationalClock;
import pe.fiadoya.shared.infrastructure.*;
import pe.fiadoya.shared.infrastructure.Model.*;

@RestController
@RequestMapping("/api")
@Transactional
public class ReportController {
  private final Data data;
  private final Access access;
  private final OperationalClock clock;
  private final CycleService cycles;

  public ReportController(Data data, Access access, OperationalClock clock, CycleService cycles) {
    this.data = data;
    this.access = access;
    this.clock = clock;
    this.cycles = cycles;
  }

  @PostMapping("/stores/{storeId}/cycles/close")
  public Object close(@PathVariable Long storeId) {
    access.store(storeId, true);
    return Map.of("generated", cycles.close(storeId, clock.now()));
  }

  @GetMapping("/stores/{storeId}/audit")
  public Object audit(@PathVariable Long storeId) {
    access.store(storeId, true);
    return data.list(Audit.class, "where e.storeId=?1 order by e.id desc", storeId);
  }

  @GetMapping("/stores/{storeId}/purchases/{id}/yield")
  public Object yield(@PathVariable Long storeId, @PathVariable Long id) {
    Purchase purchase = data.get(Purchase.class, id);
    access.account(storeId, purchase.accountId, false);
    if (!purchase.storeId.equals(storeId))
      throw new org.springframework.security.access.AccessDeniedException("Compra ajena");
    var flows =
        data.list(Obligation.class, "where e.purchaseId=?1 order by e.number", id).stream()
            .map(o -> new EffectiveYieldCalculator.Flow(o.dueDate, o.amount))
            .toList();
    return Map.of(
        "annualEffectiveYield",
        new EffectiveYieldCalculator()
            .annualYield(purchase.financed, purchase.purchasedAt.toLocalDate(), flows),
        "flows",
        flows,
        "description",
        "Rendimiento contractual del financiamiento; excluye costos, mora y rentabilidad global");
  }

  @GetMapping(value = "/stores/{storeId}/accounts/{id}/export.csv", produces = "text/csv")
  public org.springframework.http.ResponseEntity<String> csv(
      @PathVariable Long storeId, @PathVariable Long id) {
    access.account(storeId, id, false);
    StringBuilder csv =
        new StringBuilder("numero,vencimiento,capital,compensatorio,total,estado\r\n");
    for (Obligation obligation :
        data.list(Obligation.class, "where e.accountId=?1 order by e.dueDate,e.id", id))
      csv.append(obligation.number)
          .append(',')
          .append(obligation.dueDate)
          .append(',')
          .append(obligation.principal)
          .append(',')
          .append(obligation.interest)
          .append(',')
          .append(obligation.amount)
          .append(',')
          .append(obligation.paid ? "PAGADA" : "PENDIENTE")
          .append("\r\n");
    return org.springframework.http.ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=cuenta-" + id + ".csv")
        .body(csv.toString());
  }
}
