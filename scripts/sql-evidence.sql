SELECT version,description,success FROM flyway_schema_history ORDER BY installed_rank;
SELECT id,name,business_type,active FROM stores ORDER BY id;
SELECT id,store_id,account_id,purchased_at,financed,capitalized,grace_interest,period_zero FROM purchases ORDER BY id;
SELECT id AS purchase_id, JSON_UNQUOTE(JSON_EXTRACT(terms_json,'$.compensatory.type')) AS contracted_rate_type,
 JSON_EXTRACT(terms_json,'$.compensatory.percent') AS contracted_rate_percent,
 JSON_EXTRACT(terms_json,'$.compensatory.periodDays') AS contracted_rate_days,
 JSON_EXTRACT(terms_json,'$.penalty.percent') AS contracted_penalty_percent FROM purchases ORDER BY id;
SELECT purchase_id,number,due_date,opening,interest,principal,amount,closing,paid FROM payment_obligations ORDER BY purchase_id,number;
SELECT id,store_id,account_id,paid_at,received,penalty,interest,principal,remaining_capital FROM payments ORDER BY id;
SELECT payment_id,obligation_id,penalty,interest,principal FROM payment_allocations ORDER BY payment_id,obligation_id;
SELECT id,store_id,account_id,cutoff FROM billing_cycles ORDER BY id;
-- Invariante: usado debe coincidir con capital no pagado; cero filas significa coherencia.
SELECT a.id,a.used,COALESCE(SUM(CASE WHEN o.paid=0 THEN o.principal ELSE 0 END),0) expected_used
FROM credit_accounts a LEFT JOIN payment_obligations o ON o.account_id=a.id
GROUP BY a.id,a.used HAVING a.used<>expected_used;
-- Invariante: no existen obligaciones o compras cruzadas entre tiendas.
SELECT p.id FROM purchases p JOIN credit_accounts a ON a.id=p.account_id WHERE p.store_id<>a.store_id;
SELECT o.id FROM payment_obligations o JOIN purchases p ON p.id=o.purchase_id WHERE o.store_id<>p.store_id OR o.account_id<>p.account_id;
SELECT COUNT(*) AS inconsistent_credit_balances FROM (
 SELECT a.id FROM credit_accounts a LEFT JOIN payment_obligations o ON o.account_id=a.id
 GROUP BY a.id,a.used HAVING a.used<>COALESCE(SUM(CASE WHEN o.paid=0 THEN o.principal ELSE 0 END),0)
) invalid_balances;
SELECT COUNT(*) AS cross_store_purchases FROM purchases p JOIN credit_accounts a ON a.id=p.account_id WHERE p.store_id<>a.store_id;
SELECT COUNT(*) AS cross_store_obligations FROM payment_obligations o JOIN purchases p ON p.id=o.purchase_id WHERE o.store_id<>p.store_id OR o.account_id<>p.account_id;
