# FiadoYa

Aplicación de crédito comercial multitienda para SI642 — Finanzas e Ingeniería Económica, UPC, 2026-20, sección 7658, Grupo 4.

Administra comercios, clientes, condiciones versionadas, catálogo, ventas multiproducto, método francés con gracia total, pago único, mora y liquidación completa. Incluye portal del cliente, ayuda por campo, reportes, rendimiento contractual, CSV, auditoría y cierres idempotentes.

## Iniciar localmente

Requisitos: Docker Desktop/Engine con Compose y Node.js 24 para generar el entorno y la demostración.

```sh
node scripts/setup-env.cjs
docker compose up --build -d
docker compose ps
node scripts/demo-seed.cjs
```

Abrir [FiadoYa local](http://localhost:4200) y [Swagger](http://localhost:4200/swagger-ui/index.html). Las credenciales de plataforma se generan en `.env`; las de los usuarios ficticios se guardan en `.local/demo-credentials.json`. Ambos archivos son locales e ignorados por Git. No se imprimen ni se publican contraseñas.

La carga demo agrega dos comercios de giros distintos, administradores separados, cuatro cuentas, cinco productos, cuatro compras y un pago. Para reproducir la mora, la plataforma puede ajustar el reloj desde Configuración en el perfil `demo`. El reloj es global al proceso y se reinicia al reiniciar la API.

`docker compose down` conserva el volumen de datos. No utilizar `down -v` si se requiere conservar el historial. MySQL y la API no publican puertos; solo se expone Nginx en `127.0.0.1:4200`.

## Tecnologías fijadas

| Componente | Versión |
|---|---|
| Angular | 21.2.26 |
| Angular CLI / build | 21.2.31 |
| TypeScript | 5.9.3 |
| Tailwind CSS | 4.1.17 |
| Node de compilación Docker | 24.13.1 |
| Java objetivo y contenedores | 21 |
| Spring Boot | 3.5.7 |
| big-math | 2.3.2 |
| Springdoc OpenAPI | 2.8.14 |
| MySQL | 8.4.7 |
| Maven | 3.9.11 |
| Testcontainers | 1.21.3 |
| Playwright | 1.56.1 |
| Nginx | 1.28.0 |

Las dependencias administradas de Spring Security, Spring Data JPA, Flyway, JUnit 5, Mockito y MySQL Connector/J quedan fijadas por el BOM de Spring Boot en [pom.xml](backend/pom.xml). El frontend conserva `package-lock.json`. El backend utiliza JWT con expiración, sesiones persistidas, cookie HttpOnly, CSRF y BCrypt; la autorización multitienda ocurre en el servidor.

## Arquitectura y código

El monorepo contiene `frontend/`, `backend/`, `infrastructure/`, `docs/`, `scripts/` y `tests/`. El backend es un monolito modular de tres capas. Los nueve calculadores/coordinadores financieros están en [finance/domain](backend/src/main/java/pe/fiadoya/finance/domain) y son independientes de Spring y MySQL.

- [Guía de sustentación del código](docs/guia-sustentacion-codigo.md): rutas, métodos, fórmulas, ejemplos y pruebas individuales.
- [Modelo financiero](docs/modelo-financiero.md) y [pseudocódigo corregido](docs/pseudocodigo-financiero.md).
- [Arquitectura](docs/arquitectura.md), [modelo de datos](docs/modelo-base-de-datos.md) y [recorrido de compra/pago](docs/trazabilidad-operaciones.md).
- [Matriz de trazabilidad](docs/matriz-trazabilidad.md), [decisiones técnicas](docs/decisiones-tecnicas.md) y [registro de apoyo IA](docs/registro-apoyo-ia.md).
- [Documentación del informe](docs/informe/6.1-conectividad.md), [base de datos](docs/informe/6.2-base-de-datos.md) y [código fuente](docs/informe/6.3-codigo-fuente.md).
- [Manual](docs/manual-usuario.md), [demostración](docs/guia-demostracion.md) y [despliegue Ubuntu](docs/despliegue.md).

## Verificación

Pruebas backend con Java 21 y MySQL efímero real:

```sh
docker compose -f docker-compose.test.yml run --rm tests
```

Con Maven y Java instalados:

```sh
mvn -f backend/pom.xml test -Dapi.version=1.44
mvn -f backend/pom.xml -Dtest=FrenchAmortizationCalculatorTest test
```

En PowerShell se escribe `'-Dapi.version=1.44'` entre comillas. La segunda instrucción ejecuta el algoritmo aislado sin Spring ni MySQL.

Pruebas de navegador sobre la demostración local:

```sh
cd frontend
npm ci
npm run build -- --configuration production
npx playwright install chromium
npx playwright test
```

Las E2E crean un comercio ficticio nuevo por ejecución. No se deben ejecutar contra producción. Los resultados realmente obtenidos están en [resultados-pruebas](docs/resultados-pruebas.md), las capturas en [evidencias](docs/evidencias/) y el estado de trabajo en [PROGRESS.md](PROGRESS.md).

## Alcance y limitaciones

El precio contado constituye la base de financiamiento; el precio lista crédito se conserva independientemente. Los cálculos usan BigDecimal, precisión 34, base comercial 30/360 explícita y redondeo monetario HALF_UP. Los intereses de prueba no son tasas normativas verificadas.

El flujo ordinario exige el pago exacto de todas las obligaciones exigibles. El procedimiento legal de prepago es distinto y no está implementado. No se declara cumplimiento legal integral. Los límites de tasas requieren una fuente y vigencia verificadas. No se incluyen stock, impuestos, facturación ni conexión a RENIEC/SUNAT.

El despliegue remoto Ubuntu está preparado y documentado, pero no ha sido realizado. El soporte guarda solicitudes; no inventa ni envía mensajes a canales externos. Los datos humanos del registro IA, la validación legal y los materiales finales de presentación deben ser completados por el equipo.
