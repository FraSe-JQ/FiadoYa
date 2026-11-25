-- Flyway V1: immutable financial history, store scoping and integrity.
CREATE TABLE users (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 email VARCHAR(255),
 password_hash VARCHAR(255),
 name VARCHAR(255),
 role VARCHAR(255),
 active BIT NOT NULL
);
CREATE TABLE auth_sessions (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT,
 token_id VARCHAR(255),
 expires_at DATETIME(6),
 revoked BIT NOT NULL
);
CREATE TABLE stores (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(255),
 business_type VARCHAR(255),
 address VARCHAR(255),
 phone VARCHAR(255),
 active BIT NOT NULL
);
CREATE TABLE store_memberships (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 store_id BIGINT,
 user_id BIGINT
);
CREATE TABLE customers (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 store_id BIGINT,
 user_id BIGINT,
 document_type VARCHAR(255),
 document_number VARCHAR(255),
 first_name VARCHAR(255),
 last_name VARCHAR(255),
 email VARCHAR(255),
 phone VARCHAR(255),
 address VARCHAR(255),
 consent BIT NOT NULL,
 active BIT NOT NULL
);
CREATE TABLE credit_accounts (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 store_id BIGINT,
 customer_id BIGINT,
 currency VARCHAR(255),
 used DECIMAL(24,10)
);
CREATE TABLE credit_terms (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 account_id BIGINT,
 currency VARCHAR(255),
 credit_limit DECIMAL(24,10),
 max_months INT NOT NULL,
 cutoff_day INT NOT NULL,
 cutoff_time TIME(6),
 payment_day INT NOT NULL,
 compensatory_percent DECIMAL(24,10),
 compensatory_type VARCHAR(255),
 compensatory_period INT NOT NULL,
 compensatory_capitalization INT NOT NULL,
 penalty_percent DECIMAL(24,10),
 penalty_type VARCHAR(255),
 penalty_period INT NOT NULL,
 penalty_capitalization INT NOT NULL,
 created_at DATETIME(6)
);
CREATE TABLE products (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 store_id BIGINT,
 code VARCHAR(255),
 name VARCHAR(255),
 description VARCHAR(255),
 supplier VARCHAR(255),
 brand VARCHAR(255),
 unit VARCHAR(255),
 image_url VARCHAR(1000),
 cash_price DECIMAL(24,10),
 credit_price DECIMAL(24,10),
 currency VARCHAR(255),
 mode VARCHAR(255),
 active BIT NOT NULL
);
CREATE TABLE purchases (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 store_id BIGINT,
 account_id BIGINT,
 actor_id BIGINT,
 request_key VARCHAR(255),
 request_hash VARCHAR(255),
 purchased_at DATETIME(6),
 mode VARCHAR(255),
 months INT NOT NULL,
 currency VARCHAR(255),
 financed DECIMAL(24,10),
 capitalized DECIMAL(24,10),
 grace_interest DECIMAL(24,10),
 cutoff DATETIME(6),
 period_zero DATE,
 terms_json LONGTEXT
);
CREATE TABLE purchase_items (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 purchase_id BIGINT,
 product_id BIGINT,
 product_name VARCHAR(255),
 quantity DECIMAL(24,10),
 cash_price DECIMAL(24,10),
 credit_price DECIMAL(24,10),
 unit_price DECIMAL(24,10),
 subtotal DECIMAL(24,10)
);
CREATE TABLE payment_obligations (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 store_id BIGINT,
 account_id BIGINT,
 purchase_id BIGINT,
 number INT NOT NULL,
 due_date DATE,
 opening DECIMAL(24,10),
 interest DECIMAL(24,10),
 principal DECIMAL(24,10),
 amount DECIMAL(24,10),
 closing DECIMAL(24,10),
 paid BIT NOT NULL
);
CREATE TABLE payments (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 store_id BIGINT,
 account_id BIGINT,
 actor_id BIGINT,
 request_key VARCHAR(255),
 request_hash VARCHAR(255),
 paid_at DATE,
 received DECIMAL(24,10),
 penalty DECIMAL(24,10),
 interest DECIMAL(24,10),
 principal DECIMAL(24,10),
 remaining_capital DECIMAL(24,10),
 method VARCHAR(255)
);
CREATE TABLE payment_allocations (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 payment_id BIGINT,
 obligation_id BIGINT,
 penalty DECIMAL(24,10),
 interest DECIMAL(24,10),
 principal DECIMAL(24,10)
);
CREATE TABLE billing_cycles (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 store_id BIGINT,
 account_id BIGINT,
 cutoff DATETIME(6),
 generated_at DATETIME(6),
 snapshot_json LONGTEXT
);
CREATE TABLE interest_rate_limits (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 currency VARCHAR(255),
 kind VARCHAR(255),
 max_tea_percent DECIMAL(24,10),
 valid_from DATE,
 valid_until DATE,
 source VARCHAR(255)
);
CREATE TABLE audit_logs (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 actor_id BIGINT,
 store_id BIGINT,
 action VARCHAR(255),
 resource VARCHAR(255),
 occurred_at DATETIME(6)
);
CREATE TABLE support_tickets (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 user_id BIGINT,
 subject VARCHAR(255),
 message LONGTEXT,
 created_at DATETIME(6)
);
CREATE TABLE help_articles (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 title VARCHAR(255),
 content LONGTEXT
);

ALTER TABLE users ADD CONSTRAINT uk_user_email UNIQUE(email);
ALTER TABLE auth_sessions ADD CONSTRAINT uk_session_token UNIQUE(token_id), ADD FOREIGN KEY(user_id) REFERENCES users(id);
ALTER TABLE store_memberships ADD CONSTRAINT uk_membership UNIQUE(store_id,user_id), ADD FOREIGN KEY(store_id) REFERENCES stores(id), ADD FOREIGN KEY(user_id) REFERENCES users(id);
ALTER TABLE customers ADD CONSTRAINT uk_customer_document UNIQUE(store_id,document_type,document_number), ADD FOREIGN KEY(store_id) REFERENCES stores(id), ADD FOREIGN KEY(user_id) REFERENCES users(id);
ALTER TABLE credit_accounts ADD CONSTRAINT uk_account_customer UNIQUE(customer_id), ADD FOREIGN KEY(store_id) REFERENCES stores(id), ADD FOREIGN KEY(customer_id) REFERENCES customers(id), ADD CHECK(used>=0);
ALTER TABLE credit_terms ADD FOREIGN KEY(account_id) REFERENCES credit_accounts(id), ADD CHECK(credit_limit>0);
ALTER TABLE products ADD CONSTRAINT uk_product_code UNIQUE(store_id,code), ADD FOREIGN KEY(store_id) REFERENCES stores(id), ADD CHECK(cash_price>=0), ADD CHECK(credit_price>=0);
ALTER TABLE purchases ADD CONSTRAINT uk_purchase_key UNIQUE(store_id,request_key), ADD FOREIGN KEY(store_id) REFERENCES stores(id), ADD FOREIGN KEY(account_id) REFERENCES credit_accounts(id), ADD FOREIGN KEY(actor_id) REFERENCES users(id), ADD CHECK(financed>0);
ALTER TABLE purchase_items ADD FOREIGN KEY(purchase_id) REFERENCES purchases(id), ADD FOREIGN KEY(product_id) REFERENCES products(id), ADD CHECK(quantity>0);
ALTER TABLE payment_obligations ADD CONSTRAINT uk_obligation_number UNIQUE(purchase_id,number), ADD FOREIGN KEY(purchase_id) REFERENCES purchases(id), ADD FOREIGN KEY(account_id) REFERENCES credit_accounts(id), ADD FOREIGN KEY(store_id) REFERENCES stores(id), ADD CHECK(principal>=0), ADD CHECK(amount>=0);
ALTER TABLE payments ADD CONSTRAINT uk_payment_key UNIQUE(store_id,request_key), ADD FOREIGN KEY(store_id) REFERENCES stores(id), ADD FOREIGN KEY(account_id) REFERENCES credit_accounts(id), ADD FOREIGN KEY(actor_id) REFERENCES users(id), ADD CHECK(received>0);
ALTER TABLE payment_allocations ADD CONSTRAINT uk_paid_obligation UNIQUE(obligation_id), ADD FOREIGN KEY(payment_id) REFERENCES payments(id), ADD FOREIGN KEY(obligation_id) REFERENCES payment_obligations(id);
ALTER TABLE billing_cycles ADD CONSTRAINT uk_billing_cycle UNIQUE(account_id,cutoff), ADD FOREIGN KEY(store_id) REFERENCES stores(id), ADD FOREIGN KEY(account_id) REFERENCES credit_accounts(id);
ALTER TABLE audit_logs ADD FOREIGN KEY(actor_id) REFERENCES users(id), ADD FOREIGN KEY(store_id) REFERENCES stores(id);
ALTER TABLE support_tickets ADD FOREIGN KEY(user_id) REFERENCES users(id);
CREATE INDEX idx_obligation_due ON payment_obligations(store_id,paid,due_date);
CREATE INDEX idx_purchase_account ON purchases(account_id,purchased_at);
CREATE INDEX idx_terms_version ON credit_terms(account_id,id);
CREATE INDEX idx_payment_account ON payments(account_id,paid_at);
CREATE INDEX idx_customer_user ON customers(user_id,store_id);
CREATE INDEX idx_session_expiry ON auth_sessions(expires_at);
CREATE INDEX idx_limits_validity ON interest_rate_limits(currency,kind,valid_from,valid_until);
INSERT INTO help_articles(title,content) VALUES ('Primeros pasos','Registre un comercio, sus productos y clientes. Configure las condiciones antes de registrar una venta. El documento se valida por formato; no se consulta RENIEC ni SUNAT.'),('Crédito y pagos','La línea se consume por capital y gracia capitalizada. Los intereses futuros y la mora se muestran por separado. La liquidación ordinaria cancela todas las obligaciones exigibles, sin pagos parciales.'),('Asistencia técnica','Utilice el formulario de soporte. Las solicitudes se almacenan para revisión del equipo; no se envían a canales externos.');
