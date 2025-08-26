// src/main/resources/static/admin/js/auth.js

// === 1. JWT INTERCEPTOR ===
// Automatically add JWT to all fetch calls
const originalFetch = window.fetch;
window.fetch = function(...args) {
    const [resource, config] = args;
    const token = getJwtToken();
    if (token && isTokenValid(token)) {
        const newConfig = {
            ...config,
            headers: {
                ...config?.headers,
                "Authorization": `Bearer ${token}`
            }
        };
        return originalFetch(resource, newConfig);
    }
    return originalFetch(resource, config);
};

// === 2. TOKEN MANAGEMENT ===
// Get JWT from localStorage
function getJwtToken() {
    return localStorage.getItem("jwtToken");
}

// Check if token is valid (not expired)
function isTokenValid(token) {
    try {
        const payload = JSON.parse(atob(token.split(".")[1]));
        return payload.exp * 1000 > Date.now();
    } catch (e) {
        return false;
    }
}

// Save JWT and user data
function saveAuthData(token, user) {
    localStorage.setItem("jwtToken", token);
    localStorage.setItem("currentUser", JSON.stringify(user));
}

// Clear auth data
function clearAuthData() {
    localStorage.removeItem("jwtToken");
    localStorage.removeItem("currentUser");
}

// === 3. AUTH STATE CHECK ===
// Is user authenticated?
function isAuthenticated() {
    const token = getJwtToken();
    return token && isTokenValid(token);
}

// Get current user
function getCurrentUser() {
    const user = localStorage.getItem("currentUser");
    return user ? JSON.parse(user) : null;
}

// === 4. LOGOUT FUNCTION ===
// Logout: clear data and redirect
function logout() {
    clearAuthData();
    window.location.href = "/admin/login.html";
}

// === 5. AUTH GUARD ===
// Redirect if not authenticated
function requireAuth() {
    if (!isAuthenticated()) {
        window.location.href = "/admin/login.html";
        return false;
    }
    return true;
}

// === 6. AUTO-REFRESH USER (Optional) ===
// Refresh user data from localStorage
function refreshUserOnPage() {
    const user = getCurrentUser();
    const nameEl = document.getElementById("adminName");
    if (user && nameEl) {
        nameEl.textContent = user.firstName || "Admin";
    }
}

// === 7. INITIALIZE ON DOM LOAD ===
document.addEventListener("DOMContentLoaded", () => {
    // Ensure auth state on every admin page
    if (window.location.pathname.startsWith("/admin/")) {
        if (!isAuthenticated()) {
            // Only redirect if not on login page
            if (!window.location.pathname.includes("login.html")) {
                window.location.href = "/admin/login.html";
            }
        } else {
            refreshUserOnPage(); // Set welcome name
        }
    }
});