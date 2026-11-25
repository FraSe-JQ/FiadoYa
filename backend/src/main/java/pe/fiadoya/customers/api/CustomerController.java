package pe.fiadoya.customers.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.fiadoya.accounts.application.CreditService;
import pe.fiadoya.finance.domain.FinanceEngine.Terms;
import pe.fiadoya.iam.application.Access;
import pe.fiadoya.shared.infrastructure.*;
import pe.fiadoya.shared.infrastructure.Model.*;

@RestController
@RequestMapping("/api")
@Transactional
public class CustomerController {
  private final Data data;
  private final Access access;
  private final CreditService creditService;

  public CustomerController(Data data, Access access, CreditService creditService) {
    this.data = data;
    this.access = access;
    this.creditService = creditService;
  }

  public record CustomerInput(
      Long userId,
      @NotBlank @Pattern(regexp = "DNI|RUC|CE") String documentType,
      @NotBlank @Pattern(regexp = "[0-9A-Za-z]{8,12}") String documentNumber,
      @NotBlank @Size(max = 100) String firstName,
      @NotBlank @Size(max = 100) String lastName,
      @Email String email,
      @Size(max = 25) String phone,
      @Size(max = 250) String address,
      boolean consent,
      boolean active,
      @NotNull Terms terms) {}

  @GetMapping("/stores/{storeId}/customers")
  public Object customers(@PathVariable Long storeId) {
    access.store(storeId, false);
    boolean canManageStore = access.managesStore(storeId);
    return data.list(Customer.class, "where e.storeId=?1 order by e.id desc", storeId).stream()
        .filter(c -> canManageStore || access.user().id.equals(c.userId))
        .map(
            c -> {
              Account account =
                  data.first(Account.class, "where e.customerId=?1", c.id).orElseThrow();
              return Map.of(
                  "customer",
                  c,
                  "account",
                  account,
                  "terms",
                  creditService.terms(creditService.latest(account.id)));
            })
        .toList();
  }

  @PostMapping("/stores/{storeId}/customers")
  public Object customer(@PathVariable Long storeId, @Valid @RequestBody CustomerInput r) {
    access.store(storeId, true);
    validateDocument(r);
    Customer c = new Customer();
    c.storeId = storeId;
    copy(c, r);
    data.save(c);
    Account account = new Account();
    account.storeId = storeId;
    account.customerId = c.id;
    account.currency = r.terms().currency();
    data.save(account);
    creditService.version(account.id, r.terms());
    audit(storeId, "CREATE_CUSTOMER", c.id);
    return Map.of("customer", c, "account", account);
  }

  @PutMapping("/stores/{storeId}/customers/{id}")
  public Object editCustomer(
      @PathVariable Long storeId, @PathVariable Long id, @Valid @RequestBody CustomerInput r) {
    access.store(storeId, true);
    validateDocument(r);
    Customer c = data.get(Customer.class, id);
    if (!c.storeId.equals(storeId))
      throw new org.springframework.security.access.AccessDeniedException("Cliente ajeno");
    Account account =
        data.lock(
            Account.class, data.first(Account.class, "where e.customerId=?1", id).orElseThrow().id);
    if (!r.terms().currency().equals(account.currency))
      throw new IllegalArgumentException("La moneda de una cuenta es inmutable");
    if (r.terms().creditLimit().compareTo(account.used) < 0)
      throw new IllegalArgumentException("Límite inferior al crédito utilizado");
    copy(c, r);
    creditService.version(account.id, r.terms());
    audit(storeId, "VERSION_TERMS", id);
    return c;
  }

  private void validateDocument(CustomerInput r) {
    if ((r.documentType().equals("DNI") && !r.documentNumber().matches("[0-9]{8}"))
        || (r.documentType().equals("RUC") && !r.documentNumber().matches("[0-9]{11}")))
      throw new IllegalArgumentException(
          "DNI: 8 dataígitos; RUC: 11 dataígitos. Validación solo de formato");
    if (r.userId() != null) {
      User u = data.get(User.class, r.userId());
      if (!u.email.equalsIgnoreCase(r.email()) || !r.consent())
        throw new IllegalArgumentException(
            "Vincular usuario requiere correo coincidente y consentimiento");
    }
  }

  private void copy(Customer c, CustomerInput r) {
    c.userId = r.userId();
    c.documentType = r.documentType();
    c.documentNumber = r.documentNumber();
    c.firstName = r.firstName();
    c.lastName = r.lastName();
    c.email = r.email();
    c.phone = r.phone();
    c.address = r.address();
    c.consent = r.consent();
    c.active = r.active();
  }

  private void audit(Long storeId, String action, Long id) {
    Audit x = new Audit();
    x.storeId = storeId;
    x.actorId = access.user().id;
    x.action = action;
    x.resource = id.toString();
    x.occurredAt = java.time.Instant.now();
    data.save(x);
  }
}
