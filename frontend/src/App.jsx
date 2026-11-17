import { NavLink, Navigate, Route, Routes } from 'react-router-dom';
import { API_URL } from './api.js';
import Dashboard from './pages/Dashboard.jsx';
import Etudiants from './pages/Etudiants.jsx';
import Catalogue from './pages/Catalogue.jsx';
import Inscriptions from './pages/Inscriptions.jsx';
import Releve from './pages/Releve.jsx';

const LIENS = [
  { to: '/', label: 'Tableau de bord', icon: 'speedometer2', end: true },
  { to: '/etudiants', label: 'Étudiants', icon: 'people' },
  { to: '/catalogue', label: 'Modules & Cours', icon: 'book' },
  { to: '/inscriptions', label: 'Inscriptions', icon: 'journal-check' },
  { to: '/releve', label: 'Relevé de notes', icon: 'award' },
];

export default function App() {
  return (
    <>
      <nav className="navbar navbar-expand-lg navbar-dark bg-brand shadow-sm">
        <div className="container-fluid">
          <NavLink className="navbar-brand d-flex align-items-center gap-2" to="/">
            <i className="bi bi-mortarboard-fill"></i>
            <span>Scolarité <small className="opacity-75 fw-light">· microservices</small></span>
          </NavLink>
          <div className="navbar-nav flex-row flex-wrap gap-1">
            {LIENS.map((l) => (
              <NavLink key={l.to} to={l.to} end={l.end} className="nav-link px-2">
                <i className={`bi bi-${l.icon} me-1`}></i>
                {l.label}
              </NavLink>
            ))}
          </div>
        </div>
      </nav>

      <main className="container-xl py-4">
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/etudiants" element={<Etudiants />} />
          <Route path="/catalogue" element={<Catalogue />} />
          <Route path="/inscriptions" element={<Inscriptions />} />
          <Route path="/releve" element={<Releve />} />
          <Route path="/releve/:etudiantId" element={<Releve />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>

      <footer className="text-center text-muted small pb-4 no-print">
        Toutes les requêtes passent par l'API Gateway : <code>{API_URL}</code>
      </footer>
    </>
  );
}
