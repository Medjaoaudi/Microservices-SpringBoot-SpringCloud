import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { anneeCourante, api, estIndisponible, STATUTS } from '../api.js';
import { ErrorAlert, ServiceDown, SourceBadge, Spinner, StatutBadge, Toast } from '../components/ui.jsx';

export default function Inscriptions() {
  const [inscriptions, setInscriptions] = useState(null);
  const [erreurListe, setErreurListe] = useState(null);
  const [etudiants, setEtudiants] = useState({ liste: [], erreur: null });
  const [cours, setCours] = useState({ liste: [], erreur: null });
  const [demande, setDemande] = useState({ etudiantId: '', coursId: '', anneeUniversitaire: anneeCourante() });
  const [erreurAction, setErreurAction] = useState(null);
  const [notes, setNotes] = useState({});
  const [filtre, setFiltre] = useState({ statut: '', texte: '' });
  const [toast, setToast] = useState('');

  const chargerInscriptions = useCallback(async () => {
    try {
      setInscriptions(await api('/api/inscriptions'));
      setErreurListe(null);
    } catch (e) {
      setErreurListe(e);
    }
  }, []);

  const chargerReferentiels = useCallback(async () => {
    api('/api/etudiants?size=500&sort=nom,asc')
      .then((p) => setEtudiants({ liste: p.content, erreur: null }))
      .catch((e) => setEtudiants({ liste: [], erreur: e }));
    api('/api/cours')
      .then((l) => setCours({ liste: l, erreur: null }))
      .catch((e) => setCours({ liste: [], erreur: e }));
  }, []);

  useEffect(() => {
    chargerInscriptions();
    chargerReferentiels();
  }, [chargerInscriptions, chargerReferentiels]);

  // Places occupées par cours (année choisie, hors annulations) pour l'affichage dans la liste déroulante
  const occupation = useMemo(() => {
    const m = {};
    inscriptions?.forEach((i) => {
      if (i.statut !== 'ANNULEE' && i.anneeUniversitaire === demande.anneeUniversitaire) m[i.coursId] = (m[i.coursId] ?? 0) + 1;
    });
    return m;
  }, [inscriptions, demande.anneeUniversitaire]);

  const avertissements = useMemo(
    () => [...new Set((inscriptions ?? []).flatMap((i) => i.warnings ?? []))],
    [inscriptions]
  );

  const visibles = (inscriptions ?? []).filter((i) => {
    if (filtre.statut && i.statut !== filtre.statut) return false;
    if (!filtre.texte) return true;
    const t = filtre.texte.toLowerCase();
    return [i.etudiant?.nom, i.etudiant?.prenom, i.etudiant?.matricule, i.cours?.code, i.cours?.titre]
      .some((v) => v?.toLowerCase().includes(t));
  });

  const action = async (fn, message) => {
    setErreurAction(null);
    try {
      await fn();
      setToast(message);
      chargerInscriptions();
    } catch (e) {
      setErreurAction(e);
    }
  };

  const inscrire = (e) => {
    e.preventDefault();
    action(() => api('/api/inscriptions', {
      method: 'POST',
      body: { etudiantId: Number(demande.etudiantId), coursId: Number(demande.coursId), anneeUniversitaire: demande.anneeUniversitaire || null },
    }), 'Inscription enregistrée (statut EN ATTENTE)');
  };

  return (
    <>
      <h2 className="page-title mb-3">
        <i className="bi bi-journal-check me-2"></i>Inscriptions pédagogiques
      </h2>

      <div className="card mb-3">
        <div className="card-header bg-white fw-semibold">
          <i className="bi bi-plus-circle me-2"></i>Nouvelle inscription
          <span className="small text-muted fw-normal ms-2">
            (l'orchestrateur vérifie via Feign l'étudiant, le cours, les doublons et la capacité)
          </span>
        </div>
        <div className="card-body">
          <form className="row g-2 align-items-end" onSubmit={inscrire}>
            <div className="col-md-4">
              <label className="form-label small text-muted">Étudiant</label>
              {etudiants.erreur ? (
                <>
                  <input type="number" min="1" className="form-control" placeholder="Identifiant étudiant" required
                    value={demande.etudiantId} onChange={(e) => setDemande({ ...demande, etudiantId: e.target.value })} />
                  <span className="badge text-bg-danger mt-1">ETUDIANT-SERVICE indisponible</span>
                </>
              ) : (
                <select className="form-select" required value={demande.etudiantId}
                  onChange={(e) => setDemande({ ...demande, etudiantId: e.target.value })}>
                  <option value="">— choisir —</option>
                  {etudiants.liste.map((et) => (
                    <option key={et.id} value={et.id}>{et.nom} {et.prenom} ({et.matricule})</option>
                  ))}
                </select>
              )}
            </div>
            <div className="col-md-4">
              <label className="form-label small text-muted">Cours</label>
              {cours.erreur ? (
                <>
                  <input type="number" min="1" className="form-control" placeholder="Identifiant cours" required
                    value={demande.coursId} onChange={(e) => setDemande({ ...demande, coursId: e.target.value })} />
                  <span className="badge text-bg-danger mt-1">COURS-SERVICE indisponible</span>
                </>
              ) : (
                <select className="form-select" required value={demande.coursId}
                  onChange={(e) => setDemande({ ...demande, coursId: e.target.value })}>
                  <option value="">— choisir —</option>
                  {cours.liste.map((c) => {
                    const occ = occupation[c.id] ?? 0;
                    return (
                      <option key={c.id} value={c.id}>
                        {c.code} — {c.titre} ({occ}/{c.capaciteMax}{occ >= c.capaciteMax ? ' COMPLET' : ''})
                      </option>
                    );
                  })}
                </select>
              )}
            </div>
            <div className="col-md-2">
              <label className="form-label small text-muted">Année</label>
              <input className="form-control" pattern="\d{4}-\d{4}" value={demande.anneeUniversitaire}
                onChange={(e) => setDemande({ ...demande, anneeUniversitaire: e.target.value })} />
            </div>
            <div className="col-md-2">
              <button className="btn btn-primary w-100" type="submit">
                <i className="bi bi-check2 me-1"></i>Inscrire
              </button>
            </div>
          </form>
        </div>
      </div>

      <ErrorAlert error={erreurAction} onClose={() => setErreurAction(null)} />

      {avertissements.length > 0 && (
        <div className="alert alert-warning">
          <div className="fw-semibold"><i className="bi bi-exclamation-diamond me-2"></i>Mode dégradé (réponse 200 de l'orchestrateur)</div>
          <ul className="mb-0 small">{avertissements.map((w) => <li key={w}>{w}</li>)}</ul>
        </div>
      )}

      {erreurListe && (estIndisponible(erreurListe)
        ? <ServiceDown service="INSCRIPTION-SERVICE" error={erreurListe} />
        : <ErrorAlert error={erreurListe} />)}
      {!inscriptions && !erreurListe && <Spinner />}

      {inscriptions && (
        <div className="card">
          <div className="card-header bg-white d-flex flex-wrap gap-2 justify-content-between align-items-center">
            <span className="fw-semibold">{visibles.length} inscription(s)</span>
            <div className="d-flex gap-2">
              <input className="form-control form-control-sm" placeholder="Filtrer (nom, matricule, cours)…"
                value={filtre.texte} onChange={(e) => setFiltre({ ...filtre, texte: e.target.value })} />
              <select className="form-select form-select-sm w-auto" value={filtre.statut}
                onChange={(e) => setFiltre({ ...filtre, statut: e.target.value })}>
                <option value="">Tous statuts</option>
                {STATUTS.map((s) => <option key={s}>{s}</option>)}
              </select>
              <button className="btn btn-sm btn-outline-secondary" title="Actualiser" onClick={chargerInscriptions}>
                <i className="bi bi-arrow-repeat"></i>
              </button>
            </div>
          </div>
          <div className="table-responsive">
            <table className="table table-hover mb-0">
              <thead className="table-light">
                <tr>
                  <th>#</th><th>Étudiant</th><th>Cours</th><th>Année</th><th>Statut</th><th>Note /20</th><th></th>
                </tr>
              </thead>
              <tbody>
                {visibles.map((i) => (
                  <tr key={i.id} className={i.warnings?.length ? 'table-warning' : ''}>
                    <td className="text-muted">{i.id}</td>
                    <td>
                      {i.etudiant ? (
                        <>
                          <strong>{i.etudiant.nom}</strong> {i.etudiant.prenom}
                          <SourceBadge source={i.serviceStatus?.['ETUDIANT-SERVICE']} />
                          <div className="small text-muted">{i.etudiant.matricule ?? `id ${i.etudiantId}`}</div>
                        </>
                      ) : <span className="text-muted">Étudiant #{i.etudiantId} introuvable</span>}
                    </td>
                    <td>
                      {i.cours ? (
                        <>
                          <code>{i.cours.code ?? `#${i.coursId}`}</code> {i.cours.titre}
                          <SourceBadge source={i.serviceStatus?.['COURS-SERVICE']} />
                          <div className="small text-muted">{i.cours.module?.code}</div>
                        </>
                      ) : <span className="text-muted">Cours #{i.coursId} introuvable</span>}
                    </td>
                    <td className="small">{i.anneeUniversitaire}</td>
                    <td>
                      <div className="d-flex align-items-center gap-1">
                        <StatutBadge statut={i.statut} />
                        <select className="form-select form-select-sm w-auto" value={i.statut} disabled={i.statut === 'ANNULEE'}
                          onChange={(e) => action(() => api(`/api/inscriptions/${i.id}/statut`, { method: 'PUT', body: { statut: e.target.value } }), 'Statut modifié')}>
                          {STATUTS.map((s) => <option key={s}>{s}</option>)}
                        </select>
                      </div>
                    </td>
                    <td>
                      <div className="input-group input-group-sm flex-nowrap">
                        <input type="number" step="0.25" min="0" max="20" className="form-control note-input"
                          disabled={i.statut === 'ANNULEE'}
                          value={notes[i.id] ?? i.note ?? ''}
                          onChange={(e) => setNotes({ ...notes, [i.id]: e.target.value })} />
                        <button className="btn btn-outline-success" title="Enregistrer la note"
                          disabled={i.statut === 'ANNULEE' || notes[i.id] === undefined || notes[i.id] === ''}
                          onClick={() => action(async () => {
                            await api(`/api/inscriptions/${i.id}/note`, { method: 'PUT', body: { note: Number(notes[i.id]) } });
                            setNotes(({ [i.id]: _n, ...reste }) => reste);
                          }, 'Note enregistrée')}>
                          <i className="bi bi-save"></i>
                        </button>
                      </div>
                    </td>
                    <td className="text-end text-nowrap">
                      <Link className="btn btn-sm btn-outline-secondary me-1" to={`/releve/${i.etudiantId}`} title="Relevé">
                        <i className="bi bi-award"></i>
                      </Link>
                      <button className="btn btn-sm btn-outline-danger" title="Supprimer"
                        onClick={() => window.confirm(`Supprimer l'inscription #${i.id} ?`)
                          && action(() => api(`/api/inscriptions/${i.id}`, { method: 'DELETE' }), 'Inscription supprimée')}>
                        <i className="bi bi-trash"></i>
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <Toast message={toast} onClose={() => setToast('')} />
    </>
  );
}
