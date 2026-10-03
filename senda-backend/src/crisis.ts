// Capa 1 del protocolo de crisis (PLAN_MVP.md, punto 9): si el mensaje coincide,
// el backend no llama a la IA y responde directamente con el mensaje de emergencia.

export const CRISIS_RESPONSE =
  "Lo que me cuentas es muy importante y doloroso. Como soy una inteligencia artificial, no puedo darte el nivel de apoyo que mereces en este momento. Por favor, utiliza el botón 'Necesito ayuda ahora' en la pantalla o contacta a un servicio de emergencias local.";

// Se aplican sobre el texto en minúsculas y sin acentos.
const CRISIS_PATTERNS: RegExp[] = [
  /suicid/,
  /\bmatarme\b/,
  /\bme quiero matar\b/,
  /quitarme la vida/,
  /\bno quiero (seguir )?vivir\b/,
  /\bquiero morir(me)?\b/,
  /\bme quiero morir\b/,
  /\bmejor (estaria|estar) muert[oa]\b/,
  /\bestaria mejor muert[oa]\b/,
  /\bhacerme dano\b/,
  /\bautolesi/,
  /\bcortarme\b/,
  /\bme corto\b/,
  /\bme (golpea|pega|maltrata)\b/,
  /\babus(o|aron|a de mi|ando de mi)\b/,
  /\bviola(cion|ron|da|do)\b/,
  /\b(matar|lastimar) a alguien\b/,
  /\bvoces que me (dicen|ordenan)\b/,
];

export function normalize(text: string): string {
  return text
    .toLowerCase()
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '');
}

export function isCrisisMessage(message: string): boolean {
  const text = normalize(message);
  return CRISIS_PATTERNS.some((pattern) => pattern.test(text));
}
