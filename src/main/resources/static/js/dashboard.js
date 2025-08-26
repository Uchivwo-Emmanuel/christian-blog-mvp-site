document.addEventListener("DOMContentLoaded", () => {
    // === DOM Elements ===
    const hamburger = document.getElementById("hamburger");
    const closeSidebarBtn = document.getElementById("closeSidebar");  // Renamed
    const sidebar = document.getElementById("sidebar");
    const darkModeToggle = document.getElementById("darkModeToggle");
    const body = document.body;

    if (!hamburger || !closeSidebarBtn || !sidebar) {
        console.error("Missing DOM elements");
        return;
    }

    // === Sidebar Functions ===
    const openSidebar = () => {
        sidebar.classList.add("open");
    };

    const closeSidebar = () => {  // This is a function, not a DOM element
        sidebar.classList.remove("open");
    };

    // === Event Listeners ===
    hamburger.addEventListener("click", openSidebar);
    closeSidebarBtn.addEventListener("click", closeSidebar);  // Use the button

    document.querySelectorAll(".nav-link").forEach(link => {
        link.addEventListener("click", () => {
            if (window.innerWidth <= 991) {
                closeSidebar();
            }
        });
    });

    document.addEventListener("click", (e) => {
        const isInsideSidebar = sidebar.contains(e.target);
        const isOnHamburger = hamburger.contains(e.target);
        if (!isInsideSidebar && !isOnHamburger && sidebar.classList.contains("open")) {
            closeSidebar();
        }
    });

    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && sidebar.classList.contains("open")) {
            closeSidebar();
        }
    });

    const handleResize = () => {
        if (window.innerWidth >= 992) {
            closeSidebar();
        }
    };

    window.addEventListener("resize", handleResize);
    handleResize();

    // === Dark Mode Toggle ===
    if (darkModeToggle) {
        const isDarkMode = localStorage.getItem("darkMode") === "true";
        if (isDarkMode) {
            body.classList.add("dark-mode");
        }

        const updateIcon = () => {
            const icon = darkModeToggle.querySelector("i");
            if (!icon) return;
            if (body.classList.contains("dark-mode")) {
                icon.classList.replace("bi-moon-stars", "bi-brightness-high");
            } else {
                icon.classList.replace("bi-brightness-high", "bi-moon-stars");
            }
        };

        updateIcon();

        darkModeToggle.addEventListener("click", () => {
            body.classList.toggle("dark-mode");
            localStorage.setItem("darkMode", body.classList.contains("dark-mode"));
            updateIcon();
        });
    }
});