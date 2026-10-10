document.addEventListener("DOMContentLoaded", () => {
    // 1. Retrieve session data
    const username = localStorage.getItem("username") || "User";
    const rawId = localStorage.getItem("badgeId") || localStorage.getItem("userId") || "1";
    const status = localStorage.getItem("status") || "Online";

    // 2. Format numerical ID ("001", "002", "003")
    const cleanNumericId = rawId.replace(/[^0-9]/g, "");
    const formattedId = cleanNumericId ? cleanNumericId.padStart(3, "0") : "001";

    // 3. Render Header & Welcome Banner
    const displayNameElem = document.getElementById("displayName");
    const welcomeHeadingElem = document.getElementById("welcomeHeading");

    if (displayNameElem) displayNameElem.textContent = username;
    if (welcomeHeadingElem) welcomeHeadingElem.textContent = `Welcome back, ${username}!`;

    // 4. Update Metric Cards
    const userScopeEl = document.getElementById("metric-user-scope");
    if (userScopeEl) {
        userScopeEl.textContent = formattedId;
    }

    const systemStatusEl = document.getElementById("metric-system-status");
    if (systemStatusEl) {
        systemStatusEl.textContent = status;
    }

    // 5. Logout & Database Status Synchronization (FR-UM-07)
    const logoutBtn = document.getElementById("logoutBtn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", async () => {
            const userId = localStorage.getItem("userId");
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