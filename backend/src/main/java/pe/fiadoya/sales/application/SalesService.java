package pe.fiadoya.sales.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.fiadoya.accounts.application.CreditService;
import pe.fiadoya.finance.domain.FinanceEngine;
import pe.fiadoya.finance.domain.FinanceEngine.*;
import pe.fiadoya.iam.application.Access;
import pe.fiadoya.sales.domain.FinancingPolicy;
import pe.fiadoya.shared.application.OperationalClock;
import pe.fiadoya.shared.infrastructure.*;
import pe.fiadoya.shared.infrastructure.Model.*;

@Service
@Transactional
public class SalesService {
  private final Data data;
  private final Access access;
  private final CreditService credit;
  private final OperationalClock clock;
  private final ObjectMapper json;

  public SalesService(
      Data data, Access access, CreditService credit, OperationalClock clock, ObjectMapper json) {
    this.data = data;
    this.access = access;
    this.credit = credit;
    this.clock = clock;
    this.json = json;
  }

  public record Item(
      @NotNull Long productId,
      @NotNull @DecimalMin("0.001") @DecimalMax("100000") @Digits(integer = 6, fraction = 3)
          BigDecimal quantity) {}

  public record Sale(
      @NotNull Long accountId,
      @NotNull Mode mode,
      @Min(1) @Max(120) int months,
      @NotEmpty @Size(max = 100) List<@NotNull @Valid Item> items) {}

  public String serialize(Object x) {
    try {
      return json.writeValueAsString(x);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public Terms snapshot(Purchase p) {
    try {
      return json.readValue(p.termsJson, Terms.class);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public String hash(Object o) {
    try {
      return java.util.HexFormat.of()
          .formatHex(
              java.security.MessageDigest.getInstance("SHA-256")
                  .digest(serialize(o).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public Schedule simulate(Long storeId, Sale input) {
    return prepare(storeId, input, false).schedule();
  }

  record Prepared(Account account, Terms terms, List<PurchaseItem> items, Schedule schedule) {}

  private Prepared prepare(Long storeId, Sale input, boolean lock) {
    access.account(storeId, input.accountId(), true);
    if (!data.get(Store.class, storeId).active)
      throw new IllegalArgumentException("El establecimiento está desactivado");
    Account account =
        lock
            ? data.lock(Account.class, input.accountId())
            : data.get(Account.class, input.accountId());
    if (!data.get(Customer.class, account.customerId).active)
      throw new IllegalArgumentException("Cliente inactivo");
    Terms terms = credit.terms(credit.latest(account.id));
    List<PurchaseItem> items = new ArrayList<>();
    BigDecimal principal = BigDecimal.ZERO;
    if (input.mode() == Mode.MONTH_END && input.months() != 1)
      throw new IllegalArgumentException("Pago único requiere plazo 1");
    for (Item item : input.items()) {
      Product p = data.get(Product.class, item.productId());
      if (!p.storeId.equals(storeId) || !p.active)
        throw new IllegalArgumentException("Producto inactivo o ajeno a la tienda");
      if (!p.currency.equals(account.currency))
        throw new IllegalArgumentException("Monedas incompatibles");
      if (!p.mode.equals("BOTH") && !p.mode.equals(input.mode().name()))
        throw new IllegalArgumentException("Modalidad no permitida por el producto");
      PurchaseItem pi = new PurchaseItem();
      pi.productId = p.id;
      pi.productName = p.name;
      pi.quantity = item.quantity();
      pi.cashPrice = p.cashPrice;
      pi.creditPrice = p.creditPrice;
      pi.unitPrice = FinancingPolicy.unitPrincipal(p.cashPrice, p.creditPrice);
      pi.subtotal = FinanceEngine.money(pi.unitPrice.multiply(pi.quantity));
      items.add(pi);
      principal = principal.add(pi.subtotal);
    }
    Schedule schedule =
        FinanceEngine.simulate(
            FinanceEngine.money(principal), clock.now(), terms, input.mode(), input.months());
    if (account.used.add(schedule.capitalized()).compareTo(terms.creditLimit()) > 0)
      throw new IllegalArgumentException("La compra y su gracia exceden el crédito disponible");
    checkLimit(terms.currency(), "COMPENSATORY", terms.compensatory());
    checkLimit(terms.currency(), "PENALTY", terms.penalty());
    return new Prepared(account, terms, items, schedule);
  }

  private void checkLimit(String currency, String kind, Rate rate) {
    for (RateLimit l :
        data.list(RateLimit.class, "where e.currency=?1 and e.kind=?2", currency, kind))
      if (!clock.now().toLocalDate().isBefore(l.validFrom)
          && !clock.now().toLocalDate().isAfter(l.validUntil)
          && rate.effective(360).movePointRight(2).compareTo(l.maxTeaPercent) > 0)
        throw new IllegalArgumentException("La tasa supera el límite configurado vigente");
  }

  public Purchase purchase(Long storeId, Sale input, String key) {
    validKey(key);
    access.account(storeId, input.accountId(), true);
    data.lock(Account.class, input.accountId());
    Optional<Purchase> existing =
        data.first(Purchase.class, "where e.storeId=?1 and e.requestKey=?2", storeId, key);
    if (existing.isPresent()) {
      if (!existing.get().requestHash.equals(hash(input)))
        throw new IllegalArgumentException("Clave de operación reutilizada con datos diferentes");
      return existing.get();
    }
    Prepared prep = prepare(storeId, input, true);
    Schedule s = prep.schedule();
    Purchase p = new Purchase();
    p.storeId = storeId;
    p.accountId = input.accountId();
    p.actorId = access.user().id;
    p.requestKey = key;
    p.requestHash = hash(input);
    p.purchasedAt = clock.now();
    p.mode = input.mode().name();
    p.months = input.months();
    p.currency = prep.terms().currency();
    p.financed = s.financed();
    p.capitalized = s.capitalized();
    p.graceInterest = s.graceInterest();
    p.cutoff = s.cycle().cutoff();
    p.periodZero = s.periodZero();
    p.termsJson = serialize(prep.terms());
    data.save(p);
    for (PurchaseItem i : prep.items()) {
      i.purchaseId = p.id;
      data.save(i);
    }
    for (Row r : s.rows()) {
      Obligation o = new Obligation();
      o.storeId = storeId;
      o.accountId = p.accountId;
      o.purchaseId = p.id;
      o.number = r.number();
      o.dueDate = r.dueDate();
      o.opening = r.opening();
      o.interest = r.interest();
      o.principal = r.principal();
      o.amount = r.amount();
      o.closing = r.closing();
      data.save(o);
    }
    prep.account().used = prep.account().used.add(s.capitalized());
    Audit audit = new Audit();
    audit.actorId = access.user().id;
    audit.storeId = storeId;
    audit.action = "PURCHASE";
    audit.resource = p.id.toString();
    audit.occurredAt = java.time.Instant.now();
    data.save(audit);
    return p;
  }

  public static void validKey(String key) {
    if (key == null || !key.matches("[A-Za-z0-9_-]{16,100}"))
      throw new IllegalArgumentException(
          "Idempotency-Key requiere 16–100 caracteres alfanuméricos");
  }
}
