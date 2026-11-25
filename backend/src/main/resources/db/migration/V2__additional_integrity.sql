-- Database-level constraints complement application validation.
ALTER TABLE stores MODIFY name VARCHAR(255) NOT NULL, MODIFY business_type VARCHAR(255) NOT NULL;
ALTER TABLE users MODIFY email VARCHAR(255) NOT NULL, MODIFY password_hash VARCHAR(255) NOT NULL, MODIFY role VARCHAR(255) NOT NULL,
 ADD CHECK(role IN ('PLATFORM_ADMIN','STORE_ADMIN','CUSTOMER'));
ALTER TABLE purchases MODIFY store_id BIGINT NOT NULL, MODIFY account_id BIGINT NOT NULL, MODIFY request_key VARCHAR(255) NOT NULL, MODIFY terms_json LONGTEXT NOT NULL;
ALTER TABLE payments MODIFY store_id BIGINT NOT NULL, MODIFY account_id BIGINT NOT NULL, MODIFY request_key VARCHAR(255) NOT NULL;
ALTER TABLE credit_terms ADD CHECK(max_months BETWEEN 1 AND 120), ADD CHECK(cutoff_day BETWEEN 1 AND 31), ADD CHECK(payment_day BETWEEN 1 AND 31), ADD CHECK(compensatory_percent>=0), ADD CHECK(penalty_percent>=0);
