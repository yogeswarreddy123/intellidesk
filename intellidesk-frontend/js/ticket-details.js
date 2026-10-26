/**
 * Ticket Details & Comments Controller
 */

let currentTicketId = null;
let currentTicket = null;

document.addEventListener("DOMContentLoaded", async () => {
  if (!auth.requireAuth()) return;
  auth.renderLayout("Ticket Details");

  const urlParams = new URLSearchParams(window.location.search);
  currentTicketId = urlParams.get("id");

  if (!currentTicketId) {
    showToast("No ticket ID specified", "error");
    window.location.href = "tickets.html";
    return;
  }

  await loadTicket();
  await loadComments();

  // Handle comment submit
  document.getElementById("comment-form")?.addEventListener("submit", async (e) => {
    e.preventDefault();
    await submitComment();
  });
});

async function loadTicket() {
  try {
    const res = await api.get(`/tickets/${currentTicketId}`);
    if (res.success && res.data) {
      currentTicket = res.data;
      renderTicketDetails(currentTicket);
      setupAgentActions(currentTicket);
    }
  } catch (err) {
    showToast("Error loading ticket: " + err.message, "error");
  }
}

function renderTicketDetails(t) {
  document.getElementById("ticket-number-heading").textContent = t.ticketNumber;
  document.getElementById("ticket-title").textContent = t.title;
  document.getElementById("ticket-desc").textContent = t.description;

  // Badges
  const statusEl = document.getElementById("ticket-status-badge");
  statusEl.className = `badge status-${(t.status || '').toLowerCase()}`;
  statusEl.textContent = t.status;

  const priorityEl = document.getElementById("ticket-priority-badge");
  priorityEl.className = `badge priority-${(t.priority || '').toLowerCase()}`;
  priorityEl.textContent = t.priority;

  // Metadata
  document.getElementById("meta-category").textContent = t.categoryName || "-";
  document.getElementById("meta-department").textContent = t.departmentName || "-";
  document.getElementById("meta-created-by").textContent = `${t.createdByName || '-'} (${t.createdByEmail || ''})`;
  document.getElementById("meta-assigned-to").textContent = t.assignedToName ? `${t.assignedToName} (${t.assignedToEmail || ''})` : "Unassigned";
  document.getElementById("meta-created-at").textContent = formatDate(t.createdAt);
  document.getElementById("meta-resolved-at").textContent = formatDate(t.resolvedAt);
  document.getElementById("meta-closed-at").textContent = formatDate(t.closedAt);
}

async function setupAgentActions(t) {
  const role = auth.getUserRole();
  const actionsCard = document.getElementById("agent-actions-card");
  if (!actionsCard) return;

  if (role === "EMPLOYEE") {
    // Show close button if resolved
    if (t.status === "RESOLVED") {
      actionsCard.style.display = "block";
      actionsCard.innerHTML = `
        <h3 class="card-title" style="margin-bottom:0.75rem;">Resolution Confirmation</h3>
        <p style="font-size:0.85rem; color:var(--text-muted); margin-bottom:1rem;">
          Your IT Agent has resolved this issue. If everything is working properly, please confirm and close the ticket.
        </p>
        <button class="btn btn-success" onclick="closeTicket()">Confirm & Close Ticket</button>
      `;
    } else {
      actionsCard.style.display = "none";
    }
    return;
  }

  // IT_AGENT or ADMIN
  actionsCard.style.display = "block";

  // Load active agents for assignment dropdown
  let agentOptions = `<option value="">-- Select IT Agent --</option>`;
  try {
    const agentsRes = await api.get("/users/agents");
    if (agentsRes.success && agentsRes.data) {
      agentsRes.data.forEach(a => {
        const isSelected = t.assignedToId === a.id ? "selected" : "";
        agentOptions += `<option value="${a.id}" ${isSelected}>${escapeHtml(a.fullName)} (${escapeHtml(a.email)})</option>`;
      });
    }
  } catch (err) {
    console.warn("Could not load agents list", err);
  }

  actionsCard.innerHTML = `
    <h3 class="card-title" style="margin-bottom: 1rem;">Manage Ticket</h3>
    <div style="display: flex; flex-direction: column; gap: 1rem;">
      <div class="form-group" style="margin-bottom:0;">
        <label class="form-label">Assign Agent</label>
        <div style="display:flex; gap:0.5rem;">
          <select id="action-assignee-select" class="form-select">${agentOptions}</select>
          <button class="btn btn-primary btn-sm" onclick="assignTicket()">Assign</button>
        </div>
      </div>

      <div class="form-group" style="margin-bottom:0;">
        <label class="form-label">Update Status</label>
        <div style="display:flex; gap:0.5rem;">
          <select id="action-status-select" class="form-select">
            <option value="OPEN" ${t.status === 'OPEN' ? 'selected' : ''}>OPEN</option>
            <option value="IN_PROGRESS" ${t.status === 'IN_PROGRESS' ? 'selected' : ''}>IN_PROGRESS</option>
            <option value="PENDING" ${t.status === 'PENDING' ? 'selected' : ''}>PENDING</option>
            <option value="RESOLVED" ${t.status === 'RESOLVED' ? 'selected' : ''}>RESOLVED</option>
            <option value="CLOSED" ${t.status === 'CLOSED' ? 'selected' : ''}>CLOSED</option>
          </select>
          <button class="btn btn-primary btn-sm" onclick="updateTicketStatus()">Update</button>
        </div>
      </div>

      <div class="form-group" style="margin-bottom:0;">
        <label class="form-label">Update Priority</label>
        <div style="display:flex; gap:0.5rem;">
          <select id="action-priority-select" class="form-select">
            <option value="LOW" ${t.priority === 'LOW' ? 'selected' : ''}>LOW</option>
            <option value="MEDIUM" ${t.priority === 'MEDIUM' ? 'selected' : ''}>MEDIUM</option>
            <option value="HIGH" ${t.priority === 'HIGH' ? 'selected' : ''}>HIGH</option>
            <option value="CRITICAL" ${t.priority === 'CRITICAL' ? 'selected' : ''}>CRITICAL</option>
          </select>
          <button class="btn btn-primary btn-sm" onclick="updateTicketPriority()">Update</button>
        </div>
      </div>

      <div style="display: flex; gap: 0.5rem; margin-top: 0.5rem;">
        ${t.status !== 'RESOLVED' && t.status !== 'CLOSED' ? `
          <button class="btn btn-success" style="flex:1;" onclick="resolveTicket()">Mark Resolved</button>
        ` : ''}
        ${role === 'ADMIN' ? `
          <button class="btn btn-danger btn-sm" onclick="deleteTicket()">Delete Ticket</button>
        ` : ''}
      </div>
    </div>
  `;
}

async function assignTicket() {
  const agentId = document.getElementById("action-assignee-select").value;
  if (!agentId) {
    showToast("Please select an agent to assign", "warning");
    return;
  }

  try {
    const res = await api.put(`/tickets/${currentTicketId}/assign`, { agentId: Number(agentId) });
    if (res.success) {
      showToast("Ticket successfully assigned", "success");
      await loadTicket();
      await loadComments();
    }
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function updateTicketStatus() {
  const status = document.getElementById("action-status-select").value;
  try {
    const res = await api.put(`/tickets/${currentTicketId}/status`, { status });
    if (res.success) {
      showToast("Ticket status updated to " + status, "success");
      await loadTicket();
    }
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function updateTicketPriority() {
  const priority = document.getElementById("action-priority-select").value;
  try {
    const res = await api.put(`/tickets/${currentTicketId}/priority`, { priority });
    if (res.success) {
      showToast("Ticket priority updated to " + priority, "success");
      await loadTicket();
    }
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function resolveTicket() {
  if (!confirm("Are you sure you want to mark this ticket as RESOLVED?")) return;
  try {
    const res = await api.put(`/tickets/${currentTicketId}/resolve`);
    if (res.success) {
      showToast("Ticket resolved successfully", "success");
      await loadTicket();
    }
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function closeTicket() {
  if (!confirm("Confirm and close this ticket?")) return;
  try {
    const res = await api.put(`/tickets/${currentTicketId}/close`);
    if (res.success) {
      showToast("Ticket marked as CLOSED", "success");
      await loadTicket();
    }
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function deleteTicket() {
  if (!confirm("Are you sure you want to permanently delete this ticket?")) return;
  try {
    const res = await api.delete(`/tickets/${currentTicketId}`);
    if (res.success) {
      showToast("Ticket deleted successfully", "success");
      window.location.href = "tickets.html";
    }
  } catch (err) {
    showToast(err.message, "error");
  }
}

/* Comments */
async function loadComments() {
  const container = document.getElementById("comments-list");
  if (!container) return;

  try {
    const res = await api.get(`/tickets/${currentTicketId}/comments`);
    if (res.success && res.data) {
      const comments = res.data;
      if (comments.length === 0) {
        container.innerHTML = `<div style="text-align:center; color:#64748b; padding:1.5rem;">No comments yet. Start the conversation below.</div>`;
        return;
      }

      container.innerHTML = comments.map(c => `
        <div class="comment-item">
          <div class="comment-meta">
            <div>
              <span class="comment-author">${escapeHtml(c.userName)}</span>
              <span class="badge role-${(c.userRole || '').toLowerCase()}" style="margin-left:0.4rem;">${c.userRole}</span>
            </div>
            <span style="color:#64748b;">${formatDate(c.createdAt)}</span>
          </div>
          <div class="comment-text">${escapeHtml(c.message)}</div>
        </div>
      `).join("");
    }
  } catch (err) {
    container.innerHTML = `<div style="color:#ef4444; padding:1rem;">Failed to load comments: ${escapeHtml(err.message)}</div>`;
  }
}

async function submitComment() {
  const input = document.getElementById("comment-message-input");
  const msg = input.value.trim();
  if (!msg) return;

  try {
    const res = await api.post(`/tickets/${currentTicketId}/comments`, { message: msg });
    if (res.success) {
      input.value = "";
      showToast("Comment posted", "success");
      await loadComments();
    }
  } catch (err) {
    showToast(err.message, "error");
  }
}
