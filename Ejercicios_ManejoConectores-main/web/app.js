// API endpoints
const API = {
  students: '/api/students',
  socios: '/api/socios',
  libros: '/api/libros',
  librosDisponibles: '/api/libros/disponibles',
  librosAlquilados: '/api/libros/alquilados',
  alquileres: '/api/alquileres',
  alquileresActivos: '/api/alquileres/activos',
  alquileresHistorial: '/api/alquileres/historial',
  empleados: '/api/empleados'
};

// Global data storage
const dataCache = {
  estudiantes: [],
  socios: [],
  libros: [],
  alquileres: [],
  empleados: []
};

// Utility functions
function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

function showMsg(tabName, msg, isError) {
  const el = document.getElementById(`msg-${tabName}`);
  if (!el) return;
  el.textContent = msg;
  el.className = 'message ' + (isError ? 'error' : 'success');
  setTimeout(() => {
    el.className = 'message';
    el.textContent = '';
  }, 5000);
}

// Tab Management
function switchTab(tabName) {
  // Hide all tabs
  document.querySelectorAll('.tab-content').forEach(tab => {
    tab.classList.remove('active');
  });
  
  // Remove active class from all buttons
  document.querySelectorAll('.tab-button').forEach(btn => {
    btn.classList.remove('active');
  });
  
  // Show selected tab
  const tab = document.getElementById(`${tabName}-tab`);
  if (tab) {
    tab.classList.add('active');
    // Find and activate the matching button
    const buttons = document.querySelectorAll('.tab-button');
    buttons.forEach(btn => {
      if (btn.textContent.toLowerCase().includes(tabName)) {
        btn.classList.add('active');
      }
    });
    // Load data for this tab
    if (tabName === 'estudiantes') loadEstudiantes();
    else if (tabName === 'socios') loadSocios();
    else if (tabName === 'libros') loadLibros();
    else if (tabName === 'alquileres') loadAlquileres();
    else if (tabName === 'empleados') loadEmpleados();
  }
}

function switchLibroSubTab(subTab) {
  const parent = event.target.parentElement;
  parent.querySelectorAll('.subtab-button').forEach(btn => btn.classList.remove('active'));
  event.target.classList.add('active');
  
  if (subTab === 'todos') loadLibros();
  else if (subTab === 'disponibles') loadLibrosDisponibles();
  else if (subTab === 'alquilados') loadLibrosAlquilados();
}

function switchAlquilerSubTab(subTab) {
  const parent = event.target.parentElement;
  parent.querySelectorAll('.subtab-button').forEach(btn => btn.classList.remove('active'));
  event.target.classList.add('active');
  
  if (subTab === 'activos') loadAlquileresActivos();
  else if (subTab === 'historial') loadAlquileresHistorial();
}

// ========== ESTUDIANTES ==========
async function loadEstudiantes() {
  try {
    const res = await fetch(API.students);
    const data = await res.json();
    dataCache.estudiantes = data;
    renderEstudiantesTable(data);
  } catch (err) {
    console.error('Error:', err);
    showMsg('estudiantes', 'Error al cargar estudiantes', true);
  }
}

function renderEstudiantesTable(estudiantes) {
  const tbody = document.getElementById('estudiantesTableBody');
  const table = document.getElementById('estudiantesTable');
  const emptyState = document.getElementById('estudiantesEmptyState');
  
  tbody.innerHTML = '';
  
  if (!estudiantes || estudiantes.length === 0) {
    table.style.display = 'none';
    emptyState.style.display = 'block';
    return;
  }
  
  table.style.display = 'table';
  emptyState.style.display = 'none';
  
  estudiantes.forEach(e => {
    const row = document.createElement('tr');
    row.innerHTML = `
      <td>${escapeHtml(e.id)}</td>
      <td>${escapeHtml(e.name)}</td>
      <td>${escapeHtml(e.surname)}</td>
      <td>${e.age}</td>
      <td>
        <div class="actions">
          <button class="btn btn-edit" onclick="editEstudiante('${escapeHtml(e.id)}')">✏️ Editar</button>
          <button class="btn btn-danger" onclick="deleteEstudiante('${escapeHtml(e.id)}')">🗑️ Eliminar</button>
        </div>
      </td>
    `;
    tbody.appendChild(row);
  });
}

function editEstudiante(id) {
  const estudiante = dataCache.estudiantes.find(e => e.id === id);
  if (estudiante) {
    document.getElementById('idEstudiante').value = estudiante.id;
    document.getElementById('nameEstudiante').value = estudiante.name;
    document.getElementById('surnameEstudiante').value = estudiante.surname;
    document.getElementById('ageEstudiante').value = estudiante.age;
    document.getElementById('idGroupEditEstudiante').style.display = 'grid';
    document.getElementById('submitEstudiante').textContent = '💾 Actualizar';
    document.getElementById('estudianteForm').dataset.editing = 'true';
    document.getElementById('estudianteForm').scrollIntoView({ behavior: 'smooth' });
  }
}

async function deleteEstudiante(id) {
  if (!confirm(`¿Eliminar estudiante ${id}?`)) return;
  try {
    const res = await fetch(`${API.students}/${encodeURIComponent(id)}`, { method: 'DELETE' });
    if (res.ok) {
      showMsg('estudiantes', '✅ Estudiante eliminado', false);
      loadEstudiantes();
    }
  } catch (err) {
    showMsg('estudiantes', '❌ Error al eliminar', true);
  }
}

function clearEstudianteForm() {
  document.getElementById('estudianteForm').reset();
  document.getElementById('estudianteForm').dataset.editing = 'false';
  document.getElementById('idGroupEditEstudiante').style.display = 'none';
  document.getElementById('submitEstudiante').textContent = '💾 Guardar Estudiante';
  document.getElementById('nameEstudiante').focus();
}

document.getElementById('estudianteForm')?.addEventListener('submit', async (e) => {
  e.preventDefault();
  let id = document.getElementById('idEstudiante').value.trim();
  const name = document.getElementById('nameEstudiante').value.trim();
  const surname = document.getElementById('surnameEstudiante').value.trim();
  const age = parseInt(document.getElementById('ageEstudiante').value);
  
  if (!name || !surname || isNaN(age) || age < 1 || age > 120) {
    showMsg('estudiantes', '❌ Datos inválidos', true);
    return;
  }
  
  const isEditing = document.getElementById('estudianteForm').dataset.editing === 'true';
  if (!isEditing) id = generateUUID();
  
  try {
    const payload = { id, name, surname, age };
    const method = isEditing ? 'PUT' : 'POST';
    const url = isEditing ? `${API.students}/${encodeURIComponent(id)}` : API.students;
    
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    
    if (res.ok || res.status === 201) {
      showMsg('estudiantes', '✅ Estudiante guardado', false);
      clearEstudianteForm();
      loadEstudiantes();
    }
  } catch (err) {
    showMsg('estudiantes', '❌ Error', true);
  }
});

// ========== SOCIOS ==========
async function loadSocios() {
  try {
    const res = await fetch(API.socios);
    const data = await res.json();
    dataCache.socios = data;
    renderSociosTable(data);
  } catch (err) {
    console.error('Error:', err);
    showMsg('socios', 'Error al cargar socios', true);
  }
}

function renderSociosTable(socios) {
  const tbody = document.getElementById('sociosTableBody');
  const table = document.getElementById('sociosTable');
  const emptyState = document.getElementById('sociosEmptyState');
  
  tbody.innerHTML = '';
  
  if (!socios || socios.length === 0) {
    table.style.display = 'none';
    emptyState.style.display = 'block';
    return;
  }
  
  table.style.display = 'table';
  emptyState.style.display = 'none';
  
  socios.forEach(s => {
    const row = document.createElement('tr');
    row.innerHTML = `
      <td>${escapeHtml(s.id)}</td>
      <td>${escapeHtml(s.nombre)}</td>
      <td>${escapeHtml(s.email)}</td>
      <td>${escapeHtml(s.telefono)}</td>
      <td>
        <div class="actions">
          <button class="btn btn-edit" onclick="editSocio('${escapeHtml(s.id)}')">✏️ Editar</button>
          <button class="btn btn-danger" onclick="deleteSocio('${escapeHtml(s.id)}')">🗑️ Eliminar</button>
        </div>
      </td>
    `;
    tbody.appendChild(row);
  });
}

function editSocio(id) {
  const socio = dataCache.socios.find(s => s.id === id);
  if (socio) {
    document.getElementById('idSocio').value = socio.id;
    document.getElementById('nameSocio').value = socio.nombre;
    document.getElementById('surnameSocio').value = socio.apellido;
    document.getElementById('emailSocio').value = socio.email;
    document.getElementById('telefonoSocio').value = socio.telefono;
    document.getElementById('idGroupEditSocio').style.display = 'grid';
    document.getElementById('submitSocio').textContent = '💾 Actualizar';
    document.getElementById('socioForm').dataset.editing = 'true';
    document.getElementById('socioForm').scrollIntoView({ behavior: 'smooth' });
  }
}

async function deleteSocio(id) {
  if (!confirm(`¿Eliminar socio ${id}?`)) return;
  try {
    const res = await fetch(`${API.socios}/${encodeURIComponent(id)}`, { method: 'DELETE' });
    if (res.ok) {
      showMsg('socios', '✅ Socio eliminado', false);
      loadSocios();
    }
  } catch (err) {
    showMsg('socios', '❌ Error al eliminar', true);
  }
}

function clearSocioForm() {
  document.getElementById('socioForm').reset();
  document.getElementById('socioForm').dataset.editing = 'false';
  document.getElementById('idGroupEditSocio').style.display = 'none';
  document.getElementById('submitSocio').textContent = '💾 Guardar Socio';
}

document.getElementById('socioForm')?.addEventListener('submit', async (e) => {
  e.preventDefault();
  let id = document.getElementById('idSocio').value.trim();
  const nombre = document.getElementById('nameSocio').value.trim();
  const apellido = document.getElementById('surnameSocio').value.trim();
  const email = document.getElementById('emailSocio').value.trim();
  const telefono = document.getElementById('telefonoSocio').value.trim();
  
  if (!nombre || !apellido || !email) {
    showMsg('socios', '❌ Datos inválidos', true);
    return;
  }
  
  const isEditing = document.getElementById('socioForm').dataset.editing === 'true';
  if (!isEditing) id = generateUUID();
  
  try {
    const payload = { id, nombre, apellido, email, telefono };
    const method = isEditing ? 'PUT' : 'POST';
    const url = isEditing ? `${API.socios}/${encodeURIComponent(id)}` : API.socios;
    
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    
    if (res.ok || res.status === 201) {
      showMsg('socios', '✅ Socio guardado', false);
      clearSocioForm();
      loadSocios();
    }
  } catch (err) {
    showMsg('socios', '❌ Error', true);
  }
});

// ========== LIBROS ==========
let librosActuales = [];

async function loadLibros() {
  try {
    const res = await fetch(API.libros);
    const data = await res.json();
    dataCache.libros = data;
    librosActuales = data;
    renderLibrosTable(data);
  } catch (err) {
    console.error('Error:', err);
    showMsg('libros', 'Error al cargar libros', true);
  }
}

async function loadLibrosDisponibles() {
  try {
    const res = await fetch(API.librosDisponibles);
    const data = await res.json();
    librosActuales = data;
    renderLibrosTable(data);
  } catch (err) {
    showMsg('libros', 'Error al cargar libros disponibles', true);
  }
}

async function loadLibrosAlquilados() {
  try {
    const res = await fetch(API.librosAlquilados);
    const data = await res.json();
    librosActuales = data;
    renderLibrosTable(data);
  } catch (err) {
    showMsg('libros', 'Error al cargar libros alquilados', true);
  }
}

function renderLibrosTable(libros) {
  const tbody = document.getElementById('librosTableBody');
  tbody.innerHTML = '';
  
  if (!libros || libros.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="text-center">No hay libros</td></tr>';
    return;
  }
  
  libros.forEach(l => {
    const row = document.createElement('tr');
    const estado = l.disponible ? '📚 Disponible' : '🔴 Alquilado';
    row.innerHTML = `
      <td>${escapeHtml(l.id)}</td>
      <td>${escapeHtml(l.titulo)}</td>
      <td>${escapeHtml(l.autor)}</td>
      <td>${escapeHtml(l.isbn)}</td>
      <td>${estado}</td>
      <td>
        <div class="actions">
          <button class="btn btn-edit" onclick="editLibro('${escapeHtml(l.id)}')">✏️ Editar</button>
          <button class="btn btn-danger" onclick="deleteLibro('${escapeHtml(l.id)}')">🗑️ Eliminar</button>
        </div>
      </td>
    `;
    tbody.appendChild(row);
  });
}

function editLibro(id) {
  const libro = dataCache.libros.find(l => l.id === id);
  if (libro) {
    document.getElementById('idLibro').value = libro.id;
    document.getElementById('tituloLibro').value = libro.titulo;
    document.getElementById('autorLibro').value = libro.autor;
    document.getElementById('isbnLibro').value = libro.isbn;
    document.getElementById('idGroupEditLibro').style.display = 'grid';
    document.getElementById('submitLibro').textContent = '💾 Actualizar';
    document.getElementById('libroForm').dataset.editing = 'true';
    document.getElementById('libroForm').scrollIntoView({ behavior: 'smooth' });
  }
}

async function deleteLibro(id) {
  if (!confirm(`¿Eliminar libro ${id}?`)) return;
  try {
    const res = await fetch(`${API.libros}/${encodeURIComponent(id)}`, { method: 'DELETE' });
    if (res.ok) {
      showMsg('libros', '✅ Libro eliminado', false);
      loadLibros();
    }
  } catch (err) {
    showMsg('libros', '❌ Error al eliminar', true);
  }
}

function clearLibroForm() {
  document.getElementById('libroForm').reset();
  document.getElementById('libroForm').dataset.editing = 'false';
  document.getElementById('idGroupEditLibro').style.display = 'none';
  document.getElementById('submitLibro').textContent = '💾 Guardar Libro';
}

document.getElementById('libroForm')?.addEventListener('submit', async (e) => {
  e.preventDefault();
  let id = document.getElementById('idLibro').value.trim();
  const titulo = document.getElementById('tituloLibro').value.trim();
  const autor = document.getElementById('autorLibro').value.trim();
  const isbn = document.getElementById('isbnLibro').value.trim();
  
  if (!titulo || !autor) {
    showMsg('libros', '❌ Datos inválidos', true);
    return;
  }
  
  const isEditing = document.getElementById('libroForm').dataset.editing === 'true';
  if (!isEditing) id = generateUUID();
  
  try {
    const payload = { id, titulo, autor, isbn, disponible: true };
    const method = isEditing ? 'PUT' : 'POST';
    const url = isEditing ? `${API.libros}/${encodeURIComponent(id)}` : API.libros;
    
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    
    if (res.ok || res.status === 201) {
      showMsg('libros', '✅ Libro guardado', false);
      clearLibroForm();
      loadLibros();
    }
  } catch (err) {
    showMsg('libros', '❌ Error', true);
  }
});

// ========== ALQUILERES ==========
async function loadAlquileres() {
  await loadSociosInDropdown();
  await loadLibrosDisponiblesInDropdown();
  loadAlquileresActivos();
}

async function loadSociosInDropdown() {
  try {
    const res = await fetch(API.socios);
    const socios = await res.json();
    const select = document.getElementById('socioAlquiler');
    select.innerHTML = '<option value="">-- Selecciona un socio --</option>';
    socios.forEach(socio => {
      const option = document.createElement('option');
      option.value = socio.id;
      option.textContent = `${socio.nombre} ${socio.apellido}`;
      select.appendChild(option);
    });
  } catch (err) {
    console.error('Error al cargar socios en dropdown:', err);
  }
}

async function loadLibrosDisponiblesInDropdown() {
  try {
    const res = await fetch(API.librosDisponibles);
    const libros = await res.json();
    const select = document.getElementById('libroAlquiler');
    select.innerHTML = '<option value="">-- Selecciona un libro disponible --</option>';
    libros.forEach(libro => {
      const option = document.createElement('option');
      option.value = libro.id;
      option.textContent = libro.titulo;
      select.appendChild(option);
    });
  } catch (err) {
    console.error('Error al cargar libros en dropdown:', err);
  }
}

async function loadAlquileresActivos() {
  try {
    const res = await fetch(API.alquileresActivos);
    const data = await res.json();
    renderAlquileresTable(data);
  } catch (err) {
    showMsg('alquileres', 'Error al cargar alquileres', true);
  }
}

async function loadAlquileresHistorial() {
  try {
    const res = await fetch(API.alquileresHistorial);
    const data = await res.json();
    renderAlquileresTable(data);
  } catch (err) {
    showMsg('alquileres', 'Error al cargar historial', true);
  }
}

function renderAlquileresTable(alquileres) {
  const tbody = document.getElementById('alquileresTableBody');
  tbody.innerHTML = '';
  
  if (!alquileres || alquileres.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="text-center">No hay alquileres</td></tr>';
    return;
  }
  
  alquileres.forEach(a => {
    const row = document.createElement('tr');
    row.innerHTML = `
      <td>${escapeHtml(a.id)}</td>
      <td>${escapeHtml(a.socioId)}</td>
      <td>${escapeHtml(a.libroId)}</td>
      <td>${a.fechaAlquiler}</td>
      <td>${a.estado}</td>
      <td>
        <div class="actions">
          ${a.estado === 'activo' ? `<button class="btn btn-success" onclick="devolverLibro('${escapeHtml(a.id)}')">✅ Devolver</button>` : ''}
          <button class="btn btn-danger" onclick="deleteAlquiler('${escapeHtml(a.id)}')">🗑️ Eliminar</button>
        </div>
      </td>
    `;
    tbody.appendChild(row);
  });
}

async function devolverLibro(id) {
  if (!confirm('¿Registrar devolución?')) return;
  try {
    const res = await fetch(`${API.alquileres}/${encodeURIComponent(id)}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ accion: 'devolver' })
    });
    
    if (res.ok) {
      showMsg('alquileres', '✅ Devolución registrada', false);
      loadAlquileres();
    }
  } catch (err) {
    showMsg('alquileres', '❌ Error', true);
  }
}

async function deleteAlquiler(id) {
  if (!confirm('¿Eliminar alquiler?')) return;
  try {
    const res = await fetch(`${API.alquileres}/${encodeURIComponent(id)}`, { method: 'DELETE' });
    if (res.ok) {
      showMsg('alquileres', '✅ Alquiler eliminado', false);
      loadAlquileres();
    }
  } catch (err) {
    showMsg('alquileres', '❌ Error', true);
  }
}

function clearAlquilerForm() {
  document.getElementById('alquilerForm').reset();
}

document.getElementById('alquilerForm')?.addEventListener('submit', async (e) => {
  e.preventDefault();
  const socioId = document.getElementById('socioAlquiler').value;
  const libroId = document.getElementById('libroAlquiler').value;
  
  if (!socioId || !libroId) {
    showMsg('alquileres', '❌ Selecciona socio y libro', true);
    return;
  }
  
  try {
    const payload = {
      id: generateUUID(),
      socioId,
      libroId,
      fechaAlquiler: new Date().toISOString().split('T')[0],
      fechaDevolucion: null,
      estado: 'activo'
    };
    
    const res = await fetch(API.alquileres, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    
    if (res.ok || res.status === 201) {
      showMsg('alquileres', '✅ Alquiler registrado', false);
      clearAlquilerForm();
      loadAlquileres();
    }
  } catch (err) {
    showMsg('alquileres', '❌ Error', true);
  }
});

// ========== EMPLEADOS ==========
async function loadEmpleados() {
  try {
    const res = await fetch(API.empleados);
    const data = await res.json();
    dataCache.empleados = data;
    renderEmpleadosTable(data);
  } catch (err) {
    console.error('Error:', err);
    showMsg('empleados', 'Error al cargar empleados', true);
  }
}

function renderEmpleadosTable(empleados) {
  const tbody = document.getElementById('empleadosTableBody');
  const table = document.getElementById('empleadosTable');
  const emptyState = document.getElementById('empleadosEmptyState');
  
  tbody.innerHTML = '';
  
  if (!empleados || empleados.length === 0) {
    table.style.display = 'none';
    emptyState.style.display = 'block';
    return;
  }
  
  table.style.display = 'table';
  emptyState.style.display = 'none';
  
  empleados.forEach(e => {
    const row = document.createElement('tr');
    row.innerHTML = `
      <td>${escapeHtml(e.id)}</td>
      <td>${escapeHtml(e.nombre)}</td>
      <td>${escapeHtml(e.puesto)}</td>
      <td>${escapeHtml(e.email)}</td>
      <td>${e.salario}</td>
      <td>
        <div class="actions">
          <button class="btn btn-edit" onclick="editEmpleado('${escapeHtml(e.id)}')">✏️ Editar</button>
          <button class="btn btn-danger" onclick="deleteEmpleado('${escapeHtml(e.id)}')">🗑️ Eliminar</button>
        </div>
      </td>
    `;
    tbody.appendChild(row);
  });
}

function editEmpleado(id) {
  const empleado = dataCache.empleados.find(e => e.id === id);
  if (empleado) {
    document.getElementById('idEmpleado').value = empleado.id;
    document.getElementById('nameEmpleado').value = empleado.nombre;
    document.getElementById('surnameEmpleado').value = empleado.apellido;
    document.getElementById('emailEmpleado').value = empleado.email;
    document.getElementById('puestoEmpleado').value = empleado.puesto;
    document.getElementById('salarioEmpleado').value = empleado.salario;
    document.getElementById('idGroupEditEmpleado').style.display = 'grid';
    document.getElementById('submitEmpleado').textContent = '💾 Actualizar';
    document.getElementById('empleadoForm').dataset.editing = 'true';
    document.getElementById('empleadoForm').scrollIntoView({ behavior: 'smooth' });
  }
}

async function deleteEmpleado(id) {
  if (!confirm(`¿Eliminar empleado ${id}?`)) return;
  try {
    const res = await fetch(`${API.empleados}/${encodeURIComponent(id)}`, { method: 'DELETE' });
    if (res.ok) {
      showMsg('empleados', '✅ Empleado eliminado', false);
      loadEmpleados();
    }
  } catch (err) {
    showMsg('empleados', '❌ Error al eliminar', true);
  }
}

function clearEmpleadoForm() {
  document.getElementById('empleadoForm').reset();
  document.getElementById('empleadoForm').dataset.editing = 'false';
  document.getElementById('idGroupEditEmpleado').style.display = 'none';
  document.getElementById('submitEmpleado').textContent = '💾 Guardar Empleado';
}

document.getElementById('empleadoForm')?.addEventListener('submit', async (e) => {
  e.preventDefault();
  let id = document.getElementById('idEmpleado').value.trim();
  const nombre = document.getElementById('nameEmpleado').value.trim();
  const apellido = document.getElementById('surnameEmpleado').value.trim();
  const email = document.getElementById('emailEmpleado').value.trim();
  const puesto = document.getElementById('puestoEmpleado').value.trim();
  const salario = document.getElementById('salarioEmpleado').value.trim();
  
  if (!nombre || !apellido || !email || !puesto) {
    showMsg('empleados', '❌ Datos inválidos', true);
    return;
  }
  
  const isEditing = document.getElementById('empleadoForm').dataset.editing === 'true';
  if (!isEditing) id = generateUUID();
  
  try {
    const payload = { id, nombre, apellido, email, puesto, salario };
    const method = isEditing ? 'PUT' : 'POST';
    const url = isEditing ? `${API.empleados}/${encodeURIComponent(id)}` : API.empleados;
    
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    
    if (res.ok || res.status === 201) {
      showMsg('empleados', '✅ Empleado guardado', false);
      clearEmpleadoForm();
      loadEmpleados();
    }
  } catch (err) {
    showMsg('empleados', '❌ Error', true);
  }
});

// Initial load
window.addEventListener('load', () => {
  loadEstudiantes();
});

// ── React integration toggles ────────────────────────────────────────────────
function switchToReact() {
  const container = document.querySelector('.container');
  if (container) container.style.display = 'none';
  const root = document.getElementById('react-root');
  if (root) root.style.display = 'block';
  if (window.mountBiblioteca) {
    try { window.mountBiblioteca(); } catch (e) { console.error('mountBiblioteca error', e); }
  }
}

function switchFromReact() {
  const container = document.querySelector('.container');
  if (container) container.style.display = 'block';
  const root = document.getElementById('react-root');
  if (root) root.style.display = 'none';
}

window.switchToReact = switchToReact;
window.switchFromReact = switchFromReact;
