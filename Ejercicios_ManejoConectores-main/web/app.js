const apiBase = '/api/students';

async function fetchStudents() {
  const res = await fetch(apiBase);
  return res.json();
}

function renderList(students) {
  const list = document.getElementById('list');
  list.innerHTML = '';
  if (!students || students.length === 0) { list.textContent = 'No hay estudiantes.'; return; }
  const ul = document.createElement('ul');
  students.forEach(s => {
    const li = document.createElement('li');
    li.textContent = `${s.id} - ${s.name} ${s.surname} (${s.age}) `;
    const btnEdit = document.createElement('button'); btnEdit.textContent = 'Editar';
    btnEdit.onclick = () => fillForm(s);
    const btnDel = document.createElement('button'); btnDel.textContent = 'Borrar';
    btnDel.onclick = () => deleteStudent(s.id);
    li.appendChild(btnEdit); li.appendChild(btnDel);
    ul.appendChild(li);
  });
  list.appendChild(ul);
}

function fillForm(s) {
  document.getElementById('id').value = s.id;
  document.getElementById('name').value = s.name;
  document.getElementById('surname').value = s.surname;
  document.getElementById('age').value = s.age;
}

async function deleteStudent(id) {
  if (!confirm('¿Eliminar estudiante ' + id + '?')) return;
  const res = await fetch(`${apiBase}/${encodeURIComponent(id)}`, { method: 'DELETE' });
  if (res.ok) showMsg('Estudiante borrado'); else showMsg('Error borrando', true);
  await refresh();
}

document.getElementById('studentForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const id = document.getElementById('id').value.trim();
  const name = document.getElementById('name').value.trim();
  const surname = document.getElementById('surname').value.trim();
  const ageRaw = document.getElementById('age').value;
  if (!id || !name || !surname || !ageRaw) { showMsg('Rellena todos los campos', true); return; }
  const age = parseInt(ageRaw, 10);
  if (isNaN(age) || age < 0) { showMsg('Edad inválida', true); return; }
  try {
    const existing = await fetch(`${apiBase}/${encodeURIComponent(id)}`);
    const payload = { id, name, surname, age };
    let res;
    if (existing.status === 200) {
      res = await fetch(`${apiBase}/${encodeURIComponent(id)}`, { method: 'PUT', headers: {'Content-Type':'application/json'}, body: JSON.stringify(payload)});
      if (res.ok) showMsg('Estudiante actualizado'); else showMsg('Error actualizando', true);
    } else {
      res = await fetch(apiBase, { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(payload)});
      if (res.status === 201) showMsg('Estudiante creado'); else showMsg('Error creando', true);
    }
    e.target.reset();
    await refresh();
  } catch (err) {
    showMsg('Error de red', true);
  }
});

function showMsg(msg, isError) {
  const el = document.getElementById('msg');
  el.style.color = isError ? 'red' : 'green';
  el.textContent = msg;
  setTimeout(() => { el.textContent = ''; }, 4000);
}

async function refresh() {
  const students = await fetchStudents();
  renderList(students);
}

window.addEventListener('load', () => refresh());
