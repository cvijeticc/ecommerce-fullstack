/**
 * Dekodira JWT payload BEZ provere potpisa — dovoljno za čitanje "exp" na klijentu.
 * Prava verifikacija potpisa se i dalje radi isključivo na backendu.
 */
export function decodeJwtPayload(token) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(base64));
  } catch {
    return null;
  }
}

export function isTokenExpired(token) {
  const payload = decodeJwtPayload(token);
  if (!payload?.exp) return true;
  return Date.now() >= payload.exp * 1000;
}
