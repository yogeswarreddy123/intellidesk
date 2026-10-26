/**
 * IntelliDesk Authentication & Layout Controller
 */

const auth = {
  getToken() {
    return localStorage.getItem("intellidesk_token");
  },

  getUser() {
    const raw = localStorage.getItem("intellidesk_user");
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch (e) {
      return null;
    }
  },

  getUserRole() {
    const user = this.getUser();
    return user ? user.role : null;
  },

  isAuthenticated() {
    return !!this.getToken() && !!this.getUser();
  },

  async login(email, password) {
    const res = await api.post("/auth/login", { email, password });
    if (res.success && res.data) {
      localStorage.setItem("intellidesk_token", res.data.token);
      localStorage.setItem("intellidesk_user", JSON.stringify(res.data));
      return res.data;
    }
    throw new Error(res.message || "Login failed");
  },

  async register(data) {
    const res = await api.post("/auth/register", data);
    return res;
  },

  logout() {
    localStorage.removeItem("intellidesk_token");
    localStorage.removeItem("intellidesk_user");
    window.location.href = "login.html";
  },

  requireAuth(allowedRoles = []) {
    if (!this.isAuthenticated()) {
      window.location.href = "login.html";
      return false;
    }

    const role = this.getUserRole();
    if (allowedRoles.length > 0 && !allowedRoles.includes(role)) {
      alert("Access Denied: You do not have permission to access this page.");
      window.location.href = "dashboard.html";
      return false;
    }

    return true;
  },

  renderLayout(pageTitle = "IntelliDesk") {
    const user = this.getUser() || { fullName: "Guest User", role: "EMPLOYEE", email: "" };
    const role = user.role || "EMPLOYEE";
    const currentPage = window.location.pathname.split("/").pop() || "dashboard.html";

    // Set page title
    document.title = `${pageTitle} — IntelliDesk`;

    // Render Navigation Sidebar
    const sidebarEl = document.getElementById("sidebar-container");
    if (sidebarEl) {
      const initials = (user.fullName || "User").split(" ").map(n => n[0]).join("").substring(0, 2).toUpperCase();

      sidebarEl.innerHTML = `
        <div class="sidebar" id="appSidebar">
          <div class="sidebar-brand">
            <div class="sidebar-brand-icon">ID</div>
            <span>IntelliDesk</span>
          </div>

          <ul class="sidebar-nav">
            <li class="nav-section-title">Core</li>
            <li class="nav-item">
              <a href="dashboard.html" class="nav-link ${currentPage === 'dashboard.html' ? 'active' : ''}">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/></svg>
                <span>Dashboard</span>
              </a>
            </li>

            <li class="nav-section-title">Tickets</li>
            ${role === 'EMPLOYEE' ? `
              <li class="nav-item">
                <a href="tickets.html?view=my" class="nav-link ${currentPage === 'tickets.html' && window.location.search.includes('view=my') ? 'active' : ''}">
                  <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/></svg>
                  <span>My Tickets</span>
                </a>
              </li>
            ` : `
              <li class="nav-item">
                <a href="tickets.html" class="nav-link ${currentPage === 'tickets.html' ? 'active' : ''}">
                  <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/></svg>
                  <span>All Tickets</span>
                </a>
              </li>
            `}

            <li class="nav-item">
              <a href="create-ticket.html" class="nav-link ${currentPage === 'create-ticket.html' ? 'active' : ''}">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
                <span>Create Ticket</span>
              </a>
            </li>

            <li class="nav-section-title">Support & Intelligence</li>
            <li class="nav-item">
              <a href="ai-assistant.html" class="nav-link ${currentPage === 'ai-assistant.html' ? 'active' : ''}">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/></svg>
                <span>AI Assistant</span>
              </a>
            </li>
            <li class="nav-item">
              <a href="knowledge-base.html" class="nav-link ${currentPage === 'knowledge-base.html' ? 'active' : ''}">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/></svg>
                <span>Knowledge Base</span>
              </a>
            </li>

            ${role === 'ADMIN' ? `
              <li class="nav-section-title">Administration</li>
              <li class="nav-item">
                <a href="users.html" class="nav-link ${currentPage === 'users.html' ? 'active' : ''}">
                  <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/></svg>
                  <span>User Management</span>
                </a>
              </li>
              <li class="nav-item">
                <a href="reports.html" class="nav-link ${currentPage === 'reports.html' ? 'active' : ''}">
                  <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/></svg>
                  <span>Analytics & Reports</span>
                </a>
              </li>
            ` : ''}

            <li class="nav-section-title">Session</li>
            <li class="nav-item">
              <a href="javascript:void(0)" onclick="auth.logout()" class="nav-link">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/></svg>
                <span>Logout</span>
              </a>
            </li>
          </ul>

          <div class="sidebar-footer">
            <div class="user-profile-summary">
              <div class="user-avatar">${initials}</div>
              <div class="user-meta">
                <div class="user-name" title="${escapeHtml(user.fullName)}">${escapeHtml(user.fullName)}</div>
                <span class="user-role-badge">${role}</span>
              </div>
            </div>
          </div>
        </div>
      `;
    }

    // Render Header
    const headerEl = document.getElementById("top-header-container");
    if (headerEl) {
      headerEl.innerHTML = `
        <header class="top-header">
          <div class="header-left">
            <button class="mobile-toggle-btn" onclick="toggleSidebar()" aria-label="Toggle navigation">☰</button>
            <h1 class="header-title">${escapeHtml(pageTitle)}</h1>
          </div>
          <div class="header-right">
            <div class="status-pill" id="systemStatusPill">
              <span class="dot"></span>
              <span>API Online</span>
            </div>
            <span class="badge role-${role.toLowerCase()}">${role}</span>
          </div>
        </header>
      `;
    }
  }
};

function toggleSidebar() {
  const sb = document.getElementById("appSidebar");
  if (sb) {
    sb.classList.toggle("open");
  }
}
