import { estIndisponible } from '../api.js';

/** Message d'erreur lisible (affiche un badge spécial si un service est en panne). */
export function ErrorAlert({ error, onClose }) {
  if (!error) return null;
  const details = error.body?.details;
  const panne = estIndisponible(error);
  return (
    <div className={`alert ${panne ? 'alert-warning' : 'alert-danger'} d-flex align-items-start gap-2`} role="alert">
      <i className={`bi ${panne ? 'bi-plug' : 'bi-exclamation-triangle'} fs-5`}></i>
      <div className="flex-grow-1">
        {panne && <span className="badge text-bg-danger me-2">Service indisponible</span>}
        {error.status > 0 && <span className="badge text-bg-secondary me-2">HTTP {error.status}</span>}
        {error.message}
        {details && typeof details === 'object' && (
          <ul className="mb-0 mt-1 small">
            {Object.entries(details).map(([champ, msg]) => (
              <li key={champ}>
                <strong>{champ}</strong> : {msg}
              </li>
            ))}
          </ul>
        )}
      </div>
      {onClose && <button type="button" className="btn-close" onClick={onClose} aria-label="Fermer"></button>}
    </div>
  );
}

/** Encart affiché à la place d'un bloc dont le service source est hors ligne. */
export function ServiceDown({ service, error }) {
  return (
    <div className="alert alert-warning d-flex align-items-center gap-2 mb-0">
      <i className="bi bi-cloud-slash fs-4"></i>
      <div>
        <span className="badge text-bg-danger me-2">Service indisponible</span>
        <strong>{service}</strong> ne répond pas.
        <div className="small text-muted">{error?.message}</div>
      </div>
    </div>
  );
}

export function Spinner({ label = 'Chargement…' }) {
  return (
    <div className="text-center text-muted py-4">
      <div className="spinner-border spinner-border-sm me-2" role="status"></div>
      {label}
    </div>
  );
}

export function Modal({ title, show, onClose, children, footer, size = '' }) {
  if (!show) return null;
  return (
    <div className="modal d-block" tabIndex="-1" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <div className={`modal-dialog modal-dialog-scrollable ${size}`}>
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">{title}</h5>
            <button type="button" className="btn-close" onClick={onClose}></button>
          </div>
          <div className="modal-body">{children}</div>
          {footer && <div className="modal-footer">{footer}</div>}
        </div>
      </div>
    </div>
  );
}

const STATUT_STYLE = {
  EN_ATTENTE: 'text-bg-warning',
  VALIDEE: 'text-bg-success',
  ANNULEE: 'text-bg-secondary',
};

export function StatutBadge({ statut }) {
  return <span className={`badge ${STATUT_STYLE[statut] ?? 'text-bg-light'}`}>{statut?.replace('_', ' ')}</span>;
}

/** Badge d'origine d'une donnée agrégée via Feign (UP / CACHE_SECOURS / INDISPONIBLE). */
export function SourceBadge({ source }) {
  if (!source || source === 'UP') return null;
  if (source === 'CACHE_SECOURS')
    return (
      <span className="badge text-bg-info ms-1" title="Service hors ligne : dernière valeur connue (cache de secours)">
        <i className="bi bi-clock-history"></i> cache
      </span>
    );
  return (
    <span className="badge text-bg-danger ms-1" title="Service hors ligne, aucune donnée disponible">
      <i className="bi bi-cloud-slash"></i> indisponible
    </span>
  );
}

export function Toast({ message, onClose }) {
  if (!message) return null;
  return (
    <div className="position-fixed bottom-0 end-0 p-3" style={{ zIndex: 1100 }}>
      <div className="toast show align-items-center text-bg-success border-0">
        <div className="d-flex">
          <div className="toast-body">
            <i className="bi bi-check-circle me-2"></i>
            {message}
          </div>
          <button type="button" className="btn-close btn-close-white me-2 m-auto" onClick={onClose}></button>
        </div>
      </div>
    </div>
  );
}
