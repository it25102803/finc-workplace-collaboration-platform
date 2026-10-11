// Shared presence heartbeat — include on every authenticated page.
(function () {
  const userSession = JSON.parse(localStorage.getItem("user") || "{}");
  const userId = localStorage.getItem("userId") || userSession.userId;
  if (!userId) return;

  const PING_URL = `/api/presence/heartbeat?userId=${userId}`;

  // Ping immediately, then every 60 seconds
  fetch(PING_URL, { method: "POST" }).catch(() => {});
  setInterval(() => {
    fetch(PING_URL, { method: "POST" }).catch(() => {});
  }, 60000);

  // Ping when the tab becomes visible again
  document.addEventListener("visibilitychange", () => {
    if (document.visibilityState === "visible") {
      fetch(PING_URL, { method: "POST" }).catch(() => {});
    }
  });

  // NOTE: intentionally NOT sending OFFLINE on beforeunload —
  // it fires on internal navigation too, which caused the bug.
  // The scheduled sweep handles truly-abandoned sessions.
})();