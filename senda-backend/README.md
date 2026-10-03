# Senda Backend

API de Senda (Express + Prisma + PostgreSQL en Supabase).

## Puesta en marcha

1. `npm install`
2. Copia `.env.example` a `.env` y completa `DATABASE_URL`, `DIRECT_URL` y (opcional) `OPENAI_API_KEY`.
3. Crea las tablas: `npm run db:migrate`
   - Si ya ejecutaste `prisma/migrations/20261003000000_init/migration.sql` a mano en el SQL Editor de Supabase,
     márcala como aplicada: `npx prisma migrate resolve --applied 20261003000000_init`
4. Desarrollo: `npm run dev` (http://localhost:3000/health)

## Scripts

| Script | Qué hace |
|---|---|
| `npm run dev` | Servidor con recarga automática |
| `npm run build` / `npm start` | Compila a `dist/` y lo ejecuta |
| `npm run typecheck` | Revisa tipos sin compilar |
| `npm run db:generate` | Regenera el cliente de Prisma |
| `npm run db:migrate` | Aplica las migraciones pendientes |
