# FiadoYa — progreso

## Fases completadas

- **1. Referencias:** lectura de los seis documentos originales, prioridad y decisiones documentadas. Las referencias privadas y sus extracciones se excluyen de Git.
- **2. Estructura y persistencia:** monorepo en la raíz actual, MySQL 8.4.7, dos migraciones Flyway, JPA y restricciones relacionales.
- **3. Seguridad:** registro público de clientes, bootstrap de plataforma, BCrypt, JWT, sesiones persistidas, CSRF y autorización por membresía/titularidad.
- **4. Motor financiero:** nueve clases identificables, JavaDoc en español, BigDecimal, potenciación decimal, base 30/360 y pruebas por algoritmo.
- **5. Operaciones:** comercios, administradores, clientes, condiciones versionadas, catálogo, compras multiproducto, cronogramas, liquidación y pagos.
- **6. Frontend:** Angular responsive, ayuda por campo, carga, errores, vacíos y confirmaciones.
- **7. Integración:** reportes por moneda, rendimiento contractual, CSV, cierres programados/manuales, históricos y soporte editable.
- **8. Pruebas:** 37 pruebas backend en Java 21 y 4 E2E satisfactorias. Se corrigieron concurrencia, actualización asíncrona, DNS del proxy, resumen tras pago, cierres de cuotas previas y permisos de administradores que son clientes en otro comercio.
- **9. Documentación:** informe 6.1–6.3, pseudocódigo, guía de sustentación, trazabilidad, manual, despliegue, diagramas y registro IA.
- **10. Demostración:** Compose local, datos ficticios, reloj demo, 17 capturas reales y consultas SQL. Revisión visual independiente aplicada y DESIGN.md generado.

Los resultados definitivos se extraen de reportes ejecutados en `docs/resultados-pruebas.md` y `docs/evidencias/resumen-verificacion.json`.

## Decisiones

- Se mantiene `reference/`, nombre real recibido; también se ignora `references/`.
- Periodo cero: 25/10/2026. Fecha correcta de mora académica: 06/12/2026.
- El contado define el principal y la lista crédito se conserva por separado.
- La línea considera capital y gracia capitalizada; los intereses futuros y la mora no consumen línea.
- El bloqueo refresca la cuenta bajo READ_COMMITTED; compras y pagos tienen idempotencia y unicidad.
- El cálculo puro está separado de registro y persistencia.
- Git se inicializó en esta raíz. No se publicó en GitHub: no se proporcionó un remoto.

## Limitaciones y actividades externas pendientes

- VM Ubuntu documentada, sin despliegue externo ni servicios contratados.
- Prepago legal no implementado; no se afirma cumplimiento legal integral. Los límites normativos requieren fuente y vigencia verificadas.
- Npm conserva 16 avisos moderados en herramientas de desarrollo; producción registra cero. No se ejecutaron auditoría exhaustiva de seguridad ni pruebas de carga.
- Reloj demo global y no persistente entre reinicios. Los cierres tardíos reflejan el estado al generarse; no reconstruyen retroactivamente todos los estados pasados.
- Se preservan las primeras instantáneas de demostración; nuevas versiones no reescriben cierres existentes.
- El soporte almacena solicitudes; no existen canales externos reales suministrados.
- El grupo debe completar el puente humano y porcentaje de apoyo IA respecto del trabajo académico total, revisar normativa y preparar materiales de exposición. No se inventaron actividades, horas, costos ni reuniones.
