document.addEventListener("DOMContentLoaded", function () {
    loadUserDirectory();
});

/**
 * Convert a raw role enum ("ROLE_SYSTEM_ADMIN", "SYSTEM_ADMIN", etc.)
 * into a friendly label matching the report (Section 1.2).
 */
function friendlyRole(raw) {
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

function escapeHtml(s) {
    return String(s == null ? "" : s)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function loadUserDirectory() {
    const actorId = localStorage.getItem("userId") || "1";

    fetch(`/api/users?actorId=${encodeURIComponent(actorId)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to fetch user directory (HTTP " + response.status + ")");
            }
            return response.json();
        })
        .then(users => {
            const tbody = document.getElementById("userTableBody");
            tbody.innerHTML = "";

            if (!users || users.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="6" class="text-center py-4" style="color: var(--text-3);">
                            No users found.
                        </td>
                    </tr>`;
                return;
            }

            users.forEach(user => {
                // 3-digit badge ("001", "002")
                const userIdNum = user.id || user.userId || 0;
                const formattedBadge = String(userIdNum).padStart(3, '0');

                // Full name fallback to username
                let fullName = `${user.firstName || ''} ${user.lastName || ''}`.trim();
                if (!fullName) {
                    fullName = user.username || "Employee User";
                }

                // Department (FR-UM-04)
                const deptName = user.department && user.department.name
                    ? user.department.name
                    : "Not Assigned";

                // Role (FR-UM-03) — friendly label matching the report
                const rawRole = user.role && user.role.roleType ? user.role.roleType : "ROLE_EMPLOYEE";
                const roleLabel = friendlyRole(rawRole);
                const isAdminRole = /ADMIN/.test(rawRole.toUpperCase());
                const roleBadgeClass = isAdminRole ? "badge-admin" : "badge-role";

                // Status badge (FR-UM-06)
                const isOnline = user.status === "ONLINE";
                const statusBadge = isOnline
                    ? `<span class="badge-online">Online</span>`
                    : `<span class="badge-offline">Offline</span>`;

                const row = `
                    <tr>
                        <td class="fw-bold" style="color: var(--accent);">${formattedBadge}</td>
                        <td class="fw-semibold">${escapeHtml(fullName)}</td>
                        <td style="color: var(--text-3);">${escapeHtml(user.email || 'N/A')}</td>
                        <td>${escapeHtml(deptName)}</td>
                        <td><span class="${roleBadgeClass}">${escapeHtml(roleLabel)}</span></td>
                        <td>${statusBadge}</td>
                    </tr>
                `;
                tbody.innerHTML += row;
            });
        })
        .catch(error => {
            console.error("Error loading user directory:", error);
            const tbody = document.getElementById("userTableBody");
            if (tbody) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="6" class="text-center py-4" style="color: var(--danger-text);">
                            Failed to load users. Check that you are logged in as an administrator.
                        </td>
                    </tr>`;
            }
        });
}