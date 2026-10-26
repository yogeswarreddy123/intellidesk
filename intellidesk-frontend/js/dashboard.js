/**
 * Dashboard Controller
 */

document.addEventListener("DOMContentLoaded", async () => {
  if (!auth.requireAuth()) return;
  auth.renderLayout("Dashboard");

  await loadDashboard();
});

async function loadDashboard() {
  const user = auth.getUser();
  const role = user ? user.role : "EMPLOYEE";

  try {
    const res = await api.get("/dashboard/summary");
    if (res.success && res.data) {
      const summary = res.data;
      renderKPIs(role, summary);
      renderRecentTickets(summary.recentTickets || []);
      if (role === "ADMIN") {
        renderRecentActivities(summary.recentActivities || []);
      }
    }
  } catch (err) {
    showToast("Failed to load dashboard data: " + err.message, "error");
  }
}

function renderKPIs(role, summary) {
  const container = document.getElementById("kpi-container");
  if (!container) return;

  let html = "";

  if (role === "EMPLOYEE") {
    html = `
      <div class="stat-card">
        <div class="stat-icon primary">📁</div>
        <div class="stat-details">
          <div class="stat-label">My Tickets</div>
          <div class="stat-value">${summary.totalMyTickets || 0}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon info">⏳</div>
        <div class="stat-details">
          <div class="stat-label">Open Tickets</div>
          <div class="stat-value">${summary.myOpenTickets || 0}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon warning">⚡</div>
        <div class="stat-details">
          <div class="stat-label">In Progress</div>
          <div class="stat-value">${summary.myInProgressTickets || 0}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon success">✓</div>
        <div class="stat-details">
          <div class="stat-label">Resolved</div>
          <div class="stat-value">${summary.myResolvedTickets || 0}</div>
        </div>
      </div>
    `;
  } else if (role === "IT_AGENT") {
    html = `
      <div class="stat-card">
        <div class="stat-icon primary">📌</div>
        <div class="stat-details">
          <div class="stat-label">Assigned To Me</div>
          <div class="stat-value">${summary.assignedTickets || 0}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon info">📥</div>
        <div class="stat-details">
          <div class="stat-label">All Open</div>
          <div class="stat-value">${summary.openTickets || 0}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon warning">⚡</div>
        <div class="stat-details">
          <div class="stat-label">In Progress</div>
          <div class="stat-value">${summary.inProgressTickets || 0}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon danger">🚨</div>
        <div class="stat-details">
          <div class="stat-label">Critical Tickets</div>
          <div class="stat-value">${summary.criticalTickets || 0}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon success">✓</div>
        <div class="stat-details">
          <div class="stat-label">Total Resolved</div>
          <div class="stat-value">${summary.resolvedTickets || 0}</div>
        </div>
      </div>
    `;
  } else {
    // ADMIN
    html = `
      <div class="stat-card">
        <div class="stat-icon primary">👥</div>
        <div class="stat-details">
          <div class="stat-label">Total Users</div>
          <div class="stat-value">${summary.totalUsers || 0}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon info">🎫</div>
        <div class="stat-details">
          <div class="stat-label">Total Tickets</div>
          <div class="stat-value">${summary.totalTickets || 0}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon warning">📥</div>
        <div class="stat-details">
          <div class="stat-label">Open Tickets</div>
          <div class="stat-value">${summary.openTickets || 0}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon danger">🚨</div>
        <div class="stat-details">
          <div class="stat-label">Critical Tickets</div>
          <div class="stat-value">${summary.criticalTickets || 0}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon success">✓</div>
        <div class="stat-details">
          <div class="stat-label">Resolved Tickets</div>
          <div class="stat-value">${summary.resolvedTickets || 0}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon primary">🛡️</div>
        <div class="stat-details">
          <div class="stat-label">Active Agents</div>
          <div class="stat-value">${summary.activeAgents || 0}</div>
        </div>
      </div>
    `;
  }

  container.innerHTML = html;
}

function renderRecentTickets(tickets) {
  const tbody = document.getElementById("recent-tickets-tbody");
  if (!tbody) return;

  if (tickets.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding: 2rem; color: #64748b;">No recent tickets found.</td></tr>`;
    return;
  }

  tbody.innerHTML = tickets.map(t => `
    <tr>
      <td><a href="ticket-details.html?id=${t.id}" style="font-weight:700; color:var(--primary); text-decoration:none;">${t.ticketNumber}</a></td>
      <td style="max-width: 250px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${escapeHtml(t.title)}</td>
      <td>${escapeHtml(t.categoryName || '-')}</td>
      <td><span class="badge priority-${(t.priority || '').toLowerCase()}">${t.priority}</span></td>
      <td><span class="badge status-${(t.status || '').toLowerCase()}">${t.status}</span></td>
      <td>${escapeHtml(t.createdByName || '-')}</td>
      <td><a href="ticket-details.html?id=${t.id}" class="btn btn-sm btn-outline">View</a></td>
    </tr>
  `).join("");
}

function renderRecentActivities(activities) {
  const card = document.getElementById("activity-card");
  const list = document.getElementById("recent-activities-list");
  if (!card || !list) return;

  card.style.display = "block";
  if (activities.length === 0) {
    list.innerHTML = `<li style="color:#64748b; padding:1rem;">No recent activities logged.</li>`;
    return;
  }

  list.innerHTML = activities.slice(0, 5).map(act => `
    <li style="display:flex; justify-content:space-between; align-items:center; padding:0.75rem 0; border-bottom:1px solid #f1f5f9;">
      <div>
        <span style="font-weight:600; color:var(--text-main);">${escapeHtml(act.action)}</span>
        <span style="color:var(--text-muted); font-size:0.85rem;"> — ${escapeHtml(act.description || '')}</span>
      </div>
      <span style="font-size:0.75rem; color:var(--text-light);">${formatDate(act.timestamp)}</span>
    </li>
  `).join("");
}
