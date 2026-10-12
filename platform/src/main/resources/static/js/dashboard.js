document.addEventListener("DOMContentLoaded", () => {
    const userSession = JSON.parse(localStorage.getItem("user") || "{}");
    const username = localStorage.getItem("username") || userSession.username || "Employee";
    const rawId = localStorage.getItem("badgeId") || localStorage.getItem("userId") || userSession.userId || "1";
    const status = localStorage.getItem("status") || "Online";
    const departmentName = localStorage.getItem("departmentName") || (userSession.department ? userSession.department.name : "Not Assigned");
    const roleName = localStorage.getItem("role") || (userSession.role ? userSession.role : "ROLE_EMPLOYEE");

    const cleanNumericId = String(rawId).replace(/[^0-9]/g, "") || "1";

    const displayNameElem = document.getElementById("displayName");
    const welcomeHeadingElem = document.getElementById("welcomeHeading");
    const userDepartmentElem = document.getElementById("userDepartment");
    const userRoleBadgeElem = document.getElementById("userRoleBadge");

    if (displayNameElem) displayNameElem.textContent = username;
    if (welcomeHeadingElem) welcomeHeadingElem.textContent = `Welcome back, ${username}!`;
    if (userDepartmentElem) userDepartmentElem.textContent = departmentName;
    if (userRoleBadgeElem) userRoleBadgeElem.textContent = friendlyRoleName(roleName);

    const systemStatusEl = document.getElementById("metric-system-status");
    if (systemStatusEl) systemStatusEl.textContent = status;

    // Dashboard summaries — FR-UM-08 (tasks + files)
    loadDashboardSummaries(cleanNumericId);

    const logoutBtn = document.getElementById("logoutBtn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", async () => {
            const userId = localStorage.getItem("userId") || userSession.userId;
            if (userId) {
                try {
                    await fetch(`/api/auth/logout?userId=${userId}`, { method: "POST" });
                } catch (err) {
                    console.error("Logout status update failed:", err);
                }
            }
            localStorage.clear();
            window.location.href = "index.html";
        });
    }

    // Global search (FR-UM-23)
    initGlobalSearch();
});

/**
 * Convert a raw role enum ("ROLE_SYSTEM_ADMIN", "SYSTEM_ADMIN", etc.)
 * into a friendly label matching the report (Section 1.2).
 */
function friendlyRoleName(raw) {
    if (!raw) return "Employee";
    const cleaned = String(raw).replace(/^ROLE_/, "").toUpperCase();
    const map = {
        SYSTEM_ADMIN: "System Administrator",
        DEPT_ADMIN: "Department Administrator",
        TEAM_LEADER: "Team Leader",
        EMPLOYEE: "Employee"
    };
    if (map[cleaned]) return map[cleaned];
    return cleaned
        .toLowerCase()
        .replace(/_/g, " ")
        .replace(/\b\w/g, c => c.toUpperCase());
}

async function loadDashboardSummaries(userId) {
    // Always the logged-in user's numeric id (not the padded badge number shown on screen)
    const actorId = localStorage.getItem("userId") || userId;
    userId = actorId;

    // --- Active tasks (FR-UM-08) ---
    try {
        const tasksRes = await fetch(`/api/tasks/user/${userId}?actorId=${actorId}`);
        if (tasksRes.ok) {
            const tasks = await tasksRes.json();
            const activeTasksCount = Array.isArray(tasks)
                ? tasks.filter(t => t.status !== "COMPLETED").length
                : 0;
            const taskMetricEl = document.getElementById("metric-tasks");
            if (taskMetricEl) taskMetricEl.textContent = activeTasksCount;
        }
    } catch (err) {
        console.warn("Could not load tasks metric summary:", err);
    }

    // --- My files (FR-UM-08) ---
    try {
        const filesRes = await fetch(`/api/files/user/${userId}?actorId=${actorId}`);
        if (filesRes.ok) {
            const files = await filesRes.json();
            const fileCount = Array.isArray(files) ? files.length
                            : (files && typeof files.total === "number") ? files.total
                            : 0;
            const filesMetricEl = document.getElementById("metric-files");
            if (filesMetricEl) filesMetricEl.textContent = fileCount;
        }
    } catch (err) {
        console.warn("Could not load files metric summary:", err);
        // Fail silently — the card simply stays at 0
    }

    // --- Published Knowledge Base articles ---
    try {
        const kbRes = await fetch(`/api/kb/articles?actorId=${actorId}`);
        if (kbRes.ok) {
            const articles = await kbRes.json();
            const articlesEl = document.getElementById("metric-articles");
            if (articlesEl) articlesEl.textContent = Array.isArray(articles) ? articles.length : 0;
        }
    } catch (err) {
        console.warn("Could not load articles metric summary:", err);
    }
}

/* =========================================================
   Global Search — FR-UM-23
   ========================================================= */
function initGlobalSearch() {
    const input = document.getElementById("globalSearchInput");
    const panel = document.getElementById("searchResults");
    if (!input || !panel) return;

    const ICONS = {
        USER: "fa-user",
        DEPARTMENT: "fa-sitemap",
        CHANNEL: "fa-hashtag",
        TASK: "fa-list-check",
        FILE: "fa-file",
        ARTICLE: "fa-book"
    };

    let timer = null;

    input.addEventListener("input", () => {
        clearTimeout(timer);
        const q = input.value.trim();
        if (q.length < 2) {
            panel.style.display = "none";
            panel.innerHTML = "";
            return;
        }

        timer = setTimeout(async () => {
            try {
                const actorId = localStorage.getItem("userId") || "";
                const url = `/api/search?q=${encodeURIComponent(q)}&actorId=${encodeURIComponent(actorId)}`;
                const res = await fetch(url);
                if (!res.ok) throw new Error("Search failed: " + res.status);
                const data = await res.json();

                panel.innerHTML = "";

                if (!data.hits || data.hits.length === 0) {
                    panel.innerHTML = `<div class="search-empty">No results for "${escapeHtml(q)}"</div>`;
                } else {
                    data.hits.forEach(hit => {
                        const a = document.createElement("a");
                        a.className = "result-item";
                        a.href = hit.link || "#";
                        a.innerHTML = `
                            <div class="result-icon"><i class="fa-solid ${ICONS[hit.type] || 'fa-circle'}"></i></div>
                            <div style="min-width:0; flex-grow:1;">
                                <div class="result-title">${escapeHtml(hit.title || "(untitled)")}</div>
                                <div class="result-sub">${escapeHtml(hit.subtitle || "")}</div>
                            </div>
                            <span class="result-type">${escapeHtml(hit.type || "")}</span>
                        `;
                        panel.appendChild(a);
                    });
                }

                panel.style.display = "block";
            } catch (err) {
                console.warn("Search error:", err);
                panel.innerHTML = `<div class="search-empty">Search unavailable.</div>`;
                panel.style.display = "block";
            }
        }, 280);
    });

    input.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            const first = panel.querySelector(".result-item");
            if (first) window.location.href = first.getAttribute("href");
        }
        if (e.key === "Escape") {
            panel.style.display = "none";
        }
    });

    document.addEventListener("click", (e) => {
        if (!e.target.closest(".global-search-wrap")) {
            panel.style.display = "none";
        }
    });

    function escapeHtml(s) {
        return String(s)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }
}