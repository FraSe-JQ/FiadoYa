# Resultados de verificación

Las evidencias se obtuvieron en la sesión del 24/11/2026, zona America/Lima. El entorno ejecutado es Docker Desktop local; no una VM remota. Los números siguientes se extraen de reportes reales, no de expectativas del plan.

## Backend y motor financiero

JUnit registró **37 pruebas**, 0 fallos, 0 errores y 0 omitidas. Java de ejecución: 21.0.9. MySQL de integración: 8.4.7 mediante Testcontainers. Se aplicaron ambas migraciones Flyway. El log reproducible está en [java21-tests.log](evidencias/java21-tests.log).

Clase | Pruebas | Fallos | Errores
---|---|---|---
pe.fiadoya.finance.domain.BillingCycleCalculatorTest | 3 | 0 | 0
pe.fiadoya.finance.domain.CommercialDayCalculatorTest | 3 | 0 | 0
pe.fiadoya.finance.domain.CompensatoryInterestCalculatorTest | 2 | 0 | 0
pe.fiadoya.finance.domain.FinancialCalculationServiceTest | 3 | 0 | 0
pe.fiadoya.finance.domain.FrenchAmortizationCalculatorTest | 2 | 0 | 0
pe.fiadoya.finance.domain.GracePeriodCalculatorTest | 1 | 0 | 0
pe.fiadoya.finance.domain.InterestRateConverterTest | 4 | 0 | 0
pe.fiadoya.finance.domain.LateInterestCalculatorTest | 3 | 0 | 0
pe.fiadoya.finance.domain.PaymentAllocationCalculatorTest | 2 | 0 | 0
pe.fiadoya.reporting.domain.EffectiveYieldCalculatorTest | 3 | 0 | 0
pe.fiadoya.WorkflowIntegrationTest | 11 | 0 | 0

Los casos académicos reprodujeron 166.07 de capital capitalizado, cuotas 57.59/57.59/57.58, total 172.76, mora 0.53 y liquidación 58.12 con saldo futuro 111.80. Las pruebas adicionales verificaron tasas, febrero, corte, año nuevo, tasa cero, condiciones históricas, pagos, concurrencia y cierres de compras previas.

## Frontend y navegador

La compilación de producción se encuentra en [frontend-build.log](evidencias/frontend-build.log). Playwright reportó 4 pruebas satisfactorias, 0 inesperadas y 0 omitidas. El flujo registra usuario, comercio, productos, cliente, compra, cronograma y pago; también verifica portal multitienda, soporte, CSV y Swagger. Las capturas son pantallas reales de la aplicación con datos ficticios. Los resultados completos se encuentran en [playwright-results.json](evidencias/playwright-results.json).

## Persistencia y despliegue

[persistencia-sql.txt](evidencias/persistencia-sql.txt) contiene compras, cronogramas, pagos y consultas de invariantes ejecutadas contra MySQL persistente. [docker-compose-ps.txt](evidencias/docker-compose-ps.txt) registra salud y puertos. MySQL no publica puertos. La estructura real fue consultada en information_schema.

## Dependencias y límites de verificación

La auditoría npm de dependencias de producción informa 0 vulnerabilidades. Al incluir herramientas de desarrollo se informan 16 moderadas y 0 altas; quedan registradas en frontend-audit-all.json. No se fuerza una degradación de Angular para silenciar avisos transitivos. No se realizó una auditoría exhaustiva de seguridad ni una prueba de carga o penetración.

La advertencia del detector visual corresponde a Inter, tipografía expresamente solicitada, y se conserva. La revisión visual independiente verificó escritorio y móvil y dio lugar a mejoras en históricos, medios de pago y explicación de sesión. No se han medido métricas de desempeño ni se ha validado cumplimiento legal integral. Los procedimientos de prepago, validación normativa, TLS y operación en VM externa requieren tareas adicionales antes de un uso comercial.

Flyway emitió una advertencia sobre la versión MySQL 8.4; las dos migraciones, la validación JPA y las pruebas sobre MySQL 8.4.7 se ejecutaron satisfactoriamente. La ejecución local verifica estas operaciones y no sustituye una certificación de compatibilidad exhaustiva.

La comprobación SQL final registró cero saldos de capital inconsistentes, cero compras cruzadas entre tiendas y cero obligaciones cruzadas. Se ejecutó una copia de seguridad local mediante backup.ps1; su restauración no fue ensayada.

La prueba adicional de rol mixto confirma que un administrador de una tienda solo consulta su propia cuenta al actuar como cliente de otra. La interfaz también cambia al modo de consulta para ese comercio.
