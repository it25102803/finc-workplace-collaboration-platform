(function () {
    const role = (localStorage.getItem("role") || "").replace("ROLE_", "");

    // 1. Reveal admin-only sidebar links
    if (role === "SYSTEM_ADMIN") {
        const adminLink = document.getElementById("adminSettingsLink");
        if (adminLink) adminLink.style.display = "";

        const dirLink = document.getElementById("userDirectoryLink");
        if (dirLink) dirLink.style.display = "";
    }

    // 2. Page guard — redirect non-admins away from admin-only pages
    const path = window.location.pathname.toLowerCase();
    const adminOnlyPages = ["user-directory.html", "admin-settings.html"];
    const onAdminPage = adminOnlyPages.some(p => path.endsWith(p));
    if (onAdminPage && role !== "SYSTEM_ADMIN") {
        window.location.replace("dashboard.html");
    }
})();