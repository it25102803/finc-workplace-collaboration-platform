document.addEventListener("DOMContentLoaded", function () {
    loadUserDirectory();
});

function loadUserDirectory() {
    fetch('/api/users')
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to fetch user directory");
            }
            return response.json();
        })
        .then(users => {
            const tbody = document.getElementById("userTableBody");
            tbody.innerHTML = "";

            users.forEach(user => {
                // Formats ID strictly as clean 3-digit numerical string ("001", "002", "003")
                const userIdNum = user.id || user.userId || 0;
                const formattedBadge = String(userIdNum).padStart(3, '0');

                // Determine Name display
                let fullName = `${user.firstName || ''} ${user.lastName || ''}`.trim();
                if (!fullName) {
                    fullName = user.username || "Employee User";
                }

                // Department Allocation (FR-UM-04)
                const deptName = user.department && user.department.name 
                    ? user.department.name 
                    : "Software Engineering";

                // Role Allocation (FR-UM-03)
                let roleName = "EMPLOYEE";
                if (user.role) {
                    roleName = user.role.roleType ? user.role.roleType.replace("ROLE_", "") : "EMPLOYEE";
                }
                const roleBadgeClass = roleName.includes("ADMIN") ? "badge-admin" : "badge-role";

                // Online/Offline Status Badge (FR-UM-06)
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
        });
}