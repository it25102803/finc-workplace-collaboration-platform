document.addEventListener("DOMContentLoaded", () => {
    const API_BASE = "http://localhost:8081/api";

    // 1. Session & UI Setup
    const loggedInUser = localStorage.getItem("username") || "User";
    const userRole = localStorage.getItem("userRole") || "Member 1";

    const displayUserElem = document.getElementById("displayUsername");
    const welcomeNameElem = document.getElementById("welcomeName");
    const scopeElem = document.getElementById("metric-user-scope");

    if (displayUserElem) displayUserElem.textContent = loggedInUser;
    if (welcomeNameElem) welcomeNameElem.textContent = loggedInUser;
    if (scopeElem) scopeElem.textContent = userRole.replace(/_/g, ' ');

    // 2. Logout Handling
    const logoutBtn = document.getElementById("btnLogout");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            localStorage.clear();
            window.location.href = "index.html";
        });
    }

    // 3. Dynamic Data Fetching for Dashboard Metrics
    // Fetch Active Tasks Count
    fetch(`${API_BASE}/tasks`)
        .then(res => res.ok ? res.json() : [])
        .then(tasks => {
            const taskElem = document.getElementById("metric-tasks");
            if (taskElem) {
                taskElem.textContent = Array.isArray(tasks) ? tasks.length : 0;
            }
        })
        .catch(err => {
            console.warn("Tasks endpoint unavailable:", err);
            const taskElem = document.getElementById("metric-tasks");
            if (taskElem) taskElem.textContent = "0";
        });

    // Fetch Articles / Files Count
    fetch(`${API_BASE}/files`)
        .then(res => res.ok ? res.json() : [])
        .then(files => {
            const articleElem = document.getElementById("metric-articles");
            if (articleElem) {
                articleElem.textContent = Array.isArray(files) ? files.length : 0;
            }
        })
        .catch(err => {
            console.warn("Files endpoint unavailable:", err);
            const articleElem = document.getElementById("metric-articles");
            if (articleElem) articleElem.textContent = "0";
        });
});