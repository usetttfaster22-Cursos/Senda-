import { supabase } from './supabase';

// En un teléfono físico, localhost no apunta a tu computadora:
// usa la IP local, por ejemplo EXPO_PUBLIC_API_URL=http://192.168.1.20:3000
const API_URL = process.env.EXPO_PUBLIC_API_URL ?? 'http://localhost:3000';

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

// Llama al backend de Senda enviando el token de la sesión actual.
export async function apiFetch<T>(path: string, options: RequestInit = {}): Promise<T> {
  const { data } = await supabase.auth.getSession();
  const token = data.session?.access_token;

  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });

  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new ApiError(response.status, body?.error ?? `Error ${response.status} en ${path}`);
  }
  return response.json() as Promise<T>;
}
