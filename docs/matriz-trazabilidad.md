# Matriz de trazabilidad

Requisito / observación | Módulo y archivo real | Prueba / evidencia
---|---|---
Registro, acceso y roles | iam/api/AuthController.java; iam/infrastructure/SecurityConfig.java | WorkflowIntegrationTest.publicRegistrationCannotEscalateRoleAndLoginLogoutWorks; E2E
Multitienda y privacidad | iam/application/Access.java | WorkflowIntegrationTest.anotherStoreAndAnotherCustomerCannotReadAccount
Comercios y administradores | stores/api/StoreController.java | E2E; demo-seed
Clientes y condiciones | customers/api/CustomerController.java; accounts/application/CreditService.java | E2E; cambio de tasas en integración
Productos y dos precios | catalog/api/ProductController.java; sales/domain/FinancingPolicy.java | E2E; compra de múltiples ítems
Ayuda en TODOS los campos, profesor | frontend/src/app/shared/field.component.ts; core/form-definitions.ts | 05-ayuda-por-campo.png; comprobación de cobertura de campos
Tasas pactadas por compra, profesor | sales/application/SalesService.java; purchases.terms_json | completeAcademicPurchaseAndLatePaymentArePersistedAndIdempotent
Pseudocódigo coherente, profesor | docs/pseudocodigo-financiero.md; finance/domain | Pruebas individuales por calculador
Línea de crédito y concurrencia | shared/infrastructure/Data.java; SalesService.java | concurrentPurchasesCannotExceedCreditLimit
Mora nominal o efectiva, profesor | finance/domain/InterestRateConverter.java; LateInterestCalculator.java | LateInterestCalculatorTest
Pago y prelación | payments/application/PaymentService.java; PaymentAllocationCalculator.java | integración y E2E; 09-pago-saldo.png
Persistencia de todo el proceso | shared/infrastructure/Model.java; db/migration | Testcontainers; sql-evidence.sql; evidencia SQL
Cierre y listado al corte | reporting/application/CycleService.java; CycleScheduler.java | generación demo; prueba de idempotencia de cierre
Rendimiento, ABET logro del curso | reporting/domain/EffectiveYieldCalculator.java; frontend/reporting/yield.component.ts | flujos contractuales y prueba de rendimiento
Conectividad, semana15 | docker-compose.yml; frontend/nginx.conf | compose ps y construcción real
Identificar, formular y resolver, ABET/ICACIT A/E | requerimientos.md; modelo-financiero.md; guia-sustentacion-codigo.md | casos numéricos, pruebas y decisiones documentadas
Gestión y decisiones económicas, ICACIT L | PROGRESS.md; decisiones-tecnicas.md; rendimiento contractual | registro real de avances; no se inventan esfuerzo, costos o reuniones
Legal, usura, observación docente | decisiones-tecnicas.md; interest_rate_limits | fuentes oficiales y limitaciones explícitas; validación legal pendiente
Registro de apoyo IA, enunciado | registro-apoyo-ia.md | declaración de implementación asistida; datos humanos por completar
