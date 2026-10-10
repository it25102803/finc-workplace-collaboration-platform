document.addEventListener("DOMContentLoaded", () => {
    // 1. Retrieve session data from localStorage
    const userSession = JSON.parse(localStorage.getItem("user") || "{}");
    const username = localStorage.getItem("username") || userSession.username || "Employee";
    const rawId = localStorage.getItem("badgeId") || localStorage.getItem("userId") || userSession.userId || "1";
    const status = localStorage.getItem("status") || "Online";
    const departmentName = localStorage.getItem("departmentName") || (userSession.department ? userSession.department.name : "Software Engineering");
    const roleName = localStorage.getItem("role") || (userSession.role ? userSession.role.replace("ROLE_", "") : "EMPLOYEE");

    // 2. Format numerical ID strictly as clean 3 digits ("001", "002", "003")
    const cleanNumericId = String(rawId).replace(/[^0-9]/g, "");
    const formattedId = cleanNumericId ? cleanNumericId.padStart(3, "0") : "001";

    // 3. Render Header, Welcome Banner, Role & Department (FR-UM-04, FR-UM-08)
    const displayNameElem = document.getElementById("displayName");
    const welcomeHeadingElem = document.getElementById("welcomeHeading");
    const userDepartmentElem = document.getElementById("userDepartment");
    const userRoleBadgeElem = document.getElementById("userRoleBadge");

    if (displayNameElem) displayNameElem.textContent = username;
    if (welcomeHeadingElem) welcomeHeadingElem.textContent = `Welcome back, ${username}!`;
    if (userDepartmentElem) userDepartmentElem.textContent = departmentName;
    if (userRoleBadgeElem) userRoleBadgeElem.textContent = roleName.replace("ROLE_", "");

    // 4. Update Metric Cards
    const userScopeEl = document.getElementById("metric-user-scope");
    if (userScopeEl) {
        userScopeEl.textContent = formattedId;
    }

    const systemStatusEl = document.getElementById("metric-system-status");
    if (systemStatusEl) {
        systemStatusEl.textContent = status;
    }

    // 5. Load Task & File Summary Metrics (FR-UM-08)
    loadDashboardSummaries(cleanNumericId || "1");

    // 6. Session Logout & Database Status Sync (FR-UM-07)
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
});

/**
 * Fetches real-time task and file counts for the active user dashboard summary (FR-UM-08)
 */
async function loadDashboardSummaries(userId) {
    // Fetch Active Tasks Count
    try {
        const tasksRes = await fetch(`/api/tasks/user/${userId}`);
        if (tasksRes.ok) {
            const tasks = await tasksRes.json();
            const activeTasksCount = Array.isArray(tasks) ? tasks.length : 0;
            const taskMetricEl = document.getElementById("metric-tasks");
            if (taskMetricEl) taskMetricEl.textContent = activeTasksCount;
        }
    } catch (err) {
        console.warn("Could not load tasks metric summary:", err);
    }

    // Fetch Files Count / Articles
    try {
        const filesRes = await fetch(`/api/files`);
        if (filesRes.ok) {
            const files = await filesRes.json();
            const filesCount = Array.isArray(files) ? files.length : 0;
            const articlesMetricEl = document.getElementById("metric-articles");
            if (articlesMetricEl) articlesMetricEl.textContent = filesCount;
        }
    } catch (err) {
        console.warn("Could not load files metric summary:", err);
    }
}