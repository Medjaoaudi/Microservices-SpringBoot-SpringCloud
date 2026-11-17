import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api.js';

const SERVICES = [
  { id: 'etudiant-service', label: 'ETUDIANT-SERVICE', port: 8081, icon: 'people', bd: 'etudiant_db' },
  { id: 'cours-service', label: 'COURS-SERVICE', port: 8082, icon: 'book', bd: 'cours_db' },
  { id: 'inscription-service', label: 'INSCRIPTION-SERVICE', port: 8083, icon: 'diagram-3', bd: 'inscription_db' },
];

const CB_STYLE = { CLOSED: 'success', OPEN: 'danger', HALF_OPEN: 'warning', FORCED_OPEN: 'danger', DISABLED: 'secondary' };

async function settle(promise) {
  try {
    return { ok: true, value: await promise };
  } catch (error) {
    return { ok: false, error };
  }
}

export default function Dashboard() {
  const [etat, setEtat] = useState(null);
  const [maj, setMaj] = useState(null);

  const rafraichir = useCallback(async () => {
    const [gateway, ...sante] = await Promise.all([
      settle(api('/actuator/health')),
      // Via le "discovery locator" de la Gateway : /<nom-service>/actuator/health
      ...SERVICES.map((s) => settle(api(`/${s.id}/actuator/health`))),
    ]);
    const [etudiants, modules, cours, inscriptions, cbs] = await Promise.all([
      settle(api('/api/etudiants?size=1')),
      settle(api('/api/modules')),
      settle(api('/api/cours')),
      settle(api('/api/inscriptions')),
      settle(api('/inscription-service/actuator/circuitbreakers')),
    ]);
    setEtat({ gateway, sante, etudiants, modules, cours, inscriptions, cbs });
    setMaj(new Date());
  }, []);

  useEffect(() => {
    rafraichir();
    const t = setInterval(rafraichir, 5000);
    return () => clearInterval(t);
  }, [rafraichir]);

  const compteur = (r, f) => (r?.ok ? f(r.value) : null);
  const ins = etat?.inscriptions;
  const degradees = ins?.ok ? ins.value.filter((i) => i.warnings?.length).length : 0;

  const stats = [
    { label: 'Étudiants', icon: 'people', to: '/etudiants', value: compteur(etat?.etudiants, (p) => p.page.totalElements), r: etat?.etudiants },
    { label: 'Modules (UE)', icon: 'collection', to: '/catalogue', value: compteur(etat?.modules, (l) => l.length), r: etat?.modules },
    { label: 'Cours', icon: 'book', to: '/catalogue', value: compteur(etat?.cours, (l) => l.length), r: etat?.cours },
    { label: 'Inscriptions', icon: 'journal-check', to: '/inscriptions', value: compteur(ins, (l) => l.length), r: ins },
  ];

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2 className="page-title mb-0">
          <i className="bi bi-speedometer2 me-2"></i>Tableau de bord
        </h2>
        <div className="text-muted small">
          <i className="bi bi-arrow-repeat me-1"></i>
          Actualisation auto. toutes les 5 s {maj && `· ${maj.toLocaleTimeString('fr-FR')}`}
          <button className="btn btn-sm btn-outline-secondary ms-2" onClick={rafraichir}>
            Actualiser
          </button>
        </div>
      </div>

      {!etat ? (
        <div className="text-muted">Interrogation des services…</div>
      ) : (
        <>
          {!etat.gateway.ok && (
            <div className="alert alert-danger">
              <i className="bi bi-x-octagon me-2"></i>
              <strong>API Gateway injoignable</strong> (http://localhost:8888) : démarrez l'architecture avec <code>start-all</code>.
            </div>
          )}

          <div className="row g-3 mb-4">
            {stats.map((s) => (
              <div className="col-6 col-lg-3" key={s.label}>
                <Link to={s.to} className="text-decoration-none">
                  <div className="card stat-card h-100">
                    <div className="card-body d-flex justify-content-between align-items-center">
                      <div>
                        <div className="text-muted small text-uppercase">{s.label}</div>
                        {s.value !== null ? (
                          <div className="stat-value text-dark">{s.value}</div>
                        ) : (
                          <span className="badge text-bg-danger mt-2">Service indisponible</span>
                        )}
                      </div>
                      <i className={`bi bi-${s.icon} stat-icon text-primary`}></i>
                    </div>
                  </div>
                </Link>
              </div>
            ))}
          </div>

          {degradees > 0 && (
            <div className="alert alert-warning">
              <i className="bi bi-exclamation-diamond me-2"></i>
              <strong>Mode dégradé :</strong> {degradees} inscription(s) affichée(s) avec des données de secours
              (un service partenaire est hors ligne, l'orchestrateur continue de répondre).
            </div>
          )}

          <div className="row g-3">
            <div className="col-lg-7">
              <div className="card h-100">
                <div className="card-header bg-white fw-semibold">
                  <i className="bi bi-hdd-network me-2"></i>État des microservices (Eureka + Actuator)
                </div>
                <ul className="list-group list-group-flush">
                  <li className="list-group-item d-flex justify-content-between align-items-center">
                    <span>
                      <i className="bi bi-signpost-split me-2"></i>
                      <strong>API-GATEWAY</strong> <span className="text-muted small">:8888</span>
                    </span>
                    <HealthBadge r={etat.gateway} />
                  </li>
                  {SERVICES.map((s, i) => (
                    <li key={s.id} className="list-group-item d-flex justify-content-between align-items-center">
                      <span>
                        <i className={`bi bi-${s.icon} me-2`}></i>
                        <strong>{s.label}</strong> <span className="text-muted small">:{s.port} · BD {s.bd}</span>
                      </span>
                      <HealthBadge r={etat.sante[i]} />
                    </li>
                  ))}
                </ul>
                <div className="card-footer bg-white small text-muted">
                  Annuaire : <a href="http://localhost:8761" target="_blank" rel="noreferrer">Eureka (8761)</a> ·
                  Documentation : <a href="http://localhost:8888/swagger-ui.html" target="_blank" rel="noreferrer">Swagger agrégé</a>
                </div>
              </div>
            </div>

            <div className="col-lg-5">
              <div className="card h-100">
                <div className="card-header bg-white fw-semibold">
                  <i className="bi bi-lightning-charge me-2"></i>Disjoncteurs de l'orchestrateur (Resilience4j)
                </div>
                <div className="card-body">
                  {etat.cbs.ok ? (
                    Object.entries(etat.cbs.value.circuitBreakers).map(([nom, cb]) => (
                      <div key={nom} className="d-flex justify-content-between align-items-center border rounded p-2 mb-2">
                        <div>
                          <div className="fw-semibold">{nom}</div>
                          <div className="small text-muted">
                            échecs {cb.failedCalls}/{cb.bufferedCalls} · taux {cb.failureRate} · rejetés {cb.notPermittedCalls}
                          </div>
                        </div>
                        <span className={`badge fs-6 text-bg-${CB_STYLE[cb.state] ?? 'secondary'} ${cb.state !== 'CLOSED' ? 'pulse' : ''}`}>
                          {cb.state}
                        </span>
                      </div>
                    ))
                  ) : (
                    <span className="badge text-bg-danger">Service indisponible</span>
                  )}
                  <p className="small text-muted mb-0 mt-2">
                    CLOSED = appels normaux · OPEN = appels court-circuités vers le fallback · HALF_OPEN = appels de test.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </>
  );
}

function HealthBadge({ r }) {
  if (!r) return null;
  if (r.ok && r.value?.status === 'UP')
    return (
      <span className="badge text-bg-success">
        <i className="bi bi-check-circle me-1"></i>UP
      </span>
    );
  return (
    <span className="badge text-bg-danger pulse">
      <i className="bi bi-x-circle me-1"></i>Service indisponible
    </span>
  );
}
