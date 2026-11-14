/**
 * Shared helper for every page. Load it in <head>, before any other script.
 * Every same-site call to /api/... automatically carries the logged-in user's actorId,
 * so pages can use plain fetch() or apiFetch().
 */
(function () {
    if (window.__fincApiInstalled) return;
    window.__fincApiInstalled = true;

    const nativeFetch = window.fetch.bind(window);

    function userId() {
        try { return localStorage.getItem("userId") || ""; } catch (e) { return ""; }
    }

    function isApiUrl(url) {
        if (typeof url !== "string") return false;
        if (url.startsWith("/api/")) return true;
        return url.startsWith(window.location.origin + "/api/");
    }

    function addActor(url) {
        const id = userId();
        if (!id || /[?&]actorId=/.test(url)) return url;
        return url + (url.includes("?") ? "&" : "?") + "actorId=" + encodeURIComponent(id);
    }

    window.fetch = function (input, options) {
        if (typeof input !== "string" || !isApiUrl(input) || input.includes("/api/auth/")) {
            return nativeFetch(input, options);
        }
        return nativeFetch(addActor(input), options);
    };

    // Kept for pages that already call apiFetch(); it now behaves exactly like fetch().
    window.apiFetch = function (url, options) {
        return window.fetch(url, options || {});
    };

    // For plain links (downloads, previews) that can't go through fetch().
    // "IN_PROGRESS" -> "In progress", "ROLE_TEAM_LEADER" -> "Team leader", "PUBLIC" -> "Public"
    window.prettyLabel = function (value) {
        if (value === null || value === undefined) return "";
        const text = String(value).replace(/^ROLE_/, "").replace(/_/g, " ").toLowerCase();
        return text.charAt(0).toUpperCase() + text.slice(1);
    };

    // Phones and tablets: add a ☰ button to the page header that slides the sidebar in and out
    // (the layout itself is in css/brand.css)
    document.addEventListener("DOMContentLoaded", function () {
        const sidebar = document.querySelector(".app-sidebar, .app-layout > .sidebar, .app > .sidebar, .app-nav");
        if (!sidebar) return;
        const header = document.querySelector(".top-navbar, .top-header, .main-content > .topbar, .workspace-header");
        const button = document.createElement("button");
        button.type = "button";
        button.className = "finc-menu-btn";
        button.setAttribute("aria-label", "Open menu");
        button.innerHTML = '<i class="fa-solid fa-bars"></i>';
        const backdrop = document.createElement("div");
        backdrop.className = "finc-nav-backdrop";
        document.body.appendChild(backdrop);
        if (header) {
            header.insertBefore(button, header.firstChild);
        } else {
            document.body.insertBefore(button, document.body.firstChild);
        }
        const setOpen = open => document.body.classList.toggle("finc-nav-open", open);
        button.addEventListener("click", event => {
            event.stopPropagation();
            setOpen(!document.body.classList.contains("finc-nav-open"));
        });
        backdrop.addEventListener("click", () => setOpen(false));
        sidebar.addEventListener("click", event => {
            if (event.target.closest("a")) setOpen(false);
        });
        document.addEventListener("keydown", event => {
            if (event.key === "Escape") setOpen(false);
        });
    });

    window.withAuth = function (url) {
        return addActor(url);
    };
})();