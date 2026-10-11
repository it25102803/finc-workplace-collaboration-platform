/**
 * API wrapper that auto-appends actorId from localStorage.
 * Falls back to normal fetch if user isn't logged in (for public calls).
 */
window.apiFetch = function (url, options = {}) {
    const actorId = localStorage.getItem("userId");

    const method = (options.method || "GET").toUpperCase();

    // Public endpoints don't need actorId
    const isPublic =
        url.startsWith("/api/auth/") ||
        (method === "GET" && url.startsWith("/api/departments"));

    if (isPublic || !actorId) {
        return fetch(url, options);
    }

    const sep = url.includes("?") ? "&" : "?";
    return fetch(`${url}${sep}actorId=${encodeURIComponent(actorId)}`, options);
};