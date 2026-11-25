package pe.fiadoya.iam.application;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pe.fiadoya.shared.infrastructure.*;
import pe.fiadoya.shared.infrastructure.Model.*;

@Component
public class Access {
  private final Data data;

  public Access(Data data) {
    this.data = data;
  }

  public User user() {
    return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  public boolean platform() {
    return user().role.equals("PLATFORM_ADMIN");
  }

  public void platformOnly() {
    if (!platform()) throw new AccessDeniedException("Plataforma");
  }

  /** El rol global nunca sustituye la membresía del comercio que se está consultando. */
  public boolean managesStore(Long storeId) {
    return platform()
        || (user().role.equals("STORE_ADMIN")
            && data.first(
                    Membership.class, "where e.storeId=?1 and e.userId=?2", storeId, user().id)
                .isPresent());
  }

  public Store store(Long id, boolean write) {
    Store store = data.get(Store.class, id);
    if (platform()) return store;
    User currentUser = user();
    boolean admin = managesStore(id);
    boolean customer =
        !write
            && data.first(Customer.class, "where e.storeId=?1 and e.userId=?2", id, currentUser.id)
                .isPresent();
    if (!admin && !customer) throw new AccessDeniedException("Tienda ajena");
    if (write && !store.active)
      throw new IllegalArgumentException("El establecimiento está desactivado");
    return store;
  }

  public Account account(Long storeId, Long id, boolean write) {
    store(storeId, write);
    Account account = data.get(Account.class, id);
    if (!account.storeId.equals(storeId)) throw new AccessDeniedException("Cuenta ajena");
    if (!managesStore(storeId)
        && !user().id.equals(data.get(Customer.class, account.customerId).userId))
      throw new AccessDeniedException("Cuenta ajena");
    return account;
  }
}
