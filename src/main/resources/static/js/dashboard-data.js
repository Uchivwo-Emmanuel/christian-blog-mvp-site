// src/main/resources/static/admin/js/dashboard-data.js

document.addEventListener("DOMContentLoaded", () => {
    // === Ensure user is authenticated ===
    if (!isAuthenticated()) {
        alert("Session expired. Please log in again.");
        logout();
        return;
    }

    // === DOM Elements ===
    const totalCategoriesEl = document.getElementById("totalCategories");
    const totalPostsEl = document.getElementById("totalPosts");
    const totalAdminsEl = document.getElementById("totalAdmins");
    const lastUpdatedEl = document.getElementById("lastUpdated");
    const recentCategoriesEl = document.getElementById("recentCategories");
    const recentPostsEl = document.getElementById("recentPosts");
    const recentAdminsEl = document.getElementById("recentAdmins");

    // === Fetch dashboard stats ===
    fetch("/api/stats")
        .then(res => {
            if (!res.ok) throw new Error("Failed to load stats");
            return res.json();
        })
        .then(stats => {
            if (totalCategoriesEl) totalCategoriesEl.textContent = stats.totalCategories;
            if (totalPostsEl) totalPostsEl.textContent = stats.totalPosts;
            if (totalAdminsEl) totalAdminsEl.textContent = stats.totalAdmins;

            if (lastUpdatedEl && stats.lastUpdated) {
                const date = new Date(stats.lastUpdated);
                lastUpdatedEl.textContent = date.toLocaleDateString('en-US', {
                    month: 'short',
                    day: 'numeric',
                    year: 'numeric'
                });
            }
        })
        .catch(err => {
            console.error("Error loading dashboard stats:", err);
            if (err.message.includes("Unauthorized") || !isAuthenticated()) {
                alert("Session expired. Redirecting to login.");
                logout();
            }
        });

    // === Fetch recent items ===
    Promise.all([
        fetch("/api/get-categories?size=5").then(r => r.json()),
        fetch("/api/posts?size=5").then(r => r.json()),
        fetch("/api/admins?size=5").then(r => r.json())
    ])
        .then(([catsRes, postsRes, adminsRes]) => {
            const categories = catsRes.categories || [];
            const posts = postsRes.posts || [];
            const admins = adminsRes.admins || [];

            // Render lists without actions
            renderList(recentCategoriesEl, categories, 'title');
            renderList(recentPostsEl, posts, 'title', post => new Date(post.createdOn).toLocaleDateString('en-US', {
                month: 'short',
                day: 'numeric'
            }));
            renderAdminList(recentAdminsEl, admins);
        })
        .catch(err => {
            console.error("Failed to load recent items:", err);
        });

    // === Render generic list (title + optional detail) ===
    function renderList(container, items, titleKey, getDetail = null) {
        if (!container) return;
        container.innerHTML = '';
        const empty = container.nextElementSibling;

        if (items.length === 0) {
            if (empty && empty.classList.contains('empty')) {
                empty.style.display = 'block';
            }
            return;
        }

        if (empty) empty.style.display = 'none';

        items.forEach(item => {
            const li = document.createElement('li');
            const detail = getDetail ? getDetail(item) : null;
            li.innerHTML = `
                <strong>${escapeHtml(item[titleKey])}</strong>
                ${detail ? `<span>${escapeHtml(detail)}</span>` : ''}
            `;
            container.appendChild(li);
        });
    }

    // === Render admin list (name + email) ===
    function renderAdminList(container, admins) {
        if (!container) return;
        container.innerHTML = '';
        const empty = container.nextElementSibling;

        if (admins.length === 0) {
            if (empty && empty.classList.contains('empty')) {
                empty.style.display = 'block';
            }
            return;
        }

        empty.style.display = 'none';

        admins.forEach(admin => {
            const li = document.createElement('li');
            li.innerHTML = `
                <strong>${escapeHtml(admin.firstName + ' ' + admin.lastName)}</strong>
                <span>${escapeHtml(admin.appUserEmail)}</span>
            `;
            container.appendChild(li);
        });
    }

    // === Utility: Escape HTML to prevent XSS ===
    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
});