const API_CATEGORIES = "/api/kb/categories";
const API_ARTICLES = "/api/kb/articles";

const TYPE_LABELS = {
    POLICY: "Policy",
    SOP: "SOP",
    TEMPLATE: "Template",
    TRAINING: "Training",
    FAQ: "FAQ"
};

let categories = [];
let articles = [];
let selectedCategoryId = null;
let openArticle = null;
let searchTimer = null;


document.addEventListener("DOMContentLoaded", () => {


    document.getElementById("btnNewCategory").addEventListener("click", openCategoryModal);
    document.getElementById("btnNewArticle").addEventListener("click", openArticleModal);
    document.getElementById("categoryForm").addEventListener("submit", saveCategory);
    document.getElementById("articleForm").addEventListener("submit", publishArticle);
    document.getElementById("btnAttach").addEventListener("click", attachToOpenArticle);
    document.getElementById("btnArchive").addEventListener("click", archiveOpenArticle);

    document.getElementById("searchInput").addEventListener("input", () => {
        clearTimeout(searchTimer);
        searchTimer = setTimeout(loadArticles, 300);
    });

    document.querySelectorAll("[data-close]").forEach(button => {
        button.addEventListener("click", () => closeModal(button.dataset.close));
    });

    document.querySelectorAll(".modal-overlay").forEach(overlay => {
        overlay.addEventListener("click", event => {
            if (event.target === overlay) closeModal(overlay.id);
        });
    });

    document.addEventListener("keydown", event => {
        if (event.key !== "Escape") return;
        document.querySelectorAll(".modal-overlay.active").forEach(overlay => closeModal(overlay.id));
    });

    if (!getUserId()) {
        document.getElementById("sessionWarning").hidden = false;
        document.getElementById("articleGrid").innerHTML =
            `<div class="empty-state">Log in to read the Knowledge Base.</div>`;
        return;
    }

    if (isEditor()) {
        document.getElementById("btnNewCategory").hidden = false;
        document.getElementById("btnNewArticle").hidden = false;
    } else {
        document.getElementById("readOnlyNote").hidden = false;
    }

    loadCategories().then(loadArticles).then(() => {
        const articleId = Number(new URLSearchParams(window.location.search).get("article"));
        if (articleId) openView(articleId);
    });
});


/* =========================
   SESSION
========================= */

function getUserId() {
    let id = localStorage.getItem("userId");
    if (!id) {
        try {
            id = (JSON.parse(localStorage.getItem("user") || "{}")).userId;
        } catch (e) {
            id = null;
        }
    }
    const number = Number(id);
    return number > 0 ? number : null;
}

function isEditor() {
    const role = localStorage.getItem("role") || "";
    return role.includes("SYSTEM_ADMIN") || role.includes("DEPT_ADMIN");
}

function requireEditor() {
    if (getUserId() && isEditor()) return true;
    alert("Only department or system administrators can change the Knowledge Base.");
    return false;
}

function withActor(url) {
    const actorId = getUserId();
    if (!actorId) return url;
    return url + (url.includes("?") ? "&" : "?") + "actorId=" + encodeURIComponent(actorId);
}

async function readError(response) {
    const text = await response.text();
    try {
        const data = JSON.parse(text);
        return data.error || data.message || text;
    } catch (e) {
        return text;
    }
}


/* =========================
   LOAD DATA
========================= */

async function loadCategories() {
    try {
        const response = await apiFetch(API_CATEGORIES);
        if (!response.ok) throw new Error(await readError(response));
        categories = await response.json();
    } catch (error) {
        console.error(error);
        categories = [];
    }
    renderCategoryChips();
    document.getElementById("statCategories").textContent = categories.length;
}

async function loadArticles() {
    const query = document.getElementById("searchInput").value.trim();

    let url = API_ARTICLES;
    if (query) {
        url = API_ARTICLES + "/search?q=" + encodeURIComponent(query);
    } else if (selectedCategoryId) {
        url = API_CATEGORIES + "/" + selectedCategoryId + "/articles";
    }

    try {
        const response = await apiFetch(url);
        if (!response.ok) throw new Error(await readError(response));
        articles = await response.json();

        if (query && selectedCategoryId) {
            articles = articles.filter(a => a.categoryId === selectedCategoryId);
        }

        renderArticles(query);
        if (!query && !selectedCategoryId) updateStats();

    } catch (error) {
        console.error(error);
        document.getElementById("articleGrid").innerHTML =
            `<div class="empty-state">Unable to load articles. Make sure the Spring Boot app is running.</div>`;
    }
}


/* =========================
   RENDER
========================= */

function renderCategoryChips() {
    const container = document.getElementById("categoryChips");
    container.innerHTML = "";

    container.appendChild(createChip("All", null));
    categories.forEach(category => container.appendChild(createChip(category.name, category.categoryId)));
}

function createChip(label, categoryId) {
    const chip = document.createElement("button");
    chip.type = "button";
    chip.className = "chip" + (selectedCategoryId === categoryId ? " active" : "");
    chip.textContent = label;
    chip.addEventListener("click", () => {
        selectedCategoryId = categoryId;
        renderCategoryChips();
        loadArticles();
    });
    return chip;
}

function renderArticles(query) {
    const grid = document.getElementById("articleGrid");
    grid.innerHTML = "";

    if (articles.length === 0) {
        let message = "No articles here yet. Click \"New Article\" to write the first one.";
        if (query) message = `No articles match "${escapeHtml(query)}".`;
        grid.innerHTML = `<div class="empty-state">${message}</div>`;
        return;
    }

    articles.forEach(article => {
        const card = document.createElement("article");
        card.className = "article-card";
        card.tabIndex = 0;
        card.innerHTML = `
            <div class="badge-row">
                ${typeBadge(article.articleType)}
                <span class="badge badge-category">${escapeHtml(article.categoryName || "")}</span>
            </div>
            <h3>${escapeHtml(article.title)}</h3>
            <p class="article-snippet">${escapeHtml(article.content || "")}</p>
            <div class="article-footer">
                <span><i class="fa-regular fa-user"></i> ${escapeHtml(article.authorName || "Unknown")}</span>
                <span><i class="fa-regular fa-eye"></i> ${article.viewCount}</span>
            </div>
        `;
        card.addEventListener("click", () => openView(article.articleId));
        card.addEventListener("keydown", event => {
            if (event.key === "Enter") openView(article.articleId);
        });
        grid.appendChild(card);
    });
}

function updateStats() {
    document.getElementById("statArticles").textContent = articles.length;
    document.getElementById("statViews").textContent =
        articles.reduce((sum, a) => sum + (a.viewCount || 0), 0);
}

function typeBadge(type) {
    if (!type) return "";
    return `<span class="badge type-${type}">${TYPE_LABELS[type] || type}</span>`;
}


/* =========================
   NEW CATEGORY
========================= */

function openCategoryModal() {
    if (!requireEditor()) return;
    document.getElementById("categoryForm").reset();
    clearMessage("categoryMessage");
    openModal("categoryModal");
}

async function saveCategory(event) {
    event.preventDefault();
    clearMessage("categoryMessage");

    const name = document.getElementById("categoryName").value.trim();
    const description = document.getElementById("categoryDescription").value.trim();

    if (!name) {
        showMessage("categoryMessage", "Category name is required.");
        return;
    }

    try {
        const response = await apiFetch(API_CATEGORIES, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name: name, description: description || null })
        });
        if (!response.ok) throw new Error(await readError(response));

        closeModal("categoryModal");
        await loadCategories();

    } catch (error) {
        showMessage("categoryMessage", error.message || "Unable to create category.");
    }
}


/* =========================
   NEW ARTICLE (publish)
========================= */

function openArticleModal() {
    if (!requireEditor()) return;

    if (categories.length === 0) {
        alert("Create a category first, then write articles inside it.");
        return;
    }

    document.getElementById("articleForm").reset();
    clearMessage("articleMessage");

    const select = document.getElementById("articleCategory");
    select.innerHTML = `<option value="">Choose a category</option>` +
        categories.map(c => `<option value="${c.categoryId}">${escapeHtml(c.name)}</option>`).join("");
    if (selectedCategoryId) select.value = String(selectedCategoryId);

    openModal("articleModal");
}

async function publishArticle(event) {
    event.preventDefault();
    clearMessage("articleMessage");

    const title = document.getElementById("articleTitle").value.trim();
    const content = document.getElementById("articleContent").value.trim();
    const articleType = document.getElementById("articleType").value;
    const categoryId = Number(document.getElementById("articleCategory").value);
    const tags = splitList(document.getElementById("articleTags").value);
    const fileIds = splitList(document.getElementById("articleFiles").value);

    if (!title || !content || !articleType || !categoryId) {
        showMessage("articleMessage", "Required fields missing: title, content, article type and category.");
        return;
    }

    if (fileIds.some(id => !/^\d+$/.test(id))) {
        showMessage("articleMessage", "File IDs must be numbers, separated by commas.");
        return;
    }

    try {
        const response = await apiFetch(API_ARTICLES, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                title: title,
                content: content,
                articleType: articleType,
                tags: tags,
                categoryId: categoryId,
                publish: true
            })
        });
        if (!response.ok) throw new Error(await readError(response));

        const article = await response.json();

        const failedFiles = [];
        for (const fileId of fileIds) {
            const attach = await apiFetch(`${API_ARTICLES}/${article.articleId}/attachments?fileId=${fileId}`, { method: "POST" });
            if (!attach.ok) failedFiles.push(fileId);
        }

        if (failedFiles.length) {
            alert(`Article published, but these files could not be attached: ${failedFiles.join(", ")}`);
        }

        closeModal("articleModal");
        await loadArticles();
        refreshStats();

    } catch (error) {
        showMessage("articleMessage", error.message || "Unable to publish article.");
    }
}

async function refreshStats() {
    try {
        const response = await apiFetch(API_ARTICLES);
        if (!response.ok) return;
        const all = await response.json();
        document.getElementById("statArticles").textContent = all.length;
        document.getElementById("statViews").textContent = all.reduce((sum, a) => sum + (a.viewCount || 0), 0);
    } catch (e) {
        console.error(e);
    }
}


/* =========================
   VIEW ARTICLE
========================= */

async function openView(articleId) {
    clearMessage("viewMessage");

    try {
        const response = await apiFetch(`${API_ARTICLES}/${articleId}`);
        if (!response.ok) throw new Error(await readError(response));
        openArticle = await response.json();
    } catch (error) {
        alert("Unable to open this article.");
        return;
    }

    const a = openArticle;
    document.getElementById("viewBadges").innerHTML =
        `<div class="badge-row">${typeBadge(a.articleType)}<span class="badge badge-category">${escapeHtml(a.categoryName || "")}</span></div>`;
    document.getElementById("viewTitle").textContent = a.title;
    document.getElementById("viewMeta").textContent =
        `By ${a.authorName || "Unknown"} · Published ${formatDate(a.publishedAt)} · ${a.viewCount} ${a.viewCount === 1 ? "view" : "views"}`;
    document.getElementById("viewContent").textContent = a.content;
    document.getElementById("viewTags").innerHTML =
        (a.tags || []).map(t => `<span class="tag">#${escapeHtml(t)}</span>`).join("");

    const canManage = isEditor();
    document.getElementById("btnArchive").hidden = !canManage;
    document.getElementById("attachRow").hidden = !canManage;
    document.getElementById("attachFileId").value = "";

    renderAttachments(a.attachedFileIds || []);
    openModal("viewModal");
    refreshCardViewCount(a);
}

async function renderAttachments(fileIds) {
    const list = document.getElementById("viewAttachments");

    if (fileIds.length === 0) {
        list.innerHTML = `<li class="none">No files attached.</li>`;
        return;
    }

    list.innerHTML = "";
    for (const fileId of fileIds) {
        let name = "File #" + fileId;
        try {
            const response = await apiFetch(`/api/files/${fileId}`);
            if (response.ok) name = (await response.json()).fileName || name;
        } catch (e) {
            console.error(e);
        }

        const item = document.createElement("li");
        item.innerHTML =
            `<a href="${withActor(`/api/files/${fileId}/download`)}" target="_blank"><i class="fa-regular fa-file"></i> ${escapeHtml(name)}</a>`;
        list.appendChild(item);
    }
}

function refreshCardViewCount(updated) {
    const index = articles.findIndex(a => a.articleId === updated.articleId);
    if (index >= 0) {
        articles[index] = updated;
        renderArticles(document.getElementById("searchInput").value.trim());
    }
    refreshStats();
}

async function attachToOpenArticle() {
    clearMessage("viewMessage");
    const fileId = Number(document.getElementById("attachFileId").value);

    if (!fileId) {
        showMessage("viewMessage", "Enter a file ID from the File Manager.");
        return;
    }

    try {
        const response = await apiFetch(`${API_ARTICLES}/${openArticle.articleId}/attachments?fileId=${fileId}`, { method: "POST" });
        if (!response.ok) throw new Error(await readError(response));

        openArticle = await response.json();
        document.getElementById("attachFileId").value = "";
        renderAttachments(openArticle.attachedFileIds || []);
        showMessage("viewMessage", "File attached.", true);

    } catch (error) {
        showMessage("viewMessage", error.message || "Unable to attach file.");
    }
}

async function archiveOpenArticle() {
    if (!confirm(`Archive "${openArticle.title}"? It will no longer appear in the Knowledge Base.`)) return;

    try {
        const response = await apiFetch(`${API_ARTICLES}/${openArticle.articleId}/archive`, { method: "POST" });
        if (!response.ok) throw new Error(await readError(response));

        closeModal("viewModal");
        await loadArticles();
        refreshStats();

    } catch (error) {
        showMessage("viewMessage", error.message || "Unable to archive article.");
    }
}


/* =========================
   HELPERS
========================= */

function openModal(id) {
    document.getElementById(id).classList.add("active");
}

function closeModal(id) {
    document.getElementById(id).classList.remove("active");
}

function showMessage(id, message, success) {
    const element = document.getElementById(id);
    element.textContent = message;
    element.classList.add("show");
    element.classList.toggle("success", !!success);
}

function clearMessage(id) {
    const element = document.getElementById(id);
    element.textContent = "";
    element.classList.remove("show", "success");
}

function splitList(value) {
    return value.split(",").map(v => v.trim()).filter(v => v.length > 0);
}

function formatDate(value) {
    if (!value) return "—";
    const date = new Date(value);
    return isNaN(date.getTime()) ? value : date.toLocaleDateString();
}

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
