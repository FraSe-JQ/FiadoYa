package pe.fiadoya;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.*;
import pe.fiadoya.accounts.application.CreditService;
import pe.fiadoya.finance.domain.FinanceEngine.*;
import pe.fiadoya.payments.application.PaymentService;
import pe.fiadoya.sales.application.SalesService;
import pe.fiadoya.shared.application.OperationalClock;
import pe.fiadoya.shared.infrastructure.*;
import pe.fiadoya.shared.infrastructure.Model.*;

@SpringBootTest(
    properties = {
      "spring.profiles.active=demo",
      "app.jwt-secret=integration-test-secret-at-least-32-characters",
      "app.bootstrap-email=",
      "app.bootstrap-password="
    })
@AutoConfigureMockMvc
@Testcontainers
class WorkflowIntegrationTest {
  @Container static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4.7");

  @DynamicPropertySource
  static void database(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mysql::getJdbcUrl);
    registry.add("spring.datasource.username", mysql::getUsername);
    registry.add("spring.datasource.password", mysql::getPassword);
  }

  @Autowired Data data;
  @Autowired CreditService credit;
  @Autowired SalesService sales;
  @Autowired PaymentService payments;
  @Autowired OperationalClock clock;
  @Autowired TransactionTemplate transaction;
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired pe.fiadoya.reporting.application.CycleService cycles;
  User administrator;
  Store store;
  Account account;
  Product rice, gas;
  Terms terms;

  @BeforeEach
  void fixture() {
    clock.set(LocalDateTime.of(2026, 10, 6, 10, 38));
    transaction.executeWithoutResult(
        status -> {
          administrator = new User();
          administrator.email = UUID.randomUUID() + "@test.invalid";
          administrator.name = "Administrador de prueba";
          administrator.passwordHash = "not-used-in-test";
          administrator.role = "STORE_ADMIN";
          data.save(administrator);
          store = new Store();
          store.name = "Comercio prueba";
          store.businessType = "Bodega";
          data.save(store);
          Membership membership = new Membership();
          membership.storeId = store.id;
          membership.userId = administrator.id;
          data.save(membership);
          Customer customer = new Customer();
          customer.storeId = store.id;
          customer.firstName = "Juan";
          customer.lastName = "Pérez";
          customer.documentType = "DNI";
          customer.documentNumber = "72839401";
          data.save(customer);
          account = new Account();
          account.storeId = store.id;
          account.customerId = customer.id;
          account.currency = "PEN";
          data.save(account);
          terms =
              new Terms(
                  "PEN",
                  new BigDecimal("1000.00"),
                  3,
                  15,
                  LocalTime.of(23, 59),
                  25,
                  new Rate(new BigDecimal("24"), RateType.NOMINAL, 360, 30),
                  new Rate(new BigDecimal("35"), RateType.EFFECTIVE, 360, 0));
          credit.version(account.id, terms);
          rice = product("AR", "52.00");
          gas = product("GAS", "60.00");
        });
    authenticate(administrator);
  }

  @AfterEach
  void clearSecurity() {
    SecurityContextHolder.clearContext();
  }

  Product product(String code, String price) {
    Product product = new Product();
    product.storeId = store.id;
    product.code = code;
    product.name = code;
    product.currency = "PEN";
    product.cashPrice = new BigDecimal(price);
    product.creditPrice = new BigDecimal(price).add(BigDecimal.TEN);
    product.mode = "BOTH";
    product.unit = "unidad";
    return data.save(product);
  }

  void authenticate(User user) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.role))));
  }

  SalesService.Sale sale() {
    return new SalesService.Sale(
        account.id,
        Mode.INSTALLMENTS,
        3,
        List.of(
            new SalesService.Item(rice.id, new BigDecimal("2")),
            new SalesService.Item(gas.id, BigDecimal.ONE)));
  }

  @Test
  void completeAcademicPurchaseAndLatePaymentArePersistedAndIdempotent() {
    Purchase purchase = sales.purchase(store.id, sale(), "purchase-academic-key");
    assertEquals(0, new BigDecimal("164.00").compareTo(purchase.financed));
    assertEquals(0, new BigDecimal("166.07").compareTo(purchase.capitalized));
    assertEquals(purchase.id, sales.purchase(store.id, sale(), "purchase-academic-key").id);
    transaction.executeWithoutResult(
        status ->
            credit.version(
                account.id,
                new Terms(
                    "PEN",
                    new BigDecimal("1000"),
                    3,
                    15,
                    LocalTime.of(23, 59),
                    25,
                    new Rate(new BigDecimal("99"), RateType.EFFECTIVE, 360, 0),
                    new Rate(new BigDecimal("99"), RateType.EFFECTIVE, 360, 0))));
    clock.set(LocalDateTime.of(2026, 12, 6, 10, 0));
    PaymentService.Quote quote = payments.quote(store.id, account.id);
    assertEquals(0, new BigDecimal("58.12").compareTo(quote.total()));
    assertEquals(0, new BigDecimal("0.53").compareTo(quote.penalty()));
    assertEquals(1, quote.obligations().size());
    assertThrows(
        IllegalArgumentException.class,
        () ->
            payments.pay(
                store.id,
                new PaymentService.Input(account.id, new BigDecimal("58.00"), "CASH"),
                "insufficient-payment"));
    Payment payment =
        payments.pay(
            store.id,
            new PaymentService.Input(account.id, new BigDecimal("58.12"), "CASH"),
            "payment-academic-key");
    assertEquals(0, new BigDecimal("111.80").compareTo(payment.remainingCapital));
    assertEquals(
        payment.id,
        payments.pay(
                store.id,
                new PaymentService.Input(account.id, new BigDecimal("58.12"), "CASH"),
                "payment-academic-key")
            .id);
    transaction.executeWithoutResult(
        status -> {
          assertEquals(
              2, data.list(PurchaseItem.class, "where e.purchaseId=?1", purchase.id).size());
          assertEquals(3, data.list(Obligation.class, "where e.purchaseId=?1", purchase.id).size());
          assertEquals(1, data.list(Allocation.class, "where e.paymentId=?1", payment.id).size());
        });
  }

  @Test
  void concurrentPurchasesCannotExceedCreditLimit() throws Exception {
    transaction.executeWithoutResult(
        status ->
            credit.version(
                account.id,
                new Terms(
                    "PEN",
                    new BigDecimal("200"),
                    3,
                    15,
                    LocalTime.of(23, 59),
                    25,
                    terms.compensatory(),
                    terms.penalty())));
    ExecutorService executor = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    Callable<Boolean> operation =
        () -> {
          authenticate(administrator);
          start.await();
          try {
            sales.purchase(store.id, sale(), UUID.randomUUID().toString());
            return true;
          } catch (IllegalArgumentException rejected) {
            return false;
          } finally {
            SecurityContextHolder.clearContext();
          }
        };
    var first = executor.submit(operation);
    var second = executor.submit(operation);
    start.countDown();
    int successes =
        (first.get(30, TimeUnit.SECONDS) ? 1 : 0) + (second.get(30, TimeUnit.SECONDS) ? 1 : 0);
    executor.shutdown();
    assertEquals(1, successes);
    transaction.executeWithoutResult(
        status ->
            assertEquals(
                0, new BigDecimal("166.07").compareTo(data.get(Account.class, account.id).used)));
  }

  @Test
  void anotherStoreAndAnotherCustomerCannotReadAccount() throws Exception {
    Store other =
        transaction.execute(
            status -> {
              Store value = new Store();
              value.name = "Ajena";
              value.businessType = "Spa";
              return data.save(value);
            });
    mvc.perform(
            get("/api/stores/" + other.id + "/accounts/" + account.id)
                .with(authentication(SecurityContextHolder.getContext().getAuthentication())))
        .andExpect(status().isForbidden());
    User stranger =
        transaction.execute(
            status -> {
              User value = new User();
              value.email = UUID.randomUUID() + "@test.invalid";
              value.name = "Otro cliente";
              value.role = "CUSTOMER";
              value.passwordHash = "unused";
              return data.save(value);
            });
    authenticate(stranger);
    mvc.perform(
            get("/api/stores/" + store.id + "/accounts/" + account.id)
                .with(authentication(SecurityContextHolder.getContext().getAuthentication())))
        .andExpect(status().isForbidden());
  }

  @Test
  void unauthenticatedAndCsrfRequestsAreRejected() throws Exception {
    SecurityContextHolder.clearContext();
    mvc.perform(get("/api/stores")).andExpect(status().isUnauthorized());
    mvc.perform(post("/api/auth/register").contentType("application/json").content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void publicRegistrationCannotEscalateRoleAndLoginLogoutWorks() throws Exception {
    String email = UUID.randomUUID() + "@test.invalid";
    String body =
        json.writeValueAsString(
            Map.of(
                "email",
                email,
                "password",
                "Test-password-123",
                "name",
                "Nuevo usuario",
                "role",
                "PLATFORM_ADMIN"));
    mvc.perform(
            post("/api/auth/register").with(csrf()).contentType("application/json").content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("CUSTOMER"));
    var login =
        mvc.perform(
                post("/api/auth/login").with(csrf()).contentType("application/json").content(body))
            .andExpect(status().isOk())
            .andReturn();
    String cookie = login.getResponse().getHeader("Set-Cookie");
    assertTrue(cookie.contains("HttpOnly"));
    String token = cookie.substring("FIADOYA_SESSION=".length(), cookie.indexOf(';'));
    SecurityContextHolder.clearContext();
    mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
    mvc.perform(post("/api/auth/logout").with(csrf()).header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
    mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void incompatibleCurrencyModalityAndTermsAreRejected() {
    transaction.executeWithoutResult(
        status -> {
          Product product = data.get(Product.class, rice.id);
          product.currency = "USD";
        });
    assertThrows(
        IllegalArgumentException.class,
        () -> sales.purchase(store.id, sale(), "currency-error-key"));
    transaction.executeWithoutResult(
        status -> {
          Product product = data.get(Product.class, rice.id);
          product.currency = "PEN";
          product.mode = "MONTH_END";
        });
    assertThrows(
        IllegalArgumentException.class,
        () -> sales.purchase(store.id, sale(), "modality-error-key"));
  }

  @Test
  void laterBillingCycleIncludesInstallmentsFromPreviousPurchasesAndIsIdempotent() {
    Purchase purchase = sales.purchase(store.id, sale(), "cycle-installment-key");
    LocalDateTime novemberCutoff = LocalDateTime.of(2026, 11, 15, 23, 59);
    assertEquals(2, cycles.close(store.id, novemberCutoff));
    assertEquals(0, cycles.close(store.id, novemberCutoff));
    transaction.executeWithoutResult(
        status -> {
          BillingCycle november =
              data.first(
                      BillingCycle.class,
                      "where e.accountId=?1 and e.cutoff=?2",
                      account.id,
                      novemberCutoff)
                  .orElseThrow();
          try {
            var snapshot = json.readTree(november.snapshotJson);
            assertEquals(1, snapshot.get("obligations").size());
            assertEquals("2026-11-25", snapshot.get("obligations").get(0).get("dueDate").asText());
            assertEquals(
                purchase.id.longValue(), snapshot.get("purchases").get(0).get("id").asLong());
          } catch (java.io.IOException exception) {
            throw new IllegalStateException(exception);
          }
        });
  }

  @Test
  void paymentOnDueDateHasNoPenaltyAndCannotBeRepeatedWithAnotherKey() {
    sales.purchase(store.id, sale(), "on-time-purchase-key");
    clock.set(LocalDateTime.of(2026, 11, 25, 10, 0));
    var quote = payments.quote(store.id, account.id);
    assertEquals(0, quote.penalty().signum());
    assertEquals(0, new BigDecimal("57.59").compareTo(quote.total()));
    payments.pay(
        store.id,
        new PaymentService.Input(account.id, quote.total(), "CASH"),
        "on-time-payment-key");
    assertThrows(
        IllegalArgumentException.class,
        () ->
            payments.pay(
                store.id,
                new PaymentService.Input(account.id, quote.total(), "CASH"),
                "duplicate-new-payment-key"));
  }

  @Test
  void inactiveStoreCannotSellEvenForPlatformAdministrator() {
    transaction.executeWithoutResult(status -> data.get(Store.class, store.id).active = false);
    administrator.role = "PLATFORM_ADMIN";
    authenticate(administrator);
    assertThrows(
        IllegalArgumentException.class,
        () -> sales.purchase(store.id, sale(), "inactive-store-purchase-key"));
  }

  @Test
  void storeAdministratorWhoIsCustomerElsewhereCannotReadOtherCustomersThere() throws Exception {
    Long[] resources =
        transaction.execute(
            status -> {
              Store otherStore = new Store();
              otherStore.name = "Otro comercio";
              otherStore.businessType = "Peluquería";
              data.save(otherStore);
              Customer linked = new Customer();
              linked.storeId = otherStore.id;
              linked.userId = administrator.id;
              linked.firstName = "Administrador como cliente";
              linked.lastName = "Prueba";
              linked.documentType = "DNI";
              linked.documentNumber = "11111111";
              data.save(linked);
              Account linkedAccount = new Account();
              linkedAccount.storeId = otherStore.id;
              linkedAccount.customerId = linked.id;
              linkedAccount.currency = "PEN";
              data.save(linkedAccount);
              credit.version(linkedAccount.id, terms);
              Customer victim = new Customer();
              victim.storeId = otherStore.id;
              victim.firstName = "Otro cliente";
              victim.lastName = "Privado";
              victim.documentType = "DNI";
              victim.documentNumber = "22222222";
              data.save(victim);
              Account victimAccount = new Account();
              victimAccount.storeId = otherStore.id;
              victimAccount.customerId = victim.id;
              victimAccount.currency = "PEN";
              data.save(victimAccount);
              credit.version(victimAccount.id, terms);
              return new Long[] {otherStore.id, linkedAccount.id, victimAccount.id};
            });
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    mvc.perform(
            get("/api/stores/" + resources[0] + "/customers").with(authentication(authentication)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
    mvc.perform(
            get("/api/stores/" + resources[0] + "/accounts/" + resources[1])
                .with(authentication(authentication)))
        .andExpect(status().isOk());
    mvc.perform(
            get("/api/stores/" + resources[0] + "/accounts/" + resources[2])
                .with(authentication(authentication)))
        .andExpect(status().isForbidden());
  }

  @Test
  void multipleDueObligationsAreFullySettledWithoutFuturePenalty() {
    sales.purchase(store.id, sale(), "multiple-due-purchase");
    clock.set(LocalDateTime.of(2026, 12, 26, 10, 0));
    var quote = payments.quote(store.id, account.id);
    assertEquals(2, quote.obligations().size());
    Payment payment =
        payments.pay(
            store.id,
            new PaymentService.Input(account.id, quote.total(), "TRANSFER"),
            "multiple-due-payment");
    assertEquals(0, new BigDecimal("56.45").compareTo(payment.remainingCapital));
  }
}
