import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api, estIndisponible } from '../api.js';
import { ErrorAlert, ServiceDown, SourceBadge, Spinner, StatutBadge } from '../components/ui.jsx';

const MENTION_STYLE = { 'Très bien': 'success', Bien: 'success', 'Assez bien': 'primary', Passable: 'info', Ajourné: 'danger' };

export default function Releve() {
  const { etudiantId } = useParams();
  const navigate = useNavigate();
  const [etudiants, setEtudiants] = useState({ liste: [], erreur: null });
  const [annee, setAnnee] = useState('');
  const [releve, setReleve] = useState(null);
  const [erreur, setErreur] = useState(null);
  const [chargement, setChargement] = useState(false);

  useEffect(() => {
    api('/api/etudiants?size=500&sort=nom,asc')
      .then((p) => setEtudiants({ liste: p.content, erreur: null }))
      .catch((e) => setEtudiants({ liste: [], erreur: e }));
  }, []);

  useEffect(() => {
    if (!etudiantId) {
      setReleve(null);
      return;
    }
    setChargement(true);
    setErreur(null);
    api(`/api/inscriptions/etudiant/${etudiantId}/releve${annee ? `?annee=${annee}` : ''}`)
      .then(setReleve)
      .catch((e) => {
        setErreur(e);
        setReleve(null);
      })
      .finally(() => setChargement(false));
  }, [etudiantId, annee]);

  const e = releve?.etudiant;

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-3 no-print">
        <h2 className="page-title mb-0">
          <i className="bi bi-award me-2"></i>Relevé de notes
        </h2>
        {releve && (
          <button className="btn btn-outline-secondary" onClick={() => window.print()}>
            <i className="bi bi-printer me-1"></i>Imprimer
          </button>
        )}
      </div>

      <div className="card mb-3 no-print">
        <div className="card-body row g-2 align-items-end">
          <div className="col-md-7">
            <label className="form-label small text-muted">Étudiant</label>
            {etudiants.erreur ? (
              <div className="input-group">
                <input type="number" min="1" className="form-control" placeholder="Identifiant étudiant"
                  defaultValue={etudiantId} onBlur={(ev) => ev.target.value && navigate(`/releve/${ev.target.value}`)} />
                <span className="input-group-text text-bg-danger">ETUDIANT-SERVICE indisponible</span>
              </div>
            ) : (
              <select className="form-select" value={etudiantId ?? ''} onChange={(ev) => navigate(`/releve/${ev.target.value}`)}>
                <option value="" disabled>— choisir un étudiant —</option>
                {etudiants.liste.map((et) => (
                  <option key={et.id} value={et.id}>{et.nom} {et.prenom} ({et.matricule})</option>
                ))}
              </select>
            )}
          </div>
          <div className="col-md-5">
            <label className="form-label small text-muted">Année universitaire</label>
            <select className="form-select" value={annee} onChange={(ev) => setAnnee(ev.target.value)}>
              <option value="">Toutes les années</option>
              <option>2026-2027</option>
              <option>2025-2026</option>
            </select>
          </div>
        </div>
      </div>

      {chargement && <Spinner label="Agrégation des données (inscriptions + étudiant + cours)…" />}
      {erreur && (estIndisponible(erreur) ? <ServiceDown service="INSCRIPTION-SERVICE" error={erreur} /> : <ErrorAlert error={erreur} />)}
      {!etudiantId && <p className="text-muted">Choisissez un étudiant pour afficher son bulletin.</p>}

      {releve && !chargement && (
        <div className="card">
          <div className="card-body">
            <div className="d-flex justify-content-between flex-wrap gap-3 border-bottom pb-3 mb-3">
              <div>
                <div className="text-muted small text-uppercase">Bulletin de notes</div>
                <h4 className="mb-1">
                  {e.prenom} {e.nom}
                  <SourceBadge source={e.sourceDonnees} />
                </h4>
                <div className="small text-muted">
                  {e.matricule && <>Matricule <code>{e.matricule}</code> · </>}
                  {e.filiere && <>Filière {e.filiere} · Niveau {e.niveau} · </>}
                  {releve.anneeUniversitaire ?? 'Toutes années'}
                </div>
              </div>
              <div className="text-end">
                <div className="text-muted small">Moyenne pondérée</div>
                <div className="display-6 fw-bold">
                  {releve.moyennePonderee != null ? releve.moyennePonderee.toFixed(2) : '—'}
                  <small className="fs-6 text-muted"> /20</small>
                </div>
                <span className={`badge text-bg-${MENTION_STYLE[releve.mention] ?? 'secondary'}`}>{releve.mention}</span>
              </div>
            </div>

            {releve.warnings?.length > 0 && (
              <div className="alert alert-warning small">
                <i className="bi bi-exclamation-diamond me-1"></i>
                {releve.warnings.join(' · ')}
              </div>
            )}

            <table className="table">
              <thead className="table-light">
                <tr>
                  <th>Module (UE)</th><th>Cours</th><th className="text-center">Coef.</th>
                  <th className="text-center">ECTS</th><th>Statut</th><th className="text-end">Note /20</th>
                </tr>
              </thead>
              <tbody>
                {releve.lignes.map((l) => (
                  <tr key={l.inscriptionId}>
                    <td>
                      <strong>{l.moduleCode ?? '?'}</strong>
                      <div className="small text-muted">{l.moduleIntitule}</div>
                    </td>
                    <td>
                      <code>{l.coursCode ?? `#${l.coursId}`}</code> {l.coursTitre}
                      <span className="badge text-bg-light border ms-1">{l.typeCours}</span>
                    </td>
                    <td className="text-center">{l.coefficient ?? '—'}</td>
                    <td className="text-center">{l.credits ?? '—'}</td>
                    <td><StatutBadge statut={l.statut} /></td>
                    <td className={`text-end fw-semibold ${l.note != null && l.note < 10 ? 'text-danger' : ''}`}>
                      {l.note != null ? l.note.toFixed(2) : <span className="text-muted fw-normal">non noté</span>}
                    </td>
                  </tr>
                ))}
                {releve.lignes.length === 0 && (
                  <tr><td colSpan="6" className="text-center text-muted py-3">Aucune inscription active</td></tr>
                )}
              </tbody>
              <tfoot>
                <tr className="table-light">
                  <td colSpan="2" className="small text-muted">
                    {releve.nombreCoursNotes} cours noté(s) · Σ coefficients = {releve.totalCoefficients}
                  </td>
                  <td colSpan="4" className="text-end">
                    Crédits acquis : <strong>{releve.creditsAcquis} ECTS</strong>
                  </td>
                </tr>
              </tfoot>
            </table>
            <p className="small text-muted mb-0">
              Moyenne = Σ(note × coefficient du module) / Σ(coefficients), calculée par l'orchestrateur à partir
              d'inscription_db (notes) et de COURS-SERVICE (coefficients) obtenus via OpenFeign.
            </p>
          </div>
        </div>
      )}
    </>
  );
}
