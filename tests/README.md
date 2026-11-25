# Pruebas

- Unitarias: [backend/src/test/java/pe/fiadoya/finance/domain](../backend/src/test/java/pe/fiadoya/finance/domain).
- Integración MySQL/Testcontainers: [WorkflowIntegrationTest.java](../backend/src/test/java/pe/fiadoya/WorkflowIntegrationTest.java).
- Navegador: [frontend/e2e/workflow.spec.ts](../frontend/e2e/workflow.spec.ts).
- Ejecución con Java 21: `docker compose -f docker-compose.test.yml run --rm tests`.
- Resultados y límites verificados: [docs/resultados-pruebas.md](../docs/resultados-pruebas.md).
