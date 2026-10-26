/**
 * Admin Reports & Analytics Controller
 */

document.addEventListener("DOMContentLoaded", async () => {
  if (!auth.requireAuth(["ADMIN"])) return;
  auth.renderLayout("Analytics & Reports");

  await loadReports();
});

async function loadReports() {
  try {
    const res = await api.get("/reports/summary");
    if (res.success && res.data) {
      const report = res.data;
      renderKPIs(report);
      renderBarChart("status-chart-container", report.ticketsByStatus || {}, ["#3b82f6", "#f59e0b", "#a855f7", "#10b981", "#64748b"]);
      renderBarChart("priority-chart-container", report.ticketsByPriority || {}, ["#0ea5e9", "#eab308", "#f97316", "#ef4444"]);
      renderBarChart("category-chart-container", report.ticketsByCategory || {}, ["#6366f1", "#8b5cf6", "#ec4899", "#14b8a6", "#f43f5e", "#06b6d4"]);
      
      // Load monthly
      await loadMonthlyReport();
    }
  } catch (err) {
    showToast("Failed to load reports: " + err.message, "error");
  }
}

async function loadMonthlyReport() {
  try {
    const res = await api.get("/reports/by-month");
    if (res.success && res.data) {
      const labels = res.data.labels || [];
      const values = res.data.data || [];
      const map = {};
      labels.forEach((l, idx) => { map[l] = values[idx]; });
      renderBarChart("month-chart-container", map, ["#2563eb"]);
    }
  } catch (err) {
    console.warn("Could not load monthly chart", err);
  }
}

function renderKPIs(rep) {
  document.getElementById("rep-total").textContent = rep.totalTickets || 0;
  document.getElementById("rep-open").textContent = rep.openTickets || 0;
  document.getElementById("rep-resolved").textContent = rep.resolvedTickets || 0;
  document.getElementById("rep-critical").textContent = rep.criticalTickets || 0;
  document.getElementById("rep-avg-time").textContent = `${rep.averageResolutionTimeHours || 0} hrs`;
}

function renderBarChart(containerId, dataMap, colors) {
  const container = document.getElementById(containerId);
  if (!container) return;

  const entries = Object.entries(dataMap);
  if (entries.length === 0) {
    container.innerHTML = `<div style="text-align:center; color:#94a3b8; padding:2rem;">No data available</div>`;
    return;
  }

  const maxVal = Math.max(...entries.map(e => e[1]), 1);

  container.innerHTML = `
    <div style="display:flex; flex-direction:column; gap:0.85rem; padding:0.5rem 0;">
      ${entries.map(([label, val], idx) => {
        const percent = Math.round((val / maxVal) * 100);
        const color = colors[idx % colors.length];
        return `
          <div>
            <div style="display:flex; justify-content:space-between; font-size:0.8rem; margin-bottom:0.25rem; font-weight:600;">
              <span>${escapeHtml(label)}</span>
              <span>${val}</span>
            </div>
            <div style="background:#f1f5f9; height:12px; border-radius:6px; overflow:hidden;">
              <div style="background:${color}; width:${percent}%; height:100%; border-radius:6px; transition:width 0.5s ease;"></div>
            </div>
          </div>
        `;
      }).join("")}
    </div>
  `;
}
