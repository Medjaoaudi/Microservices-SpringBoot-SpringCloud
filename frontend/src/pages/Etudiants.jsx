import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, estIndisponible, FILIERES } from '../api.js';
import { ErrorAlert, Modal, ServiceDown, Spinner, Toast } from '../components/ui.jsx';

const VIDE = { matricule: '', nom: '', prenom: '', email: '', telephone: '', dateNaissance: '', filiere: 'GL', niveau: 5 };
const TAILLE_PAGE = 8;

export default function Etudiants() {
  const [page, setPage] = useState(0);
  const [tri, setTri] = useState('id,asc');
  const [filtres, setFiltres] = useState({ nom: '', filiere: '', niveau: '' });
  const [donnees, setDonnees] = useState(null);
  const [erreurChargement, setErreurChargement] = useState(null);
  const [form, setForm] = useState(null); // null = fermé ; {id?, ...champs}
  const [erreurForm, setErreurForm] = useState(null);
  const [toast, setToast] = useState('');

  const rechercheActive = filtres.nom || filtres.filiere || filtres.niveau;

  const charger = useCallback(async () => {
    setErreurChargement(null);
    try {
      if (rechercheActive) {
        const q = new URLSearchParams(Object.entries(filtres).filter(([, v]) => v !== ''));
        const liste = await api(`/api/etudiants/search?${q}`);
        setDonnees({ content: liste, page: { number: 0, totalPages: 1, totalElements: liste.length } });
      } else {
        setDonnees(await api(`/api/etudiants?page=${page}&size=${TAILLE_PAGE}&sort=${tri}`));
      }
    } catch (e) {
      setErreurChargement(e);
      setDonnees(null);
    }
  }, [page, tri, filtres, rechercheActive]);

  useEffect(() => {
    charger();
  }, [charger]);

  const enregistrer = async (e) => {
    e.preventDefault();
    setErreurForm(null);
    const { id, ...corps } = form;
    const payload = { ...corps, niveau: Number(corps.niveau), dateNaissance: corps.dateNaissance || null, telephone: corps.telephone || null };
    try {
      if (id) await api(`/api/etudiants/${id}`, { method: 'PUT', body: payload });
      else await api('/api/etudiants', { method: 'POST', body: payload });
      setForm(null);
      setToast(id ? 'Étudiant modifié' : 'Étudiant créé');
      charger();
    } catch (err) {
      setErreurForm(err);
    }
  };

  const supprimer = async (et) => {
    if (!window.confirm(`Supprimer ${et.prenom} ${et.nom} (${et.matricule}) ?`)) return;
    try {
      await api(`/api/etudiants/${et.id}`, { method: 'DELETE' });
      setToast('Étudiant supprimé');
      charger();
    } catch (err) {
      setErreurChargement(err);
    }
  };

  const champ = (nom) => ({
    value: form?.[nom] ?? '',
    onChange: (e) => setForm({ ...form, [nom]: e.target.value }),
  });

  const trier = (col) => {
    const [c, dir] = tri.split(',');
    setTri(`${col},${c === col && dir === 'asc' ? 'desc' : 'asc'}`);
  };
  const flecheTri = (col) => (tri.startsWith(col + ',') ? (tri.endsWith('asc') ? ' ▲' : ' ▼') : '');

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2 className="page-title mb-0">
          <i className="bi bi-people me-2"></i>Étudiants
        </h2>
        <button className="btn btn-primary" onClick={() => { setErreurForm(null); setForm({ ...VIDE }); }}>
          <i className="bi bi-person-plus me-1"></i>Nouvel étudiant
        </button>
      </div>

      <div className="card mb-3">
        <div className="card-body row g-2 align-items-end">
          <div className="col-md-5">
            <label className="form-label small text-muted">Nom ou prénom</label>
            <input className="form-control" placeholder="ex. ben" value={filtres.nom}
              onChange={(e) => setFiltres({ ...filtres, nom: e.target.value })} />
          </div>
          <div className="col-md-3">
            <label className="form-label small text-muted">Filière</label>
            <select className="form-select" value={filtres.filiere} onChange={(e) => setFiltres({ ...filtres, filiere: e.target.value })}>
              <option value="">Toutes</option>
              {FILIERES.map((f) => <option key={f}>{f}</option>)}
            </select>
          </div>
          <div className="col-md-2">
            <label className="form-label small text-muted">Niveau</label>
            <select className="form-select" value={filtres.niveau} onChange={(e) => setFiltres({ ...filtres, niveau: e.target.value })}>
              <option value="">Tous</option>
              {[1, 2, 3, 4, 5].map((n) => <option key={n}>{n}</option>)}
            </select>
          </div>
          <div className="col-md-2">
            <button className="btn btn-outline-secondary w-100" onClick={() => setFiltres({ nom: '', filiere: '', niveau: '' })}>
              Réinitialiser
            </button>
          </div>
        </div>
      </div>

      {erreurChargement && (estIndisponible(erreurChargement)
        ? <ServiceDown service="ETUDIANT-SERVICE" error={erreurChargement} />
        : <ErrorAlert error={erreurChargement} onClose={() => setErreurChargement(null)} />)}

      {!donnees && !erreurChargement && <Spinner />}

      {donnees && (
        <div className="card">
          <div className="table-responsive">
            <table className="table table-hover mb-0">
              <thead className="table-light">
                <tr>
                  <th role="button" onClick={() => trier('matricule')}>Matricule{flecheTri('matricule')}</th>
                  <th role="button" onClick={() => trier('nom')}>Nom & prénom{flecheTri('nom')}</th>
                  <th>Email</th>
                  <th role="button" onClick={() => trier('filiere')}>Filière{flecheTri('filiere')}</th>
                  <th role="button" onClick={() => trier('niveau')}>Niveau{flecheTri('niveau')}</th>
                  <th className="text-end">Actions</th>
                </tr>
              </thead>
              <tbody>
                {donnees.content.map((et) => (
                  <tr key={et.id}>
                    <td><code>{et.matricule}</code></td>
                    <td>
                      <strong>{et.nom}</strong> {et.prenom}
                      <div className="small text-muted">{et.telephone}</div>
                    </td>
                    <td className="small">{et.email}</td>
                    <td><span className="badge text-bg-light border">{et.filiere}</span></td>
                    <td>{et.niveau}<sup>e</sup> année</td>
                    <td className="text-end text-nowrap">
                      <Link className="btn btn-sm btn-outline-success me-1" to={`/releve/${et.id}`} title="Relevé de notes">
                        <i className="bi bi-award"></i>
                      </Link>
                      <button className="btn btn-sm btn-outline-primary me-1" title="Modifier"
                        onClick={() => { setErreurForm(null); setForm({ ...VIDE, ...et, dateNaissance: et.dateNaissance ?? '', telephone: et.telephone ?? '' }); }}>
                        <i className="bi bi-pencil"></i>
                      </button>
                      <button className="btn btn-sm btn-outline-danger" title="Supprimer" onClick={() => supprimer(et)}>
                        <i className="bi bi-trash"></i>
                      </button>
                    </td>
                  </tr>
                ))}
                {donnees.content.length === 0 && (
                  <tr><td colSpan="6" className="text-center text-muted py-4">Aucun étudiant</td></tr>
                )}
              </tbody>
            </table>
          </div>
          <div className="card-footer bg-white d-flex justify-content-between align-items-center">
            <span className="small text-muted">{donnees.page.totalElements} étudiant(s)</span>
            {!rechercheActive && donnees.page.totalPages > 1 && (
              <nav>
                <ul className="pagination pagination-sm mb-0">
                  {Array.from({ length: donnees.page.totalPages }, (_, i) => (
                    <li key={i} className={`page-item ${i === donnees.page.number ? 'active' : ''}`}>
                      <button className="page-link" onClick={() => setPage(i)}>{i + 1}</button>
                    </li>
                  ))}
                </ul>
              </nav>
            )}
          </div>
        </div>
      )}

      <Modal show={!!form} onClose={() => setForm(null)} size="modal-lg"
        title={form?.id ? `Modifier l'étudiant #${form.id}` : 'Nouvel étudiant'}
        footer={<>
          <button className="btn btn-outline-secondary" onClick={() => setForm(null)}>Annuler</button>
          <button className="btn btn-primary" type="submit" form="form-etudiant">Enregistrer</button>
        </>}>
        <ErrorAlert error={erreurForm} />
        <form id="form-etudiant" className="row g-3" onSubmit={enregistrer}>
          <div className="col-md-4">
            <label className="form-label">Matricule</label>
            <input className="form-control" placeholder="ETU-2026-011" required {...champ('matricule')} />
          </div>
          <div className="col-md-4">
            <label className="form-label">Nom</label>
            <input className="form-control" required {...champ('nom')} />
          </div>
          <div className="col-md-4">
            <label className="form-label">Prénom</label>
            <input className="form-control" required {...champ('prenom')} />
          </div>
          <div className="col-md-6">
            <label className="form-label">Email</label>
            <input type="email" className="form-control" required {...champ('email')} />
          </div>
          <div className="col-md-6">
            <label className="form-label">Téléphone</label>
            <input className="form-control" placeholder="+216 22 345 678" {...champ('telephone')} />
          </div>
          <div className="col-md-4">
            <label className="form-label">Date de naissance</label>
            <input type="date" className="form-control" {...champ('dateNaissance')} />
          </div>
          <div className="col-md-4">
            <label className="form-label">Filière</label>
            <select className="form-select" {...champ('filiere')}>
              {FILIERES.map((f) => <option key={f}>{f}</option>)}
            </select>
          </div>
          <div className="col-md-4">
            <label className="form-label">Niveau</label>
            <select className="form-select" {...champ('niveau')}>
              {[1, 2, 3, 4, 5].map((n) => <option key={n}>{n}</option>)}
            </select>
          </div>
        </form>
      </Modal>

      <Toast message={toast} onClose={() => setToast('')} />
    </>
  );
}
