# Plan de pruebas

Las pruebas unitarias de finance/domain se ejecutan sin Spring ni MySQL. Cada algoritmo tiene su archivo de prueba. Se utilizan resultados numéricos explícitos, no datos leídos desde pantallas. WorkflowIntegrationTest inicia MySQL 8.4.7 mediante Testcontainers y ejecuta migraciones reales; no emplea H2.

La integración verifica compra multiproducto, condiciones históricas tras cambio de tasas, cronograma, mora, pago insuficiente, repetición idempotente, persistencia, varias obligaciones exigibles, monedas/modalidades, autorización y concurrencia. Playwright prueba el flujo visual de registro, login, asignación, catálogo, clientes, venta, cronograma, pago, CSV, soporte, portal del cliente y Swagger.

Ejecución desde la raíz con Maven instalado: `mvn -f backend/pom.xml test -Dapi.version=1.44`. En PowerShell se escribe el argumento entre comillas: `'-Dapi.version=1.44'`. Docker debe estar disponible para integración. Para un algoritmo: `mvn -f backend/pom.xml -Dtest=FrenchAmortizationCalculatorTest test`.

Frontend: `cd frontend`, `npm ci`, `npm run build -- --configuration production`, `npx playwright install chromium`, `npx playwright test`. Las E2E requieren Compose activo, .env local y datos generados por demo-seed. Crean un comercio ficticio adicional en cada ejecución; no deben apuntar a producción.

La ejecución con Java 21 puede realizarse con el perfil tools de docker-compose.test.yml. Los resultados reales y las limitaciones se registran en resultados-pruebas.md; este plan no implica que toda prueba haya pasado.
