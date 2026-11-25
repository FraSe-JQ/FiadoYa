package pe.fiadoya.payments.application;

import jakarta.validation.constraints.*;
import java.math.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.fiadoya.finance.domain.FinanceEngine;
import pe.fiadoya.iam.application.Access;
import pe.fiadoya.sales.application.SalesService;
import pe.fiadoya.shared.application.OperationalClock;
import pe.fiadoya.shared.infrastructure.*;
import pe.fiadoya.shared.infrastructure.Model.*;

@Service
@Transactional
public class PaymentService {
  private final Data data;
  private final Access access;
  private final OperationalClock clock;
  private final SalesService sales;

  public PaymentService(Data data, Access access, OperationalClock clock, SalesService sales) {
    this.data = data;
    this.access = access;
    this.clock = clock;
    this.sales = sales;
  }

  public record Input(
      @NotNull Long accountId,
      @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2) BigDecimal received,
      @NotBlank @Pattern(regexp = "CASH|TRANSFER|CARD") String method) {}

  public record Due(Obligation obligation, int lateDays, BigDecimal penalty, BigDecimal total) {}

  public record Quote(
      List<Due> obligations,
      BigDecimal penalty,
      BigDecimal interest,
      BigDecimal principal,
      BigDecimal total) {}

  public BigDecimal penalty(Obligation o) {
    Purchase p = data.get(Purchase.class, o.purchaseId);
    return o.paid
        ? FinanceEngine.money(BigDecimal.ZERO)
        : FinanceEngine.penalty(
            o.amount, o.dueDate, clock.now().toLocalDate(), sales.snapshot(p).penalty());
  }

  public Quote quote(Long storeId, Long accountId) {
    access.account(storeId, accountId, false);
    List<Due> due = new ArrayList<>();
    BigDecimal penalty = BigDecimal.ZERO, interest = BigDecimal.ZERO, principal = BigDecimal.ZERO;
    for (Obligation o :
        data.list(
            Obligation.class,
            "where e.accountId=?1 and e.paid=false and e.dueDate<=?2 order by e.dueDate,e.id",
            accountId,
            clock.now().toLocalDate())) {
      BigDecimal m = penalty(o);
      due.add(
          new Due(
              o,
              Math.max(0, FinanceEngine.days(o.dueDate, clock.now().toLocalDate())),
              m,
              o.amount.add(m)));
      penalty = penalty.add(m);
      interest = interest.add(o.interest);
      principal = principal.add(o.principal);
    }
    return new Quote(due, penalty, interest, principal, penalty.add(interest).add(principal));
  }

  public Payment pay(Long storeId, Input input, String key) {
    SalesService.validKey(key);
    access.account(storeId, input.accountId(), true);
    Account account = data.lock(Account.class, input.accountId());
    var existing =
        data.first(Payment.class, "where e.storeId=?1 and e.requestKey=?2", storeId, key);
    if (existing.isPresent()) {
      if (!existing.get().requestHash.equals(sales.hash(input)))
        throw new IllegalArgumentException("Clave de pago reutilizada con otros datos");
      return existing.get();
    }
    Quote q = quote(storeId, account.id);
    if (q.total().signum() == 0)
      throw new IllegalArgumentException(
          "No existen obligaciones exigibles; el prepago requiere un procedimiento distinto");
    if (input.received().compareTo(q.total()) != 0)
      throw new IllegalArgumentException(
          "Se requiere el importe exacto de la liquidación: " + FinanceEngine.money(q.total()));
    new pe.fiadoya.finance.domain.PaymentAllocationCalculator()
        .allocate(input.received(), q.penalty(), q.interest(), q.principal());
    Payment p = new Payment();
    p.storeId = storeId;
    p.accountId = account.id;
    p.actorId = access.user().id;
    p.requestKey = key;
    p.requestHash = sales.hash(input);
    p.paidAt = clock.now().toLocalDate();
    p.received = input.received();
    p.penalty = q.penalty();
    p.interest = q.interest();
    p.principal = q.principal();
    p.method = input.method();
    account.used = account.used.subtract(q.principal());
    if (account.used.signum() < 0) throw new IllegalStateException("Saldo negativo");
    p.remainingCapital = account.used;
    data.save(p);
    for (Due due : q.obligations()) {
      Obligation o = due.obligation();
      o.paid = true;
      Allocation x = new Allocation();
      x.paymentId = p.id;
      x.obligationId = o.id;
      x.penalty = due.penalty();
      x.interest = o.interest;
      x.principal = o.principal;
      data.save(x);
    }
    Audit audit = new Audit();
    audit.actorId = access.user().id;
    audit.storeId = storeId;
    audit.action = "PAYMENT";
    audit.resource = p.id.toString();
    audit.occurredAt = java.time.Instant.now();
    data.save(audit);
    return p;
  }
}
