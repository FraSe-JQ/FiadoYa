package pe.fiadoya.shared.infrastructure;

import jakarta.persistence.*;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class Data {
  @PersistenceContext public EntityManager entityManager;

  public <T> T save(T entity) {
    entityManager.persist(entity);
    return entity;
  }

  public <T> T get(Class<T> type, Long id) {
    T e = entityManager.find(type, id);
    if (e == null) throw new NoSuchElementException("Recurso no encontrado");
    return e;
  }

  public <T> T lock(Class<T> type, Long id) {
    T e = entityManager.find(type, id, LockModeType.PESSIMISTIC_WRITE);
    if (e == null) throw new NoSuchElementException("Recurso no encontrado");
    // Authorization can have loaded this entity before the lock. Refresh after acquiring
    // it so access concurrent transaction never validates against the old credit balance.
    entityManager.refresh(e, LockModeType.PESSIMISTIC_WRITE);
    return e;
  }

  public <T> List<T> list(Class<T> type, String clause, Object... args) {
    var q =
        entityManager.createQuery("select e from " + type.getSimpleName() + " e " + clause, type);
    for (int i = 0; i < args.length; i++) q.setParameter(i + 1, args[i]);
    return q.getResultList();
  }

  public <T> Optional<T> first(Class<T> type, String clause, Object... args) {
    return list(type, clause, args).stream().findFirst();
  }
}
