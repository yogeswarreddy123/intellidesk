/**
 * Tickets List Controller
 */

let currentPage = 0;
const pageSize = 10;
let totalPages = 1;

document.addEventListener("DOMContentLoaded", async () => {
  if (!auth.requireAuth()) return;
  auth.renderLayout("Tickets");

  await loadCategoriesDropdown();
  await loadTickets();

  // Attach search and filter event listeners
  document.getElementById("search-input")?.addEventListener("input", debounce(() => {
    currentPage = 0;
    loadTickets();
  }, 400));

  document.getElementById("status-filter")?.addEventListener("change", () => {
    currentPage = 0;
    loadTickets();
  });

  document.getElementById("priority-filter")?.addEventListener("change", () => {
    currentPage = 0;
    loadTickets();
  });

  document.getElementById("category-filter")?.addEventListener("change", () => {
    currentPage = 0;
    loadTickets();
  });
});

async function loadCategoriesDropdown() {
  const select = document.getElementById("category-filter");
  if (!select) return;

  try {
    const res = await api.get("/categories");
    if (res.success && res.data) {
      res.data.forEach(cat => {
        const opt = document.createElement("option");
        opt.value = cat.id;
        opt.textContent = cat.name;
        select.appendChild(opt);
      });
    }
  } catch (err) {
    console.warn("Could not load categories dropdown", err);
  }
}

async function loadTickets() {
  const tbody = document.getElementById("tickets-tbody");
  if (!tbody) return;

  tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding:2rem;">Loading tickets...</td></tr>`;

  const search = document.getElementById("search-input")?.value?.trim() || "";
  const status = document.getElementById("status-filter")?.value || "";
  const priority = document.getElementById("priority-filter")?.value || "";
  const categoryId = document.getElementById("category-filter")?.value || "";

  const urlParams = new URLSearchParams(window.location.search);
  const isMyView = urlParams.get("view") === "my" || auth.getUserRole() === "EMPLOYEE";

  let endpoint = isMyView ? `/tickets/my?page=${currentPage}&size=${pageSize}` : `/tickets?page=${currentPage}&size=${pageSize}`;

  if (!isMyView) {
    if (search) endpoint += `&search=${encodeURIComponent(search)}`;
    if (status) endpoint += `&status=${status}`;
    if (priority) endpoint += `&priority=${priority}`;
    if (categoryId) endpoint += `&categoryId=${categoryId}`;
  } else if (search) {
    // When in my tickets mode, still allow query if backend supports it or filter locally
    endpoint += `&search=${encodeURIComponent(search)}`;
  }

  try {
    const res = await api.get(endpoint);
    if (res.success && res.data) {
      const pageData = res.data;
      totalPages = pageData.totalPages || 1;
      renderTicketRows(pageData.content || []);
      renderPagination(pageData.number, totalPages, pageData.totalElements);
    }
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:#ef4444; padding:2rem;">Error: ${escapeHtml(err.message)}</td></tr>`;
  }
}

function renderTicketRows(tickets) {
  const tbody = document.getElementById("tickets-tbody");
  if (!tbody) return;

  if (tickets.length === 0) {
    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding:2.5rem; color:#64748b;">No tickets match your filters.</td></tr>`;
    return;
  }

  const role = auth.getUserRole();

  tbody.innerHTML = tickets.map(t => `
    <tr>
      <td><a href="ticket-details.html?id=${t.id}" style="font-weight:700; color:var(--primary); text-decoration:none;">${t.ticketNumber}</a></td>
      <td>
        <a href="ticket-details.html?id=${t.id}" style="font-weight:600; color:var(--text-main); text-decoration:none;">
          ${escapeHtml(t.title)}
        </a>
      </td>
      <td>${escapeHtml(t.categoryName || '-')}</td>
      <td><span class="badge priority-${(t.priority || '').toLowerCase()}">${t.priority}</span></td>
      <td><span class="badge status-${(t.status || '').toLowerCase()}">${t.status}</span></td>
      <td>${escapeHtml(t.createdByName || '-')}</td>
      <td>${escapeHtml(t.assignedToName || 'Unassigned')}</td>
      <td>
        <a href="ticket-details.html?id=${t.id}" class="btn btn-sm btn-outline">Details</a>
      </td>
    </tr>
  `).join("");
}

function renderPagination(current, total, totalElements) {
  const infoEl = document.getElementById("pagination-info");
  const controlsEl = document.getElementById("pagination-controls");
  if (!controlsEl) return;

  if (infoEl) {
    infoEl.textContent = `Showing page ${current + 1} of ${Math.max(1, total)} (${totalElements} total tickets)`;
  }

  let html = `
    <button class="page-btn" ${current === 0 ? 'disabled' : ''} onclick="changePage(${current - 1})">Previous</button>
  `;

  for (let i = 0; i < total; i++) {
    if (i === 0 || i === total - 1 || (i >= current - 1 && i <= current + 1)) {
      html += `<button class="page-btn ${i === current ? 'active' : ''}" onclick="changePage(${i})">${i + 1}</button>`;
    } else if (i === current - 2 || i === current + 2) {
      html += `<span style="padding:0.4rem 0.6rem; color:#94a3b8;">...</span>`;
    }
  }

  html += `
    <button class="page-btn" ${current >= total - 1 ? 'disabled' : ''} onclick="changePage(${current + 1})">Next</button>
  `;

  controlsEl.innerHTML = html;
}

function changePage(page) {
  if (page < 0 || page >= totalPages) return;
  currentPage = page;
  loadTickets();
}

function debounce(func, wait) {
  let timeout;
  return function (...args) {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(this, args), wait);
  };
}
