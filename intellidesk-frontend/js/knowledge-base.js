/**
 * Knowledge Base Controller
 */

let allArticles = [];

document.addEventListener("DOMContentLoaded", async () => {
  if (!auth.requireAuth()) return;
  auth.renderLayout("Knowledge Base");

  await loadArticles();

  // Search input with debounce
  document.getElementById("kb-search")?.addEventListener("input", debounce(async (e) => {
    const keyword = e.target.value.trim();
    await searchArticles(keyword);
  }, 300));

  // Admin / Agent Article Create form
  const role = auth.getUserRole();
  const createBtn = document.getElementById("create-article-btn");
  if (createBtn) {
    if (role === "ADMIN" || role === "IT_AGENT") {
      createBtn.style.display = "inline-flex";
    }
  }

  // Load categories into modal select
  loadCategorySelect();
});

async function loadArticles() {
  const container = document.getElementById("articles-grid");
  if (!container) return;

  container.innerHTML = `<div style="grid-column: 1/-1; text-align:center; padding:3rem;">Loading solutions...</div>`;

  try {
    const res = await api.get("/knowledge?size=50");
    if (res.success && res.data) {
      allArticles = res.data.content || [];
      renderArticles(allArticles);
    }
  } catch (err) {
    container.innerHTML = `<div style="grid-column: 1/-1; text-align:center; color:#ef4444; padding:2rem;">Error: ${escapeHtml(err.message)}</div>`;
  }
}

async function searchArticles(keyword) {
  const container = document.getElementById("articles-grid");
  if (!container) return;

  if (!keyword) {
    renderArticles(allArticles);
    return;
  }

  try {
    const res = await api.get(`/knowledge/search?keyword=${encodeURIComponent(keyword)}`);
    if (res.success && res.data) {
      renderArticles(res.data);
    }
  } catch (err) {
    showToast("Search failed: " + err.message, "error");
  }
}

function renderArticles(articles) {
  const container = document.getElementById("articles-grid");
  if (!container) return;

  if (articles.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1/-1; text-align:center; padding:3rem; background:white; border-radius:12px; border:1px solid #e2e8f0;">
        <p style="font-size:1.1rem; font-weight:600; color:#1e293b; margin-bottom:0.5rem;">No solutions found</p>
        <p style="color:#64748b; margin-bottom:1.5rem;">Try adjusting your search terms or ask our AI assistant.</p>
        <a href="ai-assistant.html" class="btn btn-primary">Ask IntelliDesk AI</a>
      </div>
    `;
    return;
  }

  container.innerHTML = articles.map(art => `
    <div class="article-card" onclick="viewArticleDetails(${art.id})">
      <div style="margin-bottom:0.4rem;">
        <span class="badge" style="background:#e0e7ff; color:#3730a3;">${escapeHtml(art.categoryName || 'General')}</span>
      </div>
      <h3 class="article-title">${escapeHtml(art.title)}</h3>
      <p class="article-excerpt">${escapeHtml(art.content)}</p>
      <div class="article-footer">
        <span>By ${escapeHtml(art.createdByName || 'IT Admin')}</span>
        <span>${formatDate(art.createdAt)}</span>
      </div>
    </div>
  `).join("");
}

async function viewArticleDetails(id) {
  try {
    const res = await api.get(`/knowledge/${id}`);
    if (res.success && res.data) {
      const art = res.data;
      document.getElementById("modal-article-title").textContent = art.title;
      document.getElementById("modal-article-category").textContent = art.categoryName || "General";
      document.getElementById("modal-article-content").innerText = art.content;
      document.getElementById("modal-article-date").textContent = "Updated " + formatDate(art.updatedAt || art.createdAt);

      openModal("article-modal");
    }
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function loadCategorySelect() {
  const select = document.getElementById("article-category-select");
  if (!select) return;

  try {
    const res = await api.get("/categories");
    if (res.success && res.data) {
      select.innerHTML = res.data.map(c => `<option value="${c.id}">${escapeHtml(c.name)}</option>`).join("");
    }
  } catch (e) {}
}

async function saveArticle(event) {
  event.preventDefault();
  const title = document.getElementById("article-title-input").value.trim();
  const categoryId = document.getElementById("article-category-select").value;
  const keywords = document.getElementById("article-keywords-input").value.trim();
  const content = document.getElementById("article-content-input").value.trim();

  if (!title || !content || !categoryId) {
    showToast("Please fill in all required fields", "warning");
    return;
  }

  try {
    const res = await api.post("/knowledge", {
      title,
      categoryId: Number(categoryId),
      keywords,
      content,
      published: true
    });

    if (res.success) {
      showToast("Knowledge article published successfully!", "success");
      closeModal("create-article-modal");
      document.getElementById("create-article-form").reset();
      await loadArticles();
    }
  } catch (err) {
    showToast(err.message, "error");
  }
}

function openModal(id) {
  document.getElementById(id)?.classList.add("open");
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
