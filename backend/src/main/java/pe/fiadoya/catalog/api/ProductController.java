package pe.fiadoya.catalog.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.fiadoya.accounts.application.CreditService;
import pe.fiadoya.iam.application.Access;
import pe.fiadoya.shared.infrastructure.*;
import pe.fiadoya.shared.infrastructure.Model.*;

@RestController
@RequestMapping("/api")
@Transactional
public class ProductController {
  private final Data data;
  private final Access access;
  private final CreditService creditService;

  public ProductController(Data data, Access access, CreditService creditService) {
    this.data = data;
    this.access = access;
    this.creditService = creditService;
  }

  public record ProductInput(
      @NotBlank @Size(max = 50) String code,
      @NotBlank @Size(max = 150) String name,
      @Size(max = 250) String description,
      @Size(max = 100) String supplier,
      @Size(max = 100) String brand,
      @NotBlank @Size(max = 50) String unit,
      @Size(max = 1000) @Pattern(regexp = "(|https://[^\\s]+|/[^\\s]*)") String imageUrl,
      @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) @NotNull BigDecimal cashPrice,
      @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) @NotNull BigDecimal creditPrice,
      @NotBlank @Pattern(regexp = "PEN|USD") String currency,
      @NotBlank @Pattern(regexp = "BOTH|MONTH_END|INSTALLMENTS") String mode,
      boolean active) {}

  @GetMapping("/stores/{storeId}/products")
  public Object products(@PathVariable Long storeId, @RequestParam(defaultValue = "") String q) {
    access.store(storeId, false);
    return data.list(Product.class, "where e.storeId=?1 order by e.name", storeId).stream()
        .filter(
            p -> (p.name + " " + p.code + " " + p.brand).toLowerCase().contains(q.toLowerCase()))
        .toList();
  }

  @PostMapping("/stores/{storeId}/products")
  public Product product(@PathVariable Long storeId, @Valid @RequestBody ProductInput r) {
    access.store(storeId, true);
    Product p = new Product();
    p.storeId = storeId;
    copy(p, r);
    data.save(p);
    audit(storeId, "CREATE_PRODUCT", p.id);
    return p;
  }

  @PutMapping("/stores/{storeId}/products/{id}")
  public Product editProduct(
      @PathVariable Long storeId, @PathVariable Long id, @Valid @RequestBody ProductInput r) {
    access.store(storeId, true);
    Product p = data.get(Product.class, id);
    if (!p.storeId.equals(storeId))
      throw new org.springframework.security.access.AccessDeniedException("Producto ajeno");
    copy(p, r);
    audit(storeId, "EDIT_PRODUCT", id);
    return p;
  }

  private void copy(Product p, ProductInput r) {
    p.code = r.code();
    p.name = r.name();
    p.description = r.description();
    p.supplier = r.supplier();
    p.brand = r.brand();
    p.unit = r.unit();
    p.imageUrl = r.imageUrl();
    p.cashPrice = r.cashPrice();
    p.creditPrice = r.creditPrice();
    p.currency = r.currency();
    p.mode = r.mode();
    p.active = r.active();
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
