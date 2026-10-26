/**
 * Admin User Management Controller
 */

let userPage = 0;
const userPageSize = 10;
let userTotalPages = 1;
let departmentsList = [];

document.addEventListener("DOMContentLoaded", async () => {
  if (!auth.requireAuth(["ADMIN"])) return;
  auth.renderLayout("User Management");

  await loadDepartments();
  await loadUsers();

  document.getElementById("user-search-input")?.addEventListener("input", debounce(() => {
    userPage = 0;
    loadUsers();
  }, 350));
});

async function loadDepartments() {
  try {
    const res = await api.get("/departments");
    if (res.success && res.data) {
      departmentsList = res.data;
      const select = document.getElementById("user-department-select");
      if (select) {
        select.innerHTML = `<option value="">-- No Department --</option>` +
          departmentsList.map(d => `<option value="${d.id}">${escapeHtml(d.name)}</option>`).join("");
      }
    }
  } catch (err) {
    console.warn("Could not load departments", err);
  }
}

async function loadUsers() {
  const tbody = document.getElementById("users-tbody");
  if (!tbody) return;

  const search = document.getElementById("user-search-input")?.value?.trim() || "";
  let endpoint = `/users?page=${userPage}&size=${userPageSize}`;
  if (search) endpoint += `&search=${encodeURIComponent(search)}`;

  try {
    const res = await api.get(endpoint);
    if (res.success && res.data) {
      const pageData = res.data;
      userTotalPages = pageData.totalPages || 1;
      renderUserRows(pageData.content || []);
      renderUserPagination(pageData.number, userTotalPages, pageData.totalElements);
    }
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:#ef4444; padding:2rem;">Error: ${escapeHtml(err.message)}</td></tr>`;
  }
}

function renderUserRows(users) {
  const tbody = document.getElementById("users-tbody");
  if (!tbody) return;

  if (users.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:2rem; color:#64748b;">No users found.</td></tr>`;
    return;
  }

  tbody.innerHTML = users.map(u => `
    <tr>
      <td style="font-weight:600;">${escapeHtml(u.fullName)}</td>
      <td>${escapeHtml(u.email)}</td>
      <td>${escapeHtml(u.departmentName || '-')}</td>
      <td><span class="badge role-${(u.role || '').toLowerCase()}">${u.role}</span></td>
      <td>
        <span class="status-pill" style="background:${u.active ? 'var(--success-light)' : 'var(--danger-light)'}; color:${u.active ? 'var(--success)' : 'var(--danger)'};">
          <span class="dot" style="background:${u.active ? 'var(--success)' : 'var(--danger)'};"></span>
          <span>${u.active ? 'Active' : 'Disabled'}</span>
        </span>
      </td>
      <td>${formatDate(u.createdAt)}</td>
      <td>
        <div style="display:flex; gap:0.4rem;">
          <button class="btn btn-sm btn-outline" onclick="editUser(${u.id})">Edit</button>
          <button class="btn btn-sm ${u.active ? 'btn-warning' : 'btn-success'}" onclick="toggleStatus(${u.id})">
            ${u.active ? 'Deactivate' : 'Activate'}
          </button>
          <button class="btn btn-sm btn-danger" onclick="deleteUser(${u.id})">Delete</button>
        </div>
      </td>
    </tr>
  `).join("");
}

function renderUserPagination(current, total, totalElements) {
  const controls = document.getElementById("user-pagination-controls");
  const info = document.getElementById("user-pagination-info");
  if (!controls) return;

  if (info) {
    info.textContent = `Showing page ${current + 1} of ${Math.max(1, total)} (${totalElements} users)`;
  }

  let html = `
    <button class="page-btn" ${current === 0 ? 'disabled' : ''} onclick="changeUserPage(${current - 1})">Previous</button>
  `;

  for (let i = 0; i < total; i++) {
    if (i === 0 || i === total - 1 || (i >= current - 1 && i <= current + 1)) {
      html += `<button class="page-btn ${i === current ? 'active' : ''}" onclick="changeUserPage(${i})">${i + 1}</button>`;
    }
  }

  html += `
    <button class="page-btn" ${current >= total - 1 ? 'disabled' : ''} onclick="changeUserPage(${current + 1})">Next</button>
  `;

  controls.innerHTML = html;
}

function changeUserPage(page) {
  if (page < 0 || page >= userTotalPages) return;
  userPage = page;
  loadUsers();
}

function openAddUserModal() {
  document.getElementById("user-modal-title").textContent = "Add New User";
  document.getElementById("user-form").reset();
  document.getElementById("edit-user-id").value = "";
  document.getElementById("user-password-hint").textContent = "Must be at least 6 characters.";
  document.getElementById("user-password-input").required = true;
  document.getElementById("user-modal").classList.add("open");
}

async function editUser(id) {
  try {
    const res = await api.get(`/users/${id}`);
    if (res.success && res.data) {
      const u = res.data;
      document.getElementById("user-modal-title").textContent = "Edit User";
      document.getElementById("edit-user-id").value = u.id;
      document.getElementById("user-fname-input").value = u.firstName;
      document.getElementById("user-lname-input").value = u.lastName;
      document.getElementById("user-email-input").value = u.email;
      document.getElementById("user-phone-input").value = u.phone || "";
      document.getElementById("user-role-select").value = u.role;
      document.getElementById("user-department-select").value = u.departmentId || "";
      document.getElementById("user-password-input").value = "";
      document.getElementById("user-password-input").required = false;
      document.getElementById("user-password-hint").textContent = "Leave blank to keep existing password.";
      document.getElementById("user-modal").classList.add("open");
    }
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function saveUser(event) {
  event.preventDefault();
  const id = document.getElementById("edit-user-id").value;
  const firstName = document.getElementById("user-fname-input").value.trim();
  const lastName = document.getElementById("user-lname-input").value.trim();
  const email = document.getElementById("user-email-input").value.trim();
  const password = document.getElementById("user-password-input").value;
  const phone = document.getElementById("user-phone-input").value.trim();
  const role = document.getElementById("user-role-select").value;
  const departmentIdVal = document.getElementById("user-department-select").value;

  const payload = {
    firstName,
    lastName,
    email,
    phone,
    role,
    departmentId: departmentIdVal ? Number(departmentIdVal) : null
  };

  if (password) {
    payload.password = password;
  }

  try {
    let res;
    if (id) {
      res = await api.put(`/users/${id}`, payload);
    } else {
      res = await api.post("/users", payload);
    }

    if (res.success) {
      showToast(`User ${id ? 'updated' : 'created'} successfully`, "success");
      closeModal("user-modal");
      await loadUsers();
    }
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function toggleStatus(id) {
  try {
    const res = await api.patch(`/users/${id}/toggle-status`);
    if (res.success) {
      showToast("User status updated", "success");
      await loadUsers();
    }
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function deleteUser(id) {
  if (!confirm("Are you sure you want to permanently delete this user?")) return;
  try {
    const res = await api.delete(`/users/${id}`);
    if (res.success) {
      showToast("User deleted successfully", "success");
      await loadUsers();
    }
  } catch (err) {
    showToast(err.message, "error");
  }
}

function closeModal(id) {
  document.getElementById(id)?.classList.remove("open");
}

function debounce(func, wait) {
  let timeout;
  return function (...args) {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(this, args), wait);
  };
}
