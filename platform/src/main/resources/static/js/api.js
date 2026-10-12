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
    window.withAuth = function (url) {
        return addActor(url);
    };
})();