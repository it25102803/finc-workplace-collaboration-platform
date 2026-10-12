/* ============================================================
   Finc Enterprise Portal — Theme Manager (NFR-14)
   - Persists user's light/dark choice in localStorage
   - Applies on every page load (no flash thanks to inline head script)
   - Attaches click handler to any element with [data-theme-toggle]
   ============================================================ */
(function () {
    'use strict';

    function applyTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        try { localStorage.setItem('theme', theme); } catch (e) {}
        updateIcons(theme);
    }

    function currentTheme() {
        return document.documentElement.getAttribute('data-theme') || 'light';
    }

    function toggleTheme() {
        applyTheme(currentTheme() === 'dark' ? 'light' : 'dark');
    }

    function updateIcons(theme) {
        document.querySelectorAll('[data-theme-toggle]').forEach(function (btn) {
            const icon = btn.querySelector('i');
            if (!icon) return;
            icon.classList.toggle('fa-moon', theme !== 'dark');
            icon.classList.toggle('fa-sun', theme === 'dark');
        });
    }

    function init() {
        // Sync icon state on first paint
        updateIcons(currentTheme());

        // Wire up all theme toggles on the page
        document.querySelectorAll('[data-theme-toggle]').forEach(function (btn) {
            btn.addEventListener('click', function (e) {
                e.preventDefault();
                toggleTheme();
            });
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // Expose for other scripts if needed
    window.FincTheme = {
        apply: applyTheme,
        toggle: toggleTheme,
        current: currentTheme
    };
})();