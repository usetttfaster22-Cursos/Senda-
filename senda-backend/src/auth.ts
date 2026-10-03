import type { NextFunction, Request, Response } from 'express';
import { createClient } from '@supabase/supabase-js';
import type { PrismaClient } from '@prisma/client';

export interface AuthUser {
  id: string;
  email: string;
}

declare global {
  namespace Express {
    interface Request {
      user?: AuthUser;
    }
  }
}

const SUPABASE_URL = process.env.SUPABASE_URL;
const SUPABASE_ANON_KEY = process.env.SUPABASE_ANON_KEY;

if (!SUPABASE_URL || !SUPABASE_ANON_KEY) {
  console.warn('Faltan SUPABASE_URL o SUPABASE_ANON_KEY: las rutas protegidas responderán 503.');
}

const supabase =
  SUPABASE_URL && SUPABASE_ANON_KEY
    ? createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
        auth: { persistSession: false, autoRefreshToken: false },
      })
    : null;

// Verifica el token de Supabase Auth (Authorization: Bearer <token>) y deja el
// usuario en req.user. El userId nunca se toma del body ni de la URL.
export function requireAuth(prisma: PrismaClient) {
  return async (req: Request, res: Response, next: NextFunction) => {
    if (!supabase) {
      return res.status(503).json({ error: 'Autenticación no configurada en el servidor' });
    }

    const header = req.headers.authorization ?? '';
    const token = header.startsWith('Bearer ') ? header.slice(7) : null;
    if (!token) {
      return res.status(401).json({ error: 'No autenticado' });
    }

    try {
      const { data, error } = await supabase.auth.getUser(token);
      if (error || !data.user || !data.user.email) {
        return res.status(401).json({ error: 'Sesión inválida o expirada' });
      }

      const user = { id: data.user.id, email: data.user.email };

      // Crea el registro local la primera vez que el usuario llega al backend.
      await prisma.user.upsert({
        where: { id: user.id },
        update: {},
        create: { id: user.id, email: user.email },
      });

      req.user = user;
      next();
    } catch (error) {
      console.error(error);
      res.status(500).json({ error: 'Error verificando la sesión' });
    }
  };
}
