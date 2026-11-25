# Infraestructura

La definición ejecutable está en [docker-compose.yml](../docker-compose.yml), los Dockerfiles en cada aplicación y el proxy en [frontend/nginx.conf](../frontend/nginx.conf).

MySQL utiliza una red interna y un volumen persistente. Solo Nginx publica un puerto local. Las variables se describen en [.env.example](../.env.example); los secretos reales se generan localmente y no se versionan.

El procedimiento para una VM Ubuntu está en [docs/despliegue.md](../docs/despliegue.md). La VM remota no ha sido creada ni desplegada.
