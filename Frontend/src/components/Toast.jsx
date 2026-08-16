import { useEffect } from 'react';

/**
 * Toast — kratka poruka koja "pliva" preko sadržaja i uvek je vidljiva.
 *
 * Zašto postoji:
 * Ranije se poruka renderovala kao običan div na vrhu stranice. Ako korisnik
 * skroluje do dna kataloga i klikne "Dodaj u korpu", poruka se pojavi 2000px
 * iznad njegovog ekrana i on je nikad ne vidi.
 *
 * position: 'fixed' pozicionira element u odnosu na viewport (prozor browsera),
 * a ne u odnosu na stranicu. Zato ostaje na istom mestu i dok se skroluje.
 * Za razliku od 'absolute', koji se računa u odnosu na najbliži pozicionirani
 * roditelj i skroluje zajedno sa sadržajem.
 *
 * zIndex podiže toast iznad kartica proizvoda, inače bi ga one prekrile.
 *
 * @param {string|null} message  tekst poruke; null/'' → komponenta ne renderuje ništa
 * @param {'success'|'error'} type  određuje boju
 * @param {Function} onClose  poziva se kad istekne vreme prikaza
 * @param {number} duration  koliko milisekundi poruka stoji
 */
export default function Toast({ message, type = 'success', onClose, duration = 3000 }) {
  /**
   * Tajmer koji sam skloni poruku.
   *
   * return () => clearTimeout(timer) je cleanup funkcija — React je zove kad se
   * komponenta ukloni sa ekrana ili pre sledećeg pokretanja efekta. Bez nje bi
   * stari tajmer ostao aktivan i pozvao onClose nad komponentom koje više nema.
   *
   * onClose je u dependency nizu, pa roditelj mora da ga stabilizuje sa
   * useCallback — inače bi svaka nova funkcija resetovala tajmer.
   */
  useEffect(() => {
    if (!message) return;
    const timer = setTimeout(onClose, duration);
    return () => clearTimeout(timer);
  }, [message, duration, onClose]);

  // Rani return — nema poruke, nema ni elementa u DOM-u
  if (!message) return null;

  return (
    // role="alert" — čitači ekrana odmah pročitaju poruku, bez čekanja na fokus
    <div role="alert" style={{ ...styles.base, ...styles[type] }}>
      {message}
    </div>
  );
}

const styles = {
  base: {
    position: 'fixed',
    top: '1rem',
    left: '50%',
    transform: 'translateX(-50%)',
    zIndex: 1000,
    padding: '0.9rem 1.5rem',
    borderRadius: '6px',
    fontSize: '0.95rem',
    fontWeight: 500,
    boxShadow: '0 4px 16px rgba(0,0,0,0.2)',
    maxWidth: '90vw',
    textAlign: 'center',
  },
  success: { background: '#d4edda', color: '#155724', border: '1px solid #b7dfc1' },
  error: { background: '#f8d7da', color: '#a3161f', border: '1px solid #f0b6bb' },
};
