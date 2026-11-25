package pe.fiadoya.stores.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
public class StoreController {
  private final Data data;
  private final Access access;
  private final CreditService creditService;

  public StoreController(Data data, Access access, CreditService creditService) {
    this.data = data;
    this.access = access;
    this.creditService = creditService;
  }

  public record StoreInput(
      @NotBlank @Size(max = 150) String name,
      @NotBlank @Size(max = 100) String businessType,
      @Size(max = 250) String address,
      @Pattern(regexp = "[+0-9 ()-]{0,25}") String phone,
      boolean active) {}

  @GetMapping("/stores")
  public Object stores() {
    return data.list(Store.class, "").stream()
        .filter(
            s -> {
              try {
                access.store(s.id, false);
                return true;
              } catch (org.springframework.security.access.AccessDeniedException e) {
                return false;
              }
            })
        .toList();
  }

  @PostMapping("/stores")
  public Store createStore(@Valid @RequestBody StoreInput r) {
    access.platformOnly();
    Store store = copy(new Store(), r);
    data.save(store);
    audit(store.id, "CREATE_STORE", store.id);
    return store;
  }

  @PutMapping("/stores/{id}")
  public Store editStore(@PathVariable Long id, @Valid @RequestBody StoreInput r) {
    access.store(id, true);
    if (!access.platform() && !r.active())
      throw new org.springframework.security.access.AccessDeniedException(
          "Solo plataforma puede desactivar tiendas");
    Store store = copy(data.get(Store.class, id), r);
    audit(id, "EDIT_STORE", id);
    return store;
  }

  private Store copy(Store s, StoreInput r) {
    s.name = r.name();
    s.businessType = r.businessType();
    s.address = r.address();
    s.phone = r.phone();
    s.active = r.active();
    return s;
  }

  @GetMapping("/users")
  public Object users() {
    access.platformOnly();
    return data.list(User.class, "").stream().map(pe.fiadoya.iam.api.AuthController::view).toList();
  }

  @PostMapping("/stores/{id}/admins/{userId}")
  public void assign(@PathVariable Long id, @PathVariable Long userId) {
    access.platformOnly();
    data.get(Store.class, id);
    User u = data.get(User.class, userId);
    if (u.role.equals("PLATFORM_ADMIN"))
      throw new IllegalArgumentException("Seleccione un usuario de comercio");
    u.role = "STORE_ADMIN";
    if (data.first(Membership.class, "where e.storeId=?1 and e.userId=?2", id, userId).isEmpty()) {
      Membership m = new Membership();
      m.storeId = id;
      m.userId = userId;
      data.save(m);
    }
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
