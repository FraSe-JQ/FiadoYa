#!/bin/sh
set -eu
umask 077
backup_directory="${1:-.local/backups}"
mkdir -p "$backup_directory"
backup_file="$backup_directory/fiadoya-$(date -u +%Y%m%d-%H%M%S).sql"
docker compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysqldump -u root --single-transaction --routines --triggers fiadoya' > "$backup_file"
printf 'Copia guardada en %s\n' "$backup_file"
