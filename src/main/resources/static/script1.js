document.addEventListener("DOMContentLoaded", function () {
    const iconButton = document.getElementById("iconButton");
    const menuButton = document.getElementById("menuButton");
    const mobileMenu = document.getElementById("navigator");
    const modal = document.getElementById("myModal");
    const openModalBtn = document.getElementById("openModalBtn");
    const closeModal = document.getElementById("closeModal");

    // Toggle mobile menu
    iconButton.addEventListener("click", function (e) {
        e.stopPropagation(); // Prevent event from bubbling up
        mobileMenu.classList.toggle("active");

        // Change icon from ☰ to ✕
        if (menuButton.classList.contains("bi-list")) {
            menuButton.classList.remove("bi-list");
            menuButton.classList.add("bi-x");
        } else {
            menuButton.classList.remove("bi-x");
            menuButton.classList.add("bi-list");
        }
    });

    // Close menu when clicking outside
    document.addEventListener("click", function (e) {
        if (!mobileMenu.contains(e.target) && !iconButton.contains(e.target)) {
            mobileMenu.classList.remove("active");
            menuButton.classList.remove("bi-x");
            menuButton.classList.add("bi-list");
        }
    });

    // Modal functionality
    openModalBtn.addEventListener("click", function () {
        modal.style.display = "flex";
    });

    closeModal.addEventListener("click", function () {
        modal.style.display = "none";
    });

    window.addEventListener("click", function (e) {
        if (e.target === modal) {
            modal.style.display = "none";
        }
    });
});