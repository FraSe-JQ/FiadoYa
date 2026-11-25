# Decisiones técnicas y contradicciones

| Tema | Evidencia y decisión | Consecuencia |
|---|---|---|
| Fuentes privadas | Se mantiene reference/, como fue recibido; también se ignora references/ | Ninguna referencia académica ni extracción se publica |
| Periodo cero | El informe parcial muestra 06/10 con capital que recién existe al fin de gracia | Se corrige a 25/10/2026 |
| Fecha de mora | Una entrada del informe dice 06/11 aunque la validación dice 06/12 y exige 11 días | Se usa 06/12/2026; no se altera la fórmula |
| Precio base | El informe utiliza lista crédito y también calcula intereses explícitos | FinancingPolicy utiliza contado; ambos precios se conservan. El caso académico aporta 164 explícitamente |
| Día comercial | El enunciado fija 360/30 pero no una variante detallada | Se normalizan todos los finales de mes a 30 y se prueba febrero |
| Corte | Se compara fecha-hora, no únicamente el día | Igualdad exacta incluida en ciclo actual |
| Línea | Se distingue capital capitalizado de compensatorio futuro y mora | Se libera línea al amortizar capital |
| Historia | La observación del profesor exige tasas pactadas por compra | credit_terms append-only y purchases.terms_json autocontenido |
| Tablas lógicas | Roles cerrados como enum textual; imagen única en products.image_url; cronogramas integrados en payment_obligations | Se evita duplicar tablas sin necesidad; las FK y restricciones están en Flyway |
| Sesión | JWT firmado en cookie HttpOnly, sesión persistida y CSRF | No se guarda JWT en localStorage; logout revoca sesiones del usuario |
| Roles mixtos | Un administrador puede ser cliente de un comercio que no administra | La membresía se comprueba por comercio; sin ella solo puede consultar su propia cuenta. La interfaz adapta el menú al comercio activo |
| Concurrencia | Una prueba detectó entidad cargada antes del bloqueo | Data.lock refresca bajo bloqueo y se utiliza READ_COMMITTED |
| Frontend | Angular 21 utiliza detección zoneless | Los componentes notifican con ChangeDetectorRef.markForCheck al terminar operaciones asíncronas |
| Proxy | Nginx podía conservar IP anterior al recrear backend | Resolución DNS de Docker con renovación cada 10 segundos |
| Persistencia | JPA usa entidades explícitas y repositorio Data | No se generan interfaces vacías por tabla; servicios transaccionales separados del cálculo |
| Cierre | Instantáneas inmutables por cuenta/corte | El cierre es idempotente; el reloj demo es global al proceso y se reinicia al reiniciar API |

## Alcance normativo

La Ley 31143 regula protección frente a usura y modifica normas sobre intereses. Se incorpora una administración de límites por moneda, concepto, fuente y vigencia; no se fija una tasa normativa permanente. La selección y verificación de la fuente corresponde al responsable del despliegue.

La prohibición de pagos parciales corresponde al flujo académico de deudas exigibles y no equivale a negar el derecho de prepago. No se implementa reestructuración por prepago ni se declara cumplimiento integral para explotación comercial. La gracia capitalizada y la base de mora se conservan por exigencia del enunciado y requieren evaluación legal antes de uso real. No se extrapola automáticamente la normativa SBS de entidades supervisadas a cualquier comercio.

Fuentes primarias consultadas el 24/11/2026:
- [Ley 31143, Diario Oficial El Peruano](https://busquedas.elperuano.pe/dispositivo/NL/1936151-1/).
- [Código de Protección y Defensa del Consumidor](https://busquedas.elperuano.pe/dispositivo/NL/538113-1).
- [SBS: derechos durante la vigencia del crédito](https://www.sbs.gob.pe/usuarios/aprende-con-la-sbs/durantelavigenciadelcredito).
- [Compatibilidad oficial de Angular](https://angular.dev/reference/versions).
- [Requisitos de Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/system-requirements.html).

Estas referencias sustentan las limitaciones y decisiones; no constituyen un dictamen de cumplimiento ni validación de valores normativos actuales.
