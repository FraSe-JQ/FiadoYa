package pe.fiadoya.shared.infrastructure;

import jakarta.persistence.*;

public final class Model {
  @Entity(name = "User")
  @Table(name = "users")
  public static class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "email")
    public String email;

    @Column(name = "password_hash")
    public String passwordHash;

    @Column(name = "name")
    public String name;

    @Column(name = "role")
    public String role;

    @Column(name = "active")
    public boolean active = true;
  }

  @Entity(name = "Session")
  @Table(name = "auth_sessions")
  public static class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "user_id")
    public Long userId;

    @Column(name = "token_id")
    public String tokenId;

    @Column(name = "expires_at")
    public java.time.Instant expiresAt;

    @Column(name = "revoked")
    public boolean revoked;
  }

  @Entity(name = "Store")
  @Table(name = "stores")
  public static class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "name")
    public String name;

    @Column(name = "business_type")
    public String businessType;

    @Column(name = "address")
    public String address;

    @Column(name = "phone")
    public String phone;

    @Column(name = "active")
    public boolean active = true;
  }

  @Entity(name = "Membership")
  @Table(name = "store_memberships")
  public static class Membership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "store_id")
    public Long storeId;

    @Column(name = "user_id")
    public Long userId;
  }

  @Entity(name = "Customer")
  @Table(name = "customers")
  public static class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "store_id")
    public Long storeId;

    @Column(name = "user_id")
    public Long userId;

    @Column(name = "document_type")
    public String documentType;

    @Column(name = "document_number")
    public String documentNumber;

    @Column(name = "first_name")
    public String firstName;

    @Column(name = "last_name")
    public String lastName;

    @Column(name = "email")
    public String email;

    @Column(name = "phone")
    public String phone;

    @Column(name = "address")
    public String address;

    @Column(name = "consent")
    public boolean consent;

    @Column(name = "active")
    public boolean active = true;
  }

  @Entity(name = "Account")
  @Table(name = "credit_accounts")
  public static class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "store_id")
    public Long storeId;

    @Column(name = "customer_id")
    public Long customerId;

    @Column(name = "currency")
    public String currency;

    @Column(name = "used", precision = 24, scale = 10)
    public java.math.BigDecimal used = java.math.BigDecimal.ZERO;
  }

  @Entity(name = "CreditTerms")
  @Table(name = "credit_terms")
  public static class CreditTerms {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "account_id")
    public Long accountId;

    @Column(name = "currency")
    public String currency;

    @Column(name = "credit_limit", precision = 24, scale = 10)
    public java.math.BigDecimal creditLimit;

    @Column(name = "max_months")
    public int maxMonths;

    @Column(name = "cutoff_day")
    public int cutoffDay;

    @Column(name = "cutoff_time")
    public java.time.LocalTime cutoffTime;

    @Column(name = "payment_day")
    public int paymentDay;

    @Column(name = "compensatory_percent", precision = 24, scale = 10)
    public java.math.BigDecimal compensatoryPercent;

    @Column(name = "compensatory_type")
    public String compensatoryType;

    @Column(name = "compensatory_period")
    public int compensatoryPeriod;

    @Column(name = "compensatory_capitalization")
    public int compensatoryCapitalization;

    @Column(name = "penalty_percent", precision = 24, scale = 10)
    public java.math.BigDecimal penaltyPercent;

    @Column(name = "penalty_type")
    public String penaltyType;

    @Column(name = "penalty_period")
    public int penaltyPeriod;

    @Column(name = "penalty_capitalization")
    public int penaltyCapitalization;

    @Column(name = "created_at")
    public java.time.Instant createdAt;
  }

  @Entity(name = "Product")
  @Table(name = "products")
  public static class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "store_id")
    public Long storeId;

    @Column(name = "code")
    public String code;

    @Column(name = "name")
    public String name;

    @Column(name = "description")
    public String description;

    @Column(name = "supplier")
    public String supplier;

    @Column(name = "brand")
    public String brand;

    @Column(name = "unit")
    public String unit;

    @Column(name = "image_url")
    public String imageUrl;

    @Column(name = "cash_price", precision = 24, scale = 10)
    public java.math.BigDecimal cashPrice;

    @Column(name = "credit_price", precision = 24, scale = 10)
    public java.math.BigDecimal creditPrice;

    @Column(name = "currency")
    public String currency;

    @Column(name = "mode")
    public String mode;

    @Column(name = "active")
    public boolean active = true;
  }

  @Entity(name = "Purchase")
  @Table(name = "purchases")
  public static class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "store_id")
    public Long storeId;

    @Column(name = "account_id")
    public Long accountId;

    @Column(name = "actor_id")
    public Long actorId;

    @Column(name = "request_key")
    public String requestKey;

    @Column(name = "request_hash")
    public String requestHash;

    @Column(name = "purchased_at")
    public java.time.LocalDateTime purchasedAt;

    @Column(name = "mode")
    public String mode;

    @Column(name = "months")
    public int months;

    @Column(name = "currency")
    public String currency;

    @Column(name = "financed", precision = 24, scale = 10)
    public java.math.BigDecimal financed;

    @Column(name = "capitalized", precision = 24, scale = 10)
    public java.math.BigDecimal capitalized;

    @Column(name = "grace_interest", precision = 24, scale = 10)
    public java.math.BigDecimal graceInterest;

    @Column(name = "cutoff")
    public java.time.LocalDateTime cutoff;

    @Column(name = "period_zero")
    public java.time.LocalDate periodZero;

    @Column(name = "terms_json", columnDefinition = "LONGTEXT")
    public String termsJson;
  }

  @Entity(name = "PurchaseItem")
  @Table(name = "purchase_items")
  public static class PurchaseItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "purchase_id")
    public Long purchaseId;

    @Column(name = "product_id")
    public Long productId;

    @Column(name = "product_name")
    public String productName;

    @Column(name = "quantity", precision = 24, scale = 10)
    public java.math.BigDecimal quantity;

    @Column(name = "cash_price", precision = 24, scale = 10)
    public java.math.BigDecimal cashPrice;

    @Column(name = "credit_price", precision = 24, scale = 10)
    public java.math.BigDecimal creditPrice;

    @Column(name = "unit_price", precision = 24, scale = 10)
    public java.math.BigDecimal unitPrice;

    @Column(name = "subtotal", precision = 24, scale = 10)
    public java.math.BigDecimal subtotal;
  }

  @Entity(name = "Obligation")
  @Table(name = "payment_obligations")
  public static class Obligation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "store_id")
    public Long storeId;

    @Column(name = "account_id")
    public Long accountId;

    @Column(name = "purchase_id")
    public Long purchaseId;

    @Column(name = "number")
    public int number;

    @Column(name = "due_date")
    public java.time.LocalDate dueDate;

    @Column(name = "opening", precision = 24, scale = 10)
    public java.math.BigDecimal opening;

    @Column(name = "interest", precision = 24, scale = 10)
    public java.math.BigDecimal interest;

    @Column(name = "principal", precision = 24, scale = 10)
    public java.math.BigDecimal principal;

    @Column(name = "amount", precision = 24, scale = 10)
    public java.math.BigDecimal amount;

    @Column(name = "closing", precision = 24, scale = 10)
    public java.math.BigDecimal closing;

    @Column(name = "paid")
    public boolean paid;
  }

  @Entity(name = "Payment")
  @Table(name = "payments")
  public static class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "store_id")
    public Long storeId;

    @Column(name = "account_id")
    public Long accountId;

    @Column(name = "actor_id")
    public Long actorId;

    @Column(name = "request_key")
    public String requestKey;

    @Column(name = "request_hash")
    public String requestHash;

    @Column(name = "paid_at")
    public java.time.LocalDate paidAt;

    @Column(name = "received", precision = 24, scale = 10)
    public java.math.BigDecimal received;

    @Column(name = "penalty", precision = 24, scale = 10)
    public java.math.BigDecimal penalty;

    @Column(name = "interest", precision = 24, scale = 10)
    public java.math.BigDecimal interest;

    @Column(name = "principal", precision = 24, scale = 10)
    public java.math.BigDecimal principal;

    @Column(name = "remaining_capital", precision = 24, scale = 10)
    public java.math.BigDecimal remainingCapital;

    @Column(name = "method")
    public String method;
  }

  @Entity(name = "Allocation")
  @Table(name = "payment_allocations")
  public static class Allocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "payment_id")
    public Long paymentId;

    @Column(name = "obligation_id")
    public Long obligationId;

    @Column(name = "penalty", precision = 24, scale = 10)
    public java.math.BigDecimal penalty;

    @Column(name = "interest", precision = 24, scale = 10)
    public java.math.BigDecimal interest;

    @Column(name = "principal", precision = 24, scale = 10)
    public java.math.BigDecimal principal;
  }

  @Entity(name = "BillingCycle")
  @Table(name = "billing_cycles")
  public static class BillingCycle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "store_id")
    public Long storeId;

    @Column(name = "account_id")
    public Long accountId;

    @Column(name = "cutoff")
    public java.time.LocalDateTime cutoff;

    @Column(name = "generated_at")
    public java.time.Instant generatedAt;

    @Column(name = "snapshot_json", columnDefinition = "LONGTEXT")
    public String snapshotJson;
  }

  @Entity(name = "RateLimit")
  @Table(name = "interest_rate_limits")
  public static class RateLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "currency")
    public String currency;

    @Column(name = "kind")
    public String kind;

    @Column(name = "max_tea_percent", precision = 24, scale = 10)
    public java.math.BigDecimal maxTeaPercent;

    @Column(name = "valid_from")
    public java.time.LocalDate validFrom;

    @Column(name = "valid_until")
    public java.time.LocalDate validUntil;

    @Column(name = "source")
    public String source;
  }

  @Entity(name = "Audit")
  @Table(name = "audit_logs")
  public static class Audit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "actor_id")
    public Long actorId;

    @Column(name = "store_id")
    public Long storeId;

    @Column(name = "action")
    public String action;

    @Column(name = "resource")
    public String resource;

    @Column(name = "occurred_at")
    public java.time.Instant occurredAt;
  }

  @Entity(name = "Ticket")
  @Table(name = "support_tickets")
  public static class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "user_id")
    public Long userId;

    @Column(name = "subject")
    public String subject;

    @Column(name = "message", columnDefinition = "LONGTEXT")
    public String message;

    @Column(name = "created_at")
    public java.time.Instant createdAt;
  }

  @Entity(name = "Help")
  @Table(name = "help_articles")
  public static class Help {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "title")
    public String title;

    @Column(name = "content", columnDefinition = "LONGTEXT")
    public String content;
  }
}
