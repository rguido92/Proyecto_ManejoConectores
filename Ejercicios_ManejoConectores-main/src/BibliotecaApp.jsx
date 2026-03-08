import { useState, useEffect, useCallback } from "react";

// ── API helpers ──────────────────────────────────────────────────────────────
const BASE = "/api";
const api = {
  socios:    { all: () => fetch(`${BASE}/socios`).then(r => r.json()),
               get: id => fetch(`${BASE}/socios/${id}`).then(r => r.json()),
               create: d => fetch(`${BASE}/socios`, { method:"POST", headers:{"Content-Type":"application/json"}, body:JSON.stringify(d) }),
               update: (id,d) => fetch(`${BASE}/socios/${id}`, { method:"PUT", headers:{"Content-Type":"application/json"}, body:JSON.stringify(d) }),
               del: id => fetch(`${BASE}/socios/${id}`, { method:"DELETE" }) },
  libros:    { all: () => fetch(`${BASE}/libros`).then(r => r.json()),
               disponibles: () => fetch(`${BASE}/libros/disponibles`).then(r => r.json()),
               get: id => fetch(`${BASE}/libros/${id}`).then(r => r.json()),
               create: d => fetch(`${BASE}/libros`, { method:"POST", headers:{"Content-Type":"application/json"}, body:JSON.stringify(d) }),
               update: (id,d) => fetch(`${BASE}/libros/${id}`, { method:"PUT", headers:{"Content-Type":"application/json"}, body:JSON.stringify(d) }),
               del: id => fetch(`${BASE}/libros/${id}`, { method:"DELETE" }) },
  alquileres:{ activos: () => fetch(`${BASE}/alquileres/activos`).then(r => r.json()),
               historial: () => fetch(`${BASE}/alquileres/historial`).then(r => r.json()),
               crear: d => fetch(`${BASE}/alquileres`, { method:"POST", headers:{"Content-Type":"application/json"}, body:JSON.stringify(d) }),
               devolver: id => fetch(`${BASE}/alquileres/${id}/devolver`, { method:"PUT" }) },
};

// ── Estilos globales ─────────────────────────────────────────────────────────
const GlobalStyles = () => (
  <style>{`
    @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;600;700&family=Lato:wght@300;400;700&display=swap');

    *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

    :root {
      --cream:    #f5f0e8;
      --paper:    #ede8dc;
      --brown:    #3d2b1f;
      --brown-md: #6b4226;
      --brown-lt: #a0724a;
      --terra:    #c0522a;
      --terra-lt: #e07050;
      --gold:     #c8a857;
      --green:    #3a6b4a;
      --red:      #9b2c2c;
      --shadow:   0 4px 24px rgba(61,43,31,0.13);
      --radius:   10px;
    }

    html, body { height: 100%; }
    body {
      font-family: 'Lato', sans-serif;
      background: var(--cream);
      color: var(--brown);
      min-height: 100vh;
    }

    h1,h2,h3,h4 { font-family: 'Playfair Display', serif; }

    .app-shell {
      display: flex;
      min-height: 100vh;
    }

    /* ── Sidebar ── */
    .sidebar {
      width: 240px;
      min-height: 100vh;
      background: var(--brown);
      color: var(--cream);
      display: flex;
      flex-direction: column;
      padding: 0;
      position: fixed;
      top: 0; left: 0;
      z-index: 100;
    }
    .sidebar-header {
      padding: 32px 24px 24px;
      border-bottom: 1px solid rgba(255,255,255,0.1);
    }
    .sidebar-logo {
      font-family: 'Playfair Display', serif;
      font-size: 1.4rem;
      font-weight: 700;
      line-height: 1.2;
      color: var(--gold);
      margin-bottom: 4px;
    }
    .sidebar-sub {
      font-size: 0.72rem;
      letter-spacing: 0.12em;
      text-transform: uppercase;
      color: rgba(245,240,232,0.5);
    }
    .sidebar-nav {
      flex: 1;
      padding: 20px 0;
    }
    .nav-section-label {
      font-size: 0.65rem;
      letter-spacing: 0.14em;
      text-transform: uppercase;
      color: rgba(245,240,232,0.35);
      padding: 12px 24px 6px;
    }
    .nav-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 11px 24px;
      cursor: pointer;
      font-size: 0.88rem;
      font-weight: 400;
      color: rgba(245,240,232,0.75);
      border-left: 3px solid transparent;
      transition: all 0.18s ease;
      user-select: none;
    }
    .nav-item:hover { color: var(--cream); background: rgba(255,255,255,0.05); }
    .nav-item.active {
      color: var(--gold);
      border-left-color: var(--gold);
      background: rgba(200,168,87,0.1);
      font-weight: 700;
    }
    .nav-icon { font-size: 1rem; width: 20px; text-align: center; }

    /* ── Main ── */
    .main {
      margin-left: 240px;
      flex: 1;
      padding: 36px 40px;
      max-width: calc(100vw - 240px);
    }
    .page-header {
      margin-bottom: 28px;
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: 12px;
    }
    .page-title {
      font-size: 2rem;
      color: var(--brown);
      font-weight: 700;
    }
    .page-subtitle {
      font-size: 0.85rem;
      color: var(--brown-lt);
      margin-top: 2px;
    }

    /* ── Cards & Grid ── */
    .card {
      background: #fff;
      border-radius: var(--radius);
      box-shadow: var(--shadow);
      padding: 24px;
      border: 1px solid rgba(61,43,31,0.07);
    }
    .grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
    .grid-3 { display: grid; grid-template-columns: repeat(3,1fr); gap: 20px; }
    @media(max-width:900px){ .grid-2,.grid-3{ grid-template-columns:1fr; } }

    /* ── Stat cards ── */
    .stat-card {
      background: #fff;
      border-radius: var(--radius);
      padding: 20px 24px;
      border: 1px solid rgba(61,43,31,0.07);
      box-shadow: var(--shadow);
      display: flex;
      align-items: center;
      gap: 16px;
    }
    .stat-icon {
      font-size: 2rem;
      width: 52px; height: 52px;
      border-radius: 12px;
      display: flex; align-items: center; justify-content: center;
    }
    .stat-icon.brown { background: rgba(61,43,31,0.08); }
    .stat-icon.terra { background: rgba(192,82,42,0.1); }
    .stat-icon.gold  { background: rgba(200,168,87,0.12); }
    .stat-icon.green { background: rgba(58,107,74,0.1); }
    .stat-value { font-family:'Playfair Display',serif; font-size:1.9rem; font-weight:700; line-height:1; }
    .stat-label { font-size:0.8rem; color:var(--brown-lt); margin-top:3px; letter-spacing:0.04em; }

    /* ── Table ── */
    .table-wrap { overflow-x:auto; }
    table { width:100%; border-collapse:collapse; font-size:0.88rem; }
    thead tr { border-bottom:2px solid var(--paper); }
    th {
      text-align:left; padding:10px 14px;
      font-family:'Playfair Display',serif;
      font-size:0.82rem; font-weight:600;
      color:var(--brown-md);
      letter-spacing:0.03em;
      white-space:nowrap;
    }
    td { padding:11px 14px; border-bottom:1px solid var(--paper); color:var(--brown); vertical-align:middle; }
    tr:last-child td { border-bottom:none; }
    tr:hover td { background: rgba(245,240,232,0.5); }

    /* ── Buttons ── */
    .btn {
      display:inline-flex; align-items:center; gap:6px;
      padding:9px 18px; border-radius:7px; border:none;
      font-family:'Lato',sans-serif; font-size:0.85rem; font-weight:700;
      cursor:pointer; transition:all 0.15s ease; white-space:nowrap;
      letter-spacing:0.02em;
    }
    .btn-primary { background:var(--terra); color:#fff; }
    .btn-primary:hover { background:var(--terra-lt); }
    .btn-secondary { background:var(--brown); color:#fff; }
    .btn-secondary:hover { background:var(--brown-md); }
    .btn-outline { background:transparent; color:var(--brown-md); border:1.5px solid var(--brown-lt); }
    .btn-outline:hover { background:var(--paper); }
    .btn-danger { background:var(--red); color:#fff; }
    .btn-danger:hover { background:#b83232; }
    .btn-success { background:var(--green); color:#fff; }
    .btn-success:hover { background:#4a8a5a; }
    .btn-sm { padding:5px 12px; font-size:0.78rem; border-radius:5px; }
    .btn:disabled { opacity:0.5; cursor:not-allowed; }

    /* ── Form ── */
    .form-group { margin-bottom:16px; }
    label { display:block; font-size:0.8rem; font-weight:700; color:var(--brown-md); margin-bottom:5px; letter-spacing:0.04em; text-transform:uppercase; }
    input, select, textarea {
      width:100%; padding:9px 13px;
      border:1.5px solid rgba(61,43,31,0.18);
      border-radius:7px;
      font-family:'Lato',sans-serif; font-size:0.88rem;
      color:var(--brown); background:#fff;
      transition:border-color 0.15s;
      outline:none;
    }
    input:focus, select:focus, textarea:focus { border-color:var(--brown-md); }
    .form-row { display:grid; grid-template-columns:1fr 1fr; gap:14px; }

    /* ── Modal ── */
    .modal-backdrop {
      position:fixed; inset:0; background:rgba(30,20,10,0.45);
      display:flex; align-items:center; justify-content:center;
      z-index:1000; padding:20px;
      animation: fadeIn 0.15s ease;
    }
    @keyframes fadeIn { from{opacity:0} to{opacity:1} }
    .modal {
      background:#fff; border-radius:14px;
      width:100%; max-width:480px;
      box-shadow:0 20px 60px rgba(30,20,10,0.25);
      animation: slideUp 0.2s ease;
    }
    @keyframes slideUp { from{transform:translateY(16px);opacity:0} to{transform:translateY(0);opacity:1} }
    .modal-header {
      padding:22px 26px 16px;
      border-bottom:1px solid var(--paper);
      display:flex; align-items:center; justify-content:space-between;
    }
    .modal-title { font-size:1.2rem; font-weight:700; }
    .modal-body { padding:22px 26px; }
    .modal-footer { padding:14px 26px 20px; display:flex; justify-content:flex-end; gap:10px; }
    .btn-close { background:none; border:none; font-size:1.3rem; cursor:pointer; color:var(--brown-lt); line-height:1; padding:2px; }
    .btn-close:hover { color:var(--brown); }

    /* ── Toast ── */
    .toast-container { position:fixed; bottom:24px; right:24px; z-index:2000; display:flex; flex-direction:column; gap:8px; }
    .toast {
      padding:12px 20px; border-radius:8px;
      font-size:0.85rem; font-weight:700;
      box-shadow:0 4px 16px rgba(0,0,0,0.18);
      animation: slideInRight 0.25s ease;
      max-width:320px;
    }
    @keyframes slideInRight { from{transform:translateX(40px);opacity:0} to{transform:translateX(0);opacity:1} }
    .toast.success { background:var(--green); color:#fff; }
    .toast.error   { background:var(--red);   color:#fff; }
    .toast.info    { background:var(--brown);  color:var(--gold); }

    /* ── Badge ── */
    .badge {
      display:inline-block; padding:3px 10px; border-radius:20px;
      font-size:0.72rem; font-weight:700; letter-spacing:0.04em;
    }
    .badge-green { background:rgba(58,107,74,0.12); color:var(--green); }
    .badge-terra { background:rgba(192,82,42,0.12); color:var(--terra); }
    .badge-gold  { background:rgba(200,168,87,0.15); color:#8a6a10; }
    .badge-red   { background:rgba(155,44,44,0.1);  color:var(--red); }

    /* ── Empty state ── */
    .empty-state {
      text-align:center; padding:48px 24px;
      color:var(--brown-lt);
    }
    .empty-state .empty-icon { font-size:3rem; margin-bottom:12px; opacity:0.4; }
    .empty-state p { font-size:0.9rem; }

    /* ── Search bar ── */
    .search-bar { position:relative; }
    .search-bar input { padding-left:36px; }
    .search-bar::before { content:'🔍'; position:absolute; left:11px; top:50%; transform:translateY(-50%); font-size:0.85rem; pointer-events:none; }

    .section-title {
      font-family:'Playfair Display',serif;
      font-size:1.1rem; font-weight:600;
      color:var(--brown); margin-bottom:16px;
      padding-bottom:10px;
      border-bottom:1px solid var(--paper);
    }

    .loading { text-align:center; padding:40px; color:var(--brown-lt); font-style:italic; }
    .divider { height:1px; background:var(--paper); margin:20px 0; }
  `}</style>
);

// ── Toast system ─────────────────────────────────────────────────────────────
let toastId = 0;
function useToast() {
  const [toasts, setToasts] = useState([]);
  const add = useCallback((msg, type="info") => {
    const id = ++toastId;
    setToasts(p => [...p, {id, msg, type}]);
    setTimeout(() => setToasts(p => p.filter(t => t.id !== id)), 3500);
  }, []);
  return { toasts, toast: add };
}
function ToastContainer({ toasts }) {
  return (
    <div className="toast-container">
      {toasts.map(t => <div key={t.id} className={`toast ${t.type}`}>{t.msg}</div>)}
    </div>
  );
}

// ── Modal wrapper ─────────────────────────────────────────────────────────────
function Modal({ title, onClose, children, footer }) {
  return (
    <div className="modal-backdrop" onClick={e => e.target===e.currentTarget && onClose()}>
      <div className="modal">
        <div className="modal-header">
          <h3 className="modal-title">{title}</h3>
          <button className="btn-close" onClick={onClose}>×</button>
        </div>
        <div className="modal-body">{children}</div>
        {footer && <div className="modal-footer">{footer}</div>}
      </div>
    </div>
  );
}

// ── Dashboard ─────────────────────────────────────────────────────────────────
function Dashboard() {
  const [stats, setStats] = useState({ socios:0, disponibles:0, alquilados:0, historial:0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.allSettled([
      api.socios.all(),
      api.libros.disponibles(),
      api.alquileres.activos(),
      api.alquileres.historial(),
    ]).then(([s,d,a,h]) => {
      setStats({
        socios:    s.status==="fulfilled" ? (s.value?.length??0) : 0,
        disponibles: d.status==="fulfilled" ? (d.value?.length??0) : 0,
        alquilados: a.status==="fulfilled" ? (a.value?.length??0) : 0,
        historial:  h.status==="fulfilled" ? (h.value?.length??0) : 0,
      });
      setLoading(false);
    });
  }, []);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Panel Principal</h1>
          <div className="page-subtitle">Resumen general de la biblioteca</div>
        </div>
      </div>

      {loading ? <div className="loading">Cargando estadísticas…</div> : (
        <div className="grid-3" style={{marginBottom:32}}>
          <div className="stat-card">
            <div className="stat-icon brown">👥</div>
            <div><div className="stat-value">{stats.socios}</div><div className="stat-label">Socios registrados</div></div>
          </div>
          <div className="stat-card">
            <div className="stat-icon green">📗</div>
            <div><div className="stat-value">{stats.disponibles}</div><div className="stat-label">Libros disponibles</div></div>
          </div>
          <div className="stat-card">
            <div className="stat-icon terra">📕</div>
            <div><div className="stat-value">{stats.alquilados}</div><div className="stat-label">Alquileres activos</div></div>
          </div>
        </div>
      )}

      <div className="card" style={{marginBottom:24}}>
        <div className="section-title">📚 Bienvenido al Sistema de Gestión</div>
        <p style={{fontSize:"0.9rem", color:"var(--brown-md)", lineHeight:1.7}}>
          Gestiona los socios, libros y alquileres de tu biblioteca desde este panel centralizado.
          Usa la navegación lateral para acceder a cada sección.
        </p>
        <div style={{marginTop:20, display:"flex", gap:12, flexWrap:"wrap"}}>
          <div style={{padding:"12px 18px", background:"var(--paper)", borderRadius:"8px", fontSize:"0.83rem"}}>
            📖 <strong>Socios</strong> — Ver, añadir y gestionar socios
          </div>
          <div style={{padding:"12px 18px", background:"var(--paper)", borderRadius:"8px", fontSize:"0.83rem"}}>
            📗 <strong>Libros disponibles</strong> — Catálogo libre para alquilar
          </div>
          <div style={{padding:"12px 18px", background:"var(--paper)", borderRadius:"8px", fontSize:"0.83rem"}}>
            📕 <strong>Alquileres activos</strong> — Gestión de préstamos en curso
          </div>
          <div style={{padding:"12px 18px", background:"var(--paper)", borderRadius:"8px", fontSize:"0.83rem"}}>
            🕑 <strong>Historial</strong> — Registro completo de préstamos pasados
          </div>
        </div>
      </div>
    </div>
  );
}

// ── Socios ────────────────────────────────────────────────────────────────────
function SocioForm({ initial={}, onSubmit, onCancel, loading }) {
  const [form, setForm] = useState({ nombre:"", apellido:"", email:"", telefono:"", ...initial });
  const set = k => e => setForm(p => ({...p, [k]:e.target.value}));
  return (
    <form onSubmit={e=>{e.preventDefault();onSubmit(form);}}>
      <div className="form-row">
        <div className="form-group"><label>Nombre *</label><input required value={form.nombre} onChange={set("nombre")} placeholder="Juan"/></div>
        <div className="form-group"><label>Apellido *</label><input required value={form.apellido} onChange={set("apellido")} placeholder="García"/></div>
      </div>
      <div className="form-group"><label>Email</label><input type="email" value={form.email} onChange={set("email")} placeholder="juan@email.com"/></div>
      <div className="form-group"><label>Teléfono</label><input value={form.telefono} onChange={set("telefono")} placeholder="600 000 000"/></div>
      <div style={{display:"flex",gap:10,justifyContent:"flex-end",marginTop:8}}>
        <button type="button" className="btn btn-outline" onClick={onCancel}>Cancelar</button>
        <button type="submit" className="btn btn-primary" disabled={loading}>{loading?"Guardando…":"Guardar"}</button>
      </div>
    </form>
  );
}

function GestionSocios({ toast }) {
  const [socios, setSocios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [modal, setModal] = useState(null); // null | {mode:"create"|"edit", data?}
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try { setSocios(await api.socios.all()); } catch { toast("Error cargando socios","error"); }
    setLoading(false);
  }, [toast]);

  useEffect(()=>{ load(); },[load]);

  const filtered = socios.filter(s =>
    `${s.nombre} ${s.apellido} ${s.email||""} ${s.id}`.toLowerCase().includes(search.toLowerCase())
  );

  const handleCreate = async (data) => {
    setSaving(true);
    try {
      const res = await api.socios.create(data);
      if(res.ok||res.status===201){ toast("Socio creado ✓","success"); setModal(null); load(); }
      else toast("Error creando socio","error");
    } catch { toast("Error de red","error"); }
    setSaving(false);
  };
  const handleEdit = async (data) => {
    setSaving(true);
    try {
      const res = await api.socios.update(modal.data.id, data);
      if(res.ok){ toast("Socio actualizado ✓","success"); setModal(null); load(); }
      else toast("Error actualizando","error");
    } catch { toast("Error de red","error"); }
    setSaving(false);
  };
  const handleDelete = async (s) => {
    if(!confirm(`¿Eliminar a ${s.nombre} ${s.apellido}?`)) return;
    try {
      const res = await api.socios.del(s.id);
      if(res.ok){ toast("Socio eliminado","info"); load(); }
      else toast("Error eliminando","error");
    } catch { toast("Error de red","error"); }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Socios</h1>
          <div className="page-subtitle">{socios.length} socios registrados</div>
        </div>
        <button className="btn btn-primary" onClick={()=>setModal({mode:"create"})}>+ Nuevo socio</button>
      </div>

      <div className="card">
        <div style={{marginBottom:16}}>
          <div className="search-bar"><input placeholder="Buscar por nombre, apellido o email…" value={search} onChange={e=>setSearch(e.target.value)}/></div>
        </div>
        {loading ? <div className="loading">Cargando…</div> : filtered.length===0 ? (
          <div className="empty-state"><div className="empty-icon">👤</div><p>No hay socios{search?" que coincidan con la búsqueda":""}.</p></div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>#</th><th>Nombre</th><th>Apellido</th><th>Email</th><th>Teléfono</th><th>Acciones</th></tr></thead>
              <tbody>
                {filtered.map(s => (
                  <tr key={s.id}>
                    <td><span className="badge badge-gold">{s.id}</span></td>
                    <td><strong>{s.nombre}</strong></td>
                    <td>{s.apellido}</td>
                    <td style={{color:"var(--brown-lt)"}}>{s.email||"—"}</td>
                    <td style={{color:"var(--brown-lt)"}}>{s.telefono||"—"}</td>
                    <td>
                      <div style={{display:"flex",gap:6}}>
                        <button className="btn btn-outline btn-sm" onClick={()=>setModal({mode:"edit",data:s})}>✏️ Editar</button>
                        <button className="btn btn-danger btn-sm" onClick={()=>handleDelete(s)}>🗑</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {modal && (
        <Modal title={modal.mode==="create"?"Nuevo Socio":"Editar Socio"} onClose={()=>setModal(null)}>
          <SocioForm
            initial={modal.data}
            onSubmit={modal.mode==="create" ? handleCreate : handleEdit}
            onCancel={()=>setModal(null)}
            loading={saving}
          />
        </Modal>
      )}
    </div>
  );
}

// ── Libros Disponibles ────────────────────────────────────────────────────────
function LibroForm({ initial={}, socios=[], onSubmit, onCancel, loading, mode }) {
  const [form, setForm] = useState({ titulo:"", autor:"", isbn:"", anio:"", socioId:"", ...initial });
  const set = k => e => setForm(p => ({...p, [k]:e.target.value}));
  return (
    <form onSubmit={e=>{e.preventDefault();onSubmit(form);}}>
      <div className="form-group"><label>Título *</label><input required value={form.titulo} onChange={set("titulo")} placeholder="El Quijote"/></div>
      <div className="form-row">
        <div className="form-group"><label>Autor *</label><input required value={form.autor} onChange={set("autor")} placeholder="Miguel de Cervantes"/></div>
        <div className="form-group"><label>Año</label><input type="number" value={form.anio} onChange={set("anio")} placeholder="1605"/></div>
      </div>
      <div className="form-group"><label>ISBN</label><input value={form.isbn} onChange={set("isbn")} placeholder="978-..."/></div>
      {mode==="alquilar" && (
        <div className="form-group">
          <label>Socio *</label>
          <select required value={form.socioId} onChange={set("socioId")}>
            <option value="">Selecciona un socio…</option>
            {socios.map(s=><option key={s.id} value={s.id}>{s.nombre} {s.apellido}</option>)}
          </select>
        </div>
      )}
      <div style={{display:"flex",gap:10,justifyContent:"flex-end",marginTop:8}}>
        <button type="button" className="btn btn-outline" onClick={onCancel}>Cancelar</button>
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading?"Guardando…": mode==="alquilar"?"Alquilar":"Guardar"}
        </button>
      </div>
    </form>
  );
}

function LibrosDisponibles({ toast }) {
  const [libros, setLibros] = useState([]);
  const [socios, setSocios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [modal, setModal] = useState(null);
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [l, s] = await Promise.all([api.libros.disponibles(), api.socios.all()]);
      setLibros(l); setSocios(s);
    } catch { toast("Error cargando datos","error"); }
    setLoading(false);
  }, [toast]);

  useEffect(()=>{ load(); },[load]);

  const filtered = libros.filter(l =>
    `${l.titulo} ${l.autor} ${l.isbn||""}`.toLowerCase().includes(search.toLowerCase())
  );

  const handleCreate = async (data) => {
    setSaving(true);
    try {
      const res = await api.libros.create(data);
      if(res.ok||res.status===201){ toast("Libro añadido ✓","success"); setModal(null); load(); }
      else toast("Error añadiendo libro","error");
    } catch { toast("Error de red","error"); }
    setSaving(false);
  };

  const handleAlquilar = async (data) => {
    setSaving(true);
    try {
      const res = await api.alquileres.crear({ libroId: modal.data.id, socioId: data.socioId });
      if(res.ok||res.status===201){ toast("Libro alquilado ✓","success"); setModal(null); load(); }
      else toast("Error al alquilar","error");
    } catch { toast("Error de red","error"); }
    setSaving(false);
  };

  const handleDelete = async (l) => {
    if(!confirm(`¿Eliminar "${l.titulo}"?`)) return;
    try {
      const res = await api.libros.del(l.id);
      if(res.ok){ toast("Libro eliminado","info"); load(); }
      else toast("Error eliminando","error");
    } catch { toast("Error de red","error"); }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Libros Disponibles</h1>
          <div className="page-subtitle">{libros.length} libros sin alquilar</div>
        </div>
        <button className="btn btn-primary" onClick={()=>setModal({mode:"create"})}>+ Añadir libro</button>
      </div>

      <div className="card">
        <div style={{marginBottom:16}}>
          <div className="search-bar"><input placeholder="Buscar por título, autor o ISBN…" value={search} onChange={e=>setSearch(e.target.value)}/></div>
        </div>
        {loading ? <div className="loading">Cargando…</div> : filtered.length===0 ? (
          <div className="empty-state"><div className="empty-icon">📗</div><p>No hay libros disponibles{search?" que coincidan":""}.{!search&&" ¡Añade uno!"}</p></div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>#</th><th>Título</th><th>Autor</th><th>Año</th><th>ISBN</th><th>Acciones</th></tr></thead>
              <tbody>
                {filtered.map(l => (
                  <tr key={l.id}>
                    <td><span className="badge badge-green">{l.id}</span></td>
                    <td><strong>{l.titulo}</strong></td>
                    <td>{l.autor}</td>
                    <td style={{color:"var(--brown-lt)"}}>{l.anio||"—"}</td>
                    <td style={{color:"var(--brown-lt)",fontSize:"0.8rem"}}>{l.isbn||"—"}</td>
                    <td>
                      <div style={{display:"flex",gap:6}}>
                        <button className="btn btn-success btn-sm" onClick={()=>setModal({mode:"alquilar",data:l})}>📤 Alquilar</button>
                        <button className="btn btn-outline btn-sm" onClick={()=>setModal({mode:"edit",data:l})}>✏️</button>
                        <button className="btn btn-danger btn-sm" onClick={()=>handleDelete(l)}>🗑</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {modal && modal.mode==="create" && (
        <Modal title="Añadir Libro" onClose={()=>setModal(null)}>
          <LibroForm onSubmit={handleCreate} onCancel={()=>setModal(null)} loading={saving}/>
        </Modal>
      )}
      {modal && modal.mode==="alquilar" && (
        <Modal title={`Alquilar: "${modal.data.titulo}"`} onClose={()=>setModal(null)}>
          <LibroForm mode="alquilar" socios={socios} initial={modal.data} onSubmit={handleAlquilar} onCancel={()=>setModal(null)} loading={saving}/>
        </Modal>
      )}
      {modal && modal.mode==="edit" && (
        <Modal title="Editar Libro" onClose={()=>setModal(null)}>
          <LibroForm initial={modal.data} onSubmit={async(d)=>{
            setSaving(true);
            try{
              const res=await api.libros.update(modal.data.id,d);
              if(res.ok){toast("Libro actualizado ✓","success");setModal(null);load();}
              else toast("Error actualizando","error");
            }catch{toast("Error de red","error");}
            setSaving(false);
          }} onCancel={()=>setModal(null)} loading={saving}/>
        </Modal>
      )}
    </div>
  );
}

// ── Alquileres Activos ────────────────────────────────────────────────────────
function AlquileresActivos({ toast }) {
  const [alquileres, setAlquileres] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");

  const load = useCallback(async () => {
    setLoading(true);
    try { setAlquileres(await api.alquileres.activos()); }
    catch { toast("Error cargando alquileres","error"); }
    setLoading(false);
  }, [toast]);

  useEffect(()=>{ load(); },[load]);

  const filtered = alquileres.filter(a =>
    `${a.libro?.titulo||""} ${a.socio?.nombre||""} ${a.socio?.apellido||""}`.toLowerCase().includes(search.toLowerCase())
  );

  const handleDevolver = async (a) => {
    if(!confirm(`¿Marcar como devuelto "${a.libro?.titulo||"libro"}"?`)) return;
    try {
      const res = await api.alquileres.devolver(a.id);
      if(res.ok){ toast("Libro devuelto ✓","success"); load(); }
      else toast("Error al devolver","error");
    } catch { toast("Error de red","error"); }
  };

  const diasAlquilado = (fecha) => {
    if(!fecha) return "—";
    const diff = Math.floor((Date.now() - new Date(fecha)) / 86400000);
    return `${diff} día${diff!==1?"s":""}`;
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Alquileres Activos</h1>
          <div className="page-subtitle">{alquileres.length} préstamos en curso</div>
        </div>
      </div>

      <div className="card">
        <div style={{marginBottom:16}}>
          <div className="search-bar"><input placeholder="Buscar por libro o socio…" value={search} onChange={e=>setSearch(e.target.value)}/></div>
        </div>
        {loading ? <div className="loading">Cargando…</div> : filtered.length===0 ? (
          <div className="empty-state"><div className="empty-icon">📕</div><p>No hay alquileres activos{search?" que coincidan":""}.{!search&&" ¡Todo devuelto!"}</p></div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>#</th><th>Libro</th><th>Autor</th><th>Socio</th><th>Fecha alquiler</th><th>Días</th><th>Acción</th></tr></thead>
              <tbody>
                {filtered.map(a => (
                  <tr key={a.id}>
                    <td><span className="badge badge-terra">{a.id}</span></td>
                    <td><strong>{a.libro?.titulo||a.libroTitulo||"—"}</strong></td>
                    <td style={{color:"var(--brown-lt)"}}>{a.libro?.autor||a.libroAutor||"—"}</td>
                    <td>{a.socio?.nombre||a.socioNombre||"—"} {a.socio?.apellido||a.socioApellido||""}</td>
                    <td style={{fontSize:"0.82rem",color:"var(--brown-lt)"}}>{a.fechaAlquiler ? new Date(a.fechaAlquiler).toLocaleDateString("es-ES") : "—"}</td>
                    <td><span className="badge badge-terra">{diasAlquilado(a.fechaAlquiler)}</span></td>
                    <td>
                      <button className="btn btn-success btn-sm" onClick={()=>handleDevolver(a)}>📥 Devolver</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

// ── Historial ─────────────────────────────────────────────────────────────────
function Historial({ toast }) {
  const [historial, setHistorial] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");

  const load = useCallback(async () => {
    setLoading(true);
    try { setHistorial(await api.alquileres.historial()); }
    catch { toast("Error cargando historial","error"); }
    setLoading(false);
  }, [toast]);

  useEffect(()=>{ load(); },[load]);

  const filtered = historial.filter(a =>
    `${a.libro?.titulo||a.libroTitulo||""} ${a.socio?.nombre||a.socioNombre||""} ${a.socio?.apellido||a.socioApellido||""}`.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Historial de Alquileres</h1>
          <div className="page-subtitle">{historial.length} préstamos completados en total</div>
        </div>
      </div>

      <div className="card">
        <div style={{marginBottom:16}}>
          <div className="search-bar"><input placeholder="Buscar en el historial…" value={search} onChange={e=>setSearch(e.target.value)}/></div>
        </div>
        {loading ? <div className="loading">Cargando historial…</div> : filtered.length===0 ? (
          <div className="empty-state"><div className="empty-icon">🕑</div><p>No hay historial{search?" que coincida":""}.</p></div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>#</th><th>Libro</th><th>Autor</th><th>Socio</th><th>Fecha alquiler</th><th>Fecha devolución</th><th>Duración</th></tr></thead>
              <tbody>
                {filtered.map(a => {
                  const dias = a.fechaAlquiler && a.fechaDevolucion
                    ? Math.floor((new Date(a.fechaDevolucion) - new Date(a.fechaAlquiler)) / 86400000)
                    : null;
                  return (
                    <tr key={a.id}>
                      <td><span className="badge badge-gold">{a.id}</span></td>
                      <td><strong>{a.libro?.titulo||a.libroTitulo||"—"}</strong></td>
                      <td style={{color:"var(--brown-lt)"}}>{a.libro?.autor||a.libroAutor||"—"}</td>
                      <td>{a.socio?.nombre||a.socioNombre||"—"} {a.socio?.apellido||a.socioApellido||""}</td>
                      <td style={{fontSize:"0.82rem"}}>{a.fechaAlquiler ? new Date(a.fechaAlquiler).toLocaleDateString("es-ES") : "—"}</td>
                      <td style={{fontSize:"0.82rem"}}>{a.fechaDevolucion ? new Date(a.fechaDevolucion).toLocaleDateString("es-ES") : <span className="badge badge-terra">Activo</span>}</td>
                      <td>{dias!==null ? <span className="badge badge-green">{dias}d</span> : "—"}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

// ── App Shell ─────────────────────────────────────────────────────────────────
const PAGES = [
  { id:"dashboard", icon:"🏛", label:"Panel principal" },
  { id:"socios",    icon:"👥", label:"Gestión de socios" },
  { id:"disponibles", icon:"📗", label:"Libros disponibles" },
  { id:"activos",   icon:"📕", label:"Alquileres activos" },
  { id:"historial", icon:"🕑", label:"Historial" },
];

export default function App() {
  const [page, setPage] = useState("dashboard");
  const { toasts, toast } = useToast();

  const renderPage = () => {
    switch(page) {
      case "dashboard":    return <Dashboard />;
      case "socios":       return <GestionSocios toast={toast}/>;
      case "disponibles":  return <LibrosDisponibles toast={toast}/>;
      case "activos":      return <AlquileresActivos toast={toast}/>;
      case "historial":    return <Historial toast={toast}/>;
      default: return null;
    }
  };

  return (
    <>
      <GlobalStyles />
      <div className="app-shell">
        <aside className="sidebar">
          <div className="sidebar-header">
            <div className="sidebar-logo">📚 Biblioteca</div>
            <div className="sidebar-sub">Sistema de Gestión</div>
          </div>
          <nav className="sidebar-nav">
            <div className="nav-section-label">Navegación</div>
            {PAGES.map(p => (
              <div
                key={p.id}
                className={`nav-item${page===p.id?" active":""}`}
                onClick={()=>setPage(p.id)}
              >
                <span className="nav-icon">{p.icon}</span>
                {p.label}
              </div>
            ))}
          </nav>
          <div style={{padding:"16px 24px", borderTop:"1px solid rgba(255,255,255,0.08)", fontSize:"0.72rem", color:"rgba(245,240,232,0.3)"}}>
            Proyecto ManejoConectores · DAM
          </div>
        </aside>
        <main className="main">
          {renderPage()}
        </main>
      </div>
      <ToastContainer toasts={toasts}/>
    </>
  );
}
