// Point d'entrée UNIQUE : l'API Gateway. Aucun appel direct aux microservices.
export const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8888';

export class ApiError extends Error {
  constructor(status, body) {
    super(body?.message || `Erreur HTTP ${status}`);
    this.status = status;
    this.body = body;
  }
}

export async function api(path, { method = 'GET', body, signal } = {}) {
  let res;
  try {
    res = await fetch(API_URL + path, {
      method,
      signal,
      headers: body !== undefined ? { 'Content-Type': 'application/json' } : undefined,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch (e) {
    if (e.name === 'AbortError') throw e;
    throw new ApiError(0, {
      message: `API Gateway injoignable (${API_URL}). Les services sont-ils démarrés ?`,
    });
  }
  if (res.status === 204) return null;
  const text = await res.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = { message: text };
  }
  if (!res.ok) throw new ApiError(res.status, data);
  return data;
}

/** Vrai si l'erreur traduit une panne (service hors ligne / gateway injoignable). */
export const estIndisponible = (e) => [0, 502, 503, 504].includes(e?.status);

export const FILIERES = ['GL', 'RT', 'IIA', 'IMI', 'CBA', 'MPI'];
export const SEMESTRES = ['S1', 'S2', 'S3', 'S4', 'S5', 'S6', 'S7', 'S8', 'S9', 'S10'];
export const TYPES_COURS = ['COURS', 'TD', 'TP'];
export const STATUTS = ['EN_ATTENTE', 'VALIDEE', 'ANNULEE'];

export function anneeCourante(date = new Date()) {
  const debut = date.getMonth() >= 8 ? date.getFullYear() : date.getFullYear() - 1;
  return `${debut}-${debut + 1}`;
}
