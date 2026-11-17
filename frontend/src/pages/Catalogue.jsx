import { useCallback, useEffect, useState } from 'react';
import { api, estIndisponible, SEMESTRES, TYPES_COURS } from '../api.js';
import { ErrorAlert, Modal, ServiceDown, Spinner, Toast } from '../components/ui.jsx';

const MODULE_VIDE = { code: '', intitule: '', semestre: 'S9', coefficient: 2, credits: 4 };
const COURS_VIDE = { code: '', titre: '', description: '', volumeHoraire: 21, enseignant: '', capaciteMax: 30, type: 'COURS', moduleId: '' };
const TYPE_STYLE = { COURS: 'primary', TD: 'info', TP: 'success' };

export default function Catalogue() {
  const [modules, setModules] = useState(null);
  const [cours, setCours] = useState(null);
  const [selection, setSelection] = useState(null); // id du module sélectionné
  const [recherche, setRecherche] = useState('');
  const [erreur, setErreur] = useState(null);
  const [formModule, setFormModule] = useState(null);
  const [formCours, setFormCours] = useState(null);
  const [erreurForm, setErreurForm] = useState(null);
  const [toast, setToast] = useState('');

  const charger = useCallback(async () => {
    setErreur(null);
    try {
      const [m, c] = await Promise.all([
        api('/api/modules'),
        recherche
          ? api(`/api/cours/search?titre=${encodeURIComponent(recherche)}`)
          : selection
            ? api(`/api/modules/${selection}/cours`)
            : api('/api/cours'),
      ]);
      setModules(m);
      setCours(c);
    } catch (e) {
      setErreur(e);
    }
  }, [selection, recherche]);

  useEffect(() => {
    charger();
  }, [charger]);

  const soumettre = async (e, url, form, fermer, conversion) => {
    e.preventDefault();
    setErreurForm(null);
    const { id, ...corps } = form;
    try {
      await api(id ? `${url}/${id}` : url, { method: id ? 'PUT' : 'POST', body: conversion(corps) });
      fermer(null);
      setToast(id ? 'Modification enregistrée' : 'Création réussie');
      charger();
    } catch (err) {
      setErreurForm(err);
    }
  };

  const supprimer = async (url, libelle) => {
    if (!window.confirm(`Supprimer ${libelle} ?`)) return;
    try {
      await api(url, { method: 'DELETE' });
      setToast(`${libelle} supprimé`);
      charger();
    } catch (err) {
      setErreur(err);
    }
  };

  const lier = (form, setForm) => (nom) => ({
    value: form?.[nom] ?? '',
    onChange: (e) => setForm({ ...form, [nom]: e.target.value }),
  });
  const champM = lier(formModule, setFormModule);
  const champC = lier(formCours, setFormCours);
  const moduleCourant = modules?.find((m) => m.id === selection);

  if (erreur && estIndisponible(erreur)) return <ServiceDown service="COURS-SERVICE" error={erreur} />;

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2 className="page-title mb-0">
          <i className="bi bi-book me-2"></i>Modules & Cours
        </h2>
        <div className="d-flex gap-2">
          <button className="btn btn-outline-primary" onClick={() => { setErreurForm(null); setFormModule({ ...MODULE_VIDE }); }}>
            <i className="bi bi-collection me-1"></i>Nouveau module
          </button>
          <button className="btn btn-primary" disabled={!modules?.length}
            onClick={() => { setErreurForm(null); setFormCours({ ...COURS_VIDE, moduleId: selection ?? modules?.[0]?.id ?? '' }); }}>
            <i className="bi bi-plus-lg me-1"></i>Nouveau cours
          </button>
        </div>
      </div>

      <ErrorAlert error={erreur} onClose={() => setErreur(null)} />
      {!modules && !erreur && <Spinner />}

      {modules && (
        <div className="row g-3">
          <div className="col-lg-4">
            <div className="card">
              <div className="card-header bg-white fw-semibold">Unités d'enseignement</div>
              <div className="list-group list-group-flush">
                <button className={`list-group-item list-group-item-action ${selection === null ? 'active' : ''}`}
                  onClick={() => { setSelection(null); setRecherche(''); }}>
                  <i className="bi bi-grid me-2"></i>Tous les cours
                </button>
                {modules.map((m) => (
                  <div key={m.id}
                    className={`list-group-item list-group-item-action d-flex justify-content-between align-items-start ${selection === m.id ? 'active' : ''}`}
                    role="button" onClick={() => { setSelection(m.id); setRecherche(''); }}>
                    <div>
                      <div className="fw-semibold">{m.code}</div>
                      <div className="small">{m.intitule}</div>
                      <div className="small opacity-75">
                        {m.semestre} · coef. {m.coefficient} · {m.credits} ECTS · {m.nombreCours} cours
                      </div>
                    </div>
                    <div className="btn-group btn-group-sm" onClick={(e) => e.stopPropagation()}>
                      <button className="btn btn-light" title="Modifier"
                        onClick={() => { setErreurForm(null); setFormModule({ ...m }); }}>
                        <i className="bi bi-pencil"></i>
                      </button>
                      <button className="btn btn-light text-danger" title="Supprimer"
                        onClick={() => supprimer(`/api/modules/${m.id}`, `le module ${m.code} (et ses cours)`)}>
                        <i className="bi bi-trash"></i>
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="col-lg-8">
            <div className="card">
              <div className="card-header bg-white d-flex justify-content-between align-items-center gap-2">
                <span className="fw-semibold">
                  {moduleCourant ? `Cours du module ${moduleCourant.code}` : recherche ? 'Résultats de recherche' : 'Tous les cours'}
                </span>
                <input className="form-control form-control-sm w-auto" placeholder="Rechercher un titre…"
                  value={recherche} onChange={(e) => setRecherche(e.target.value)} />
              </div>
              <div className="table-responsive">
                <table className="table table-hover mb-0">
                  <thead className="table-light">
                    <tr>
                      <th>Code</th><th>Titre</th><th>Type</th><th>Enseignant</th><th>Vol.</th><th>Capacité</th><th></th>
                    </tr>
                  </thead>
                  <tbody>
                    {cours?.map((c) => (
                      <tr key={c.id}>
                        <td className="text-nowrap"><code>{c.code}</code><div className="small text-muted">#{c.id}</div></td>
                        <td>
                          {c.titre}
                          <div className="small text-muted">{c.module?.code} · {c.description}</div>
                        </td>
                        <td><span className={`badge text-bg-${TYPE_STYLE[c.type]}`}>{c.type}</span></td>
                        <td className="small">{c.enseignant}</td>
                        <td className="small">{c.volumeHoraire} h</td>
                        <td>{c.capaciteMax}</td>
                        <td className="text-end text-nowrap">
                          <button className="btn btn-sm btn-outline-primary me-1"
                            onClick={() => { setErreurForm(null); setFormCours({ ...COURS_VIDE, ...c, description: c.description ?? '', moduleId: c.module?.id }); }}>
                            <i className="bi bi-pencil"></i>
                          </button>
                          <button className="btn btn-sm btn-outline-danger" onClick={() => supprimer(`/api/cours/${c.id}`, `le cours ${c.code}`)}>
                            <i className="bi bi-trash"></i>
                          </button>
                        </td>
                      </tr>
                    ))}
                    {cours?.length === 0 && <tr><td colSpan="7" className="text-center text-muted py-4">Aucun cours</td></tr>}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      )}

      <Modal show={!!formModule} onClose={() => setFormModule(null)}
        title={formModule?.id ? `Modifier le module ${formModule.code}` : 'Nouveau module (UE)'}
        footer={<>
          <button className="btn btn-outline-secondary" onClick={() => setFormModule(null)}>Annuler</button>
          <button className="btn btn-primary" type="submit" form="form-module">Enregistrer</button>
        </>}>
        <ErrorAlert error={erreurForm} />
        <form id="form-module" className="row g-3"
          onSubmit={(e) => soumettre(e, '/api/modules', formModule, setFormModule,
            ({ nombreCours: _ignore, ...c }) => ({ ...c, coefficient: Number(c.coefficient), credits: Number(c.credits) }))}>
          <div className="col-md-5">
            <label className="form-label">Code</label>
            <input className="form-control" placeholder="UE-CLOUD" required {...champM('code')} />
          </div>
          <div className="col-md-7">
            <label className="form-label">Intitulé</label>
            <input className="form-control" required {...champM('intitule')} />
          </div>
          <div className="col-md-4">
            <label className="form-label">Semestre</label>
            <select className="form-select" {...champM('semestre')}>{SEMESTRES.map((s) => <option key={s}>{s}</option>)}</select>
          </div>
          <div className="col-md-4">
            <label className="form-label">Coefficient</label>
            <input type="number" step="0.5" min="0.5" className="form-control" required {...champM('coefficient')} />
          </div>
          <div className="col-md-4">
            <label className="form-label">Crédits ECTS</label>
            <input type="number" min="1" className="form-control" required {...champM('credits')} />
          </div>
        </form>
      </Modal>

      <Modal show={!!formCours} onClose={() => setFormCours(null)} size="modal-lg"
        title={formCours?.id ? `Modifier le cours ${formCours.code}` : 'Nouveau cours'}
        footer={<>
          <button className="btn btn-outline-secondary" onClick={() => setFormCours(null)}>Annuler</button>
          <button className="btn btn-primary" type="submit" form="form-cours">Enregistrer</button>
        </>}>
        <ErrorAlert error={erreurForm} />
        <form id="form-cours" className="row g-3"
          onSubmit={(e) => soumettre(e, '/api/cours', formCours, setFormCours,
            ({ module: _m, ...c }) => ({ ...c, volumeHoraire: Number(c.volumeHoraire), capaciteMax: Number(c.capaciteMax), moduleId: Number(c.moduleId) }))}>
          <div className="col-md-4">
            <label className="form-label">Code</label>
            <input className="form-control" placeholder="JEE2-TD" required {...champC('code')} />
          </div>
          <div className="col-md-8">
            <label className="form-label">Titre</label>
            <input className="form-control" required {...champC('titre')} />
          </div>
          <div className="col-12">
            <label className="form-label">Description</label>
            <textarea className="form-control" rows="2" {...champC('description')} />
          </div>
          <div className="col-md-6">
            <label className="form-label">Module</label>
            <select className="form-select" required {...champC('moduleId')}>
              {modules?.map((m) => <option key={m.id} value={m.id}>{m.code} — {m.intitule}</option>)}
            </select>
          </div>
          <div className="col-md-6">
            <label className="form-label">Enseignant</label>
            <input className="form-control" required {...champC('enseignant')} />
          </div>
          <div className="col-md-4">
            <label className="form-label">Type</label>
            <select className="form-select" {...champC('type')}>{TYPES_COURS.map((t) => <option key={t}>{t}</option>)}</select>
          </div>
          <div className="col-md-4">
            <label className="form-label">Volume horaire</label>
            <input type="number" min="1" className="form-control" required {...champC('volumeHoraire')} />
          </div>
          <div className="col-md-4">
            <label className="form-label">Capacité max</label>
            <input type="number" min="1" className="form-control" required {...champC('capaciteMax')} />
          </div>
        </form>
      </Modal>

      <Toast message={toast} onClose={() => setToast('')} />
    </>
  );
}
