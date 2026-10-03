# Despliegue en el servidor (Docker + Nginx)

El backend corre en un contenedor que escucha solo en `127.0.0.1:8110`;
Nginx lo publica con HTTPS en un subdominio (por ejemplo `api.TU-DOMINIO.com`).

## 1. Descargar el código

```bash
git clone https://github.com/usetttfaster22-Cursos/Senda-.git /opt/senda
cd /opt/senda/senda-backend
```

## 2. Configurar variables

```bash
cp .env.example .env
nano .env        # DATABASE_URL, DIRECT_URL, SUPABASE_URL, SUPABASE_ANON_KEY, OPENAI_API_KEY
chmod 600 .env
```

## 3. Levantar el contenedor

```bash
docker compose up -d --build
curl http://127.0.0.1:8110/health
```

## 4. Crear las tablas (solo la primera vez)

```bash
docker compose run --rm api npx prisma migrate deploy
```

Si ya ejecutaste `migration.sql` a mano en el SQL Editor de Supabase, en lugar de lo anterior:

```bash
docker compose run --rm api npx prisma migrate resolve --applied 20261003000000_init
```

## 5. Publicar con Nginx + HTTPS

Primero crea un registro DNS tipo A de `api.TU-DOMINIO.com` apuntando a la IP del servidor.

```bash
cp deploy/nginx-senda.conf /etc/nginx/sites-available/senda
nano /etc/nginx/sites-available/senda        # reemplaza api.TU-DOMINIO.com
ln -s /etc/nginx/sites-available/senda /etc/nginx/sites-enabled/senda
nginx -t && systemctl reload nginx

# Certificado gratuito de Let's Encrypt (instala certbot si no lo tienes)
apt install -y certbot python3-certbot-nginx
certbot --nginx -d api.TU-DOMINIO.com
```

Prueba: `curl https://api.TU-DOMINIO.com/health`

En la app móvil, usa `EXPO_PUBLIC_API_URL=https://api.TU-DOMINIO.com`.

## Actualizar

```bash
cd /opt/senda && git pull
cd senda-backend && docker compose up -d --build
docker compose run --rm api npx prisma migrate deploy   # si hay migraciones nuevas
```

## Logs

```bash
docker compose logs -f api
```
