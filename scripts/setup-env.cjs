const fs=require('fs'),crypto=require('crypto');
if(fs.existsSync('.env')){console.log('.env ya existe; se conserva.');process.exit(0);}
const secret=()=>crypto.randomBytes(32).toString('hex');
fs.writeFileSync('.env',`DB_PASSWORD=${secret()}\nDB_ROOT_PASSWORD=${secret()}\nJWT_SECRET=${secret()}\nBOOTSTRAP_EMAIL=platform@fiadoya.test\nBOOTSTRAP_PASSWORD=${secret()}\nAPP_PROFILE=demo\nHTTP_BIND=127.0.0.1\nHTTP_PORT=4200\nCORS_ORIGIN=http://localhost:4200\n`);
console.log('Se creó .env con secretos aleatorios; consúltelo localmente.');
