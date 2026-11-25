param([string]$OutputDirectory = '.local/backups')
$ErrorActionPreference = 'Stop'
New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
$backupFile = Join-Path $OutputDirectory ("fiadoya-" + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.sql')
docker compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysqldump -u root --single-transaction --routines --triggers fiadoya' | Set-Content -LiteralPath $backupFile -Encoding utf8
if ($LASTEXITCODE -ne 0) { throw 'No se completó el volcado de seguridad.' }
Write-Output "Copia guardada en $backupFile"
