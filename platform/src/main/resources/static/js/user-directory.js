document.addEventListener("DOMContentLoaded", function () {
    loadUserDirectory();
});

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
                        <td colspan="6" class="text-center text-muted py-4">
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

                // Role (FR-UM-03)
                let roleName = "EMPLOYEE";
                if (user.role) {
                    roleName = user.role.roleType
                        ? user.role.roleType.replace("ROLE_", "")
                        : "EMPLOYEE";
                }
                const roleBadgeClass = roleName.includes("ADMIN") ? "badge-admin" : "badge-role";

                // Status badge (FR-UM-06)
                const isOnline = user.status === "ONLINE";
                const statusBadge = isOnline
                    ? `<span class="badge-online">ONLINE</span>`
                    : `<span class="badge-offline">OFFLINE</span>`;

                const row = `
                    <tr>
                        <td class="fw-bold text-info">${formattedBadge}</td>
                        <td class="fw-semibold">${fullName}</td>
                        <td class="text-secondary">${user.email || 'N/A'}</td>
                        <td>${deptName}</td>
                        <td><span class="${roleBadgeClass}">${roleName}</span></td>
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
                        <td colspan="6" class="text-center text-danger py-4">
                            Failed to load users. Check that you are logged in as an administrator.
                        </td>
                    </tr>`;
            }
        });
}