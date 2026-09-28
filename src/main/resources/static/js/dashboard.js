document.addEventListener("DOMContentLoaded", () => {
    // Check if user session data exists
    const loggedInUser = localStorage.getItem("username") || "User";
    
    // Update UI elements with session details
    document.getElementById("displayUsername").textContent = loggedInUser;
    document.getElementById("welcomeName").textContent = loggedInUser;

    // Logout handling
    const logoutBtn = document.getElementById("btnLogout");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            // Clear local storage session
            localStorage.removeItem("username");
            // Redirect back to login page
            window.location.href = "index.html";
        });
    }
});