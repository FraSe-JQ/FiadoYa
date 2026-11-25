# Instalación y despliegue

## Ejecución local reproducible

Requisitos: Docker con Compose y Node.js 24 para scripts. Se ejecuta desde la raíz del repositorio:

```sh
node scripts/setup-env.cjs
docker compose up --build -d
docker compose ps
node scripts/demo-seed.cjs
```

La URL local es http://localhost:4200. Swagger: http://localhost:4200/swagger-ui/index.html. La base no publica puertos. El volumen mysql_data conserva los registros al recrear contenedores. `docker compose down` conserva el volumen; no se debe agregar -v si se desea preservar información.

El administrador inicial se crea solo si no existe un PLATFORM_ADMIN y se suministran BOOTSTRAP_EMAIL y BOOTSTRAP_PASSWORD de 12–72 caracteres. setup-env genera valores aleatorios locales; no incluye credenciales privadas en Git. Después de inicializar se pueden vaciar las variables de bootstrap conservando JWT_SECRET y las contraseñas de base de datos.

Para desarrollo, npm ci y npm start dentro de frontend, con backend en localhost:8080 y proxy.conf.json. El backend local requiere Java 21, Maven 3.9.11, MySQL accesible localmente mediante un entorno de desarrollo explícito y variables DB_URL, DB_USER, DB_PASSWORD y JWT_SECRET. La configuración Compose no expone MySQL por defecto.

## VM Ubuntu propuesta

Se instala Docker Engine con Compose, se copia el repositorio sin referencias privadas y se ejecuta el mismo procedimiento. Se establece APP_PROFILE=default, se usan secretos propios, se restringe .env con permisos 600 y se configura HTTP_BIND según el proxy TLS elegido. La publicación de producción requiere dominio y certificado TLS reales no suministrados. El proxy debe preservar X-Forwarded-Proto para cookies Secure.

No se ha desplegado en una VM remota. No se han adquirido servicios ni certificados. El despliegue demostrado es Docker Desktop local. Como dimensionamiento inicial propuesto, no medido mediante carga, se recomiendan 2 vCPU, 4 GB RAM y 10 GB de disco disponible; las compilaciones pueden necesitar más memoria. No se afirman capacidades de usuarios concurrentes no medidas.

## Operación

Se verifica salud con docker compose ps y docker compose logs. Antes de actualizar se realiza copia de seguridad. En Windows, scripts/backup.ps1 guarda un volcado sin poner la contraseña en la línea de comandos; en Ubuntu se ejecuta `sh scripts/backup.sh`. La restauración debe ensayarse en una base separada antes de producción. Las migraciones no se editan una vez aplicadas: los cambios nuevos se agregan como versiones Flyway.

Los secretos y las credenciales ficticias están excluidos de Git. Se requiere rotación de secretos, política de copias, TLS, supervisión y revisión de normativa antes de cualquier uso comercial.
