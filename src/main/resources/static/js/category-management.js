// category-management.js
document.addEventListener("DOMContentLoaded", () => {
    // === Modal Elements ===
    const editModal = document.getElementById("editModal");
    const deleteModal = document.getElementById("deleteModal");
    const closeModal = document.getElementById("closeModal");
    const cancelEdit = document.getElementById("cancelEdit");
    const cancelDelete = document.getElementById("cancelDelete");
    const confirmDelete = document.getElementById("confirmDelete");
    const editForm = document.getElementById("editCategoryForm");
    const categoriesGrid = document.getElementById("categoriesGrid");

    // === Safety Check ===
    if (!editModal || !deleteModal || !categoriesGrid) {
        console.error("Essential elements not found");
        return;
    }

    // === FETCH & RENDER CATEGORIES ===
    fetch("/api/get-categories")
        .then(res => {
            if (!res.ok) throw new Error("Failed to load categories");
            return res.json();
        })
        .then(data => {
            const categories = data.categories || [];
            categoriesGrid.innerHTML = "";

            categories.forEach(cat => {
                const card = document.createElement("div");
                card.className = "category-card";

                card.innerHTML = `
                    <div class="category-image" ${cat.imageUrl ? '' : 'style="display:none"'} title="${cat.title}">
                        <img src="/uploads/${cat.imageUrl}" 
                             alt="${cat.title}" />
                    </div>
                    <div class="category-info">
                        <h3>${cat.title}</h3>
                        <p>${cat.description ? cat.description : '<em>No description</em>'}</p>
                    </div>
                    <div class="category-actions">
                        <button class="btn-edit" 
                                data-id="${cat.id}" 
                                aria-label="Edit ${cat.title}">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn-delete" 
                                data-id="${cat.id}" 
                                data-title="${cat.title}" 
                                aria-label="Delete ${cat.title}">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>
                `;

                categoriesGrid.appendChild(card);
            });

            attachActionHandlers();
        })
        .catch(err => {
            console.error("Error fetching categories:", err);
            categoriesGrid.innerHTML = "<p>Failed to load categories.</p>";
        });

    // === Attach modal handlers after render ===
    function attachActionHandlers() {
        // ✅ EDIT: Fetch full category data and fill modal
        document.querySelectorAll(".btn-edit").forEach(btn => {
            btn.addEventListener("click", async function () {
                const id = this.dataset.id;
                console.log("Editing category ID:", id);

                if (!id) {
                    alert("Category ID is missing!");
                    return;
                }

                try {
                    const response = await fetch(`/api/categories/${id}`);
                    if (!response.ok) {
                        const errorText = await response.text();
                        throw new Error(`HTTP ${response.status}: ${errorText}`);
                    }

                    const cat = await response.json();
                    console.log("Category loaded:", cat);

                    // Fill basic fields
                    document.getElementById("editCategoryId").value = cat.id;
                    document.getElementById("editTitle").value = cat.title || "";
                    document.getElementById("editDescription").value = cat.description || "";

                    // Current image preview
                    const currentPreview = document.getElementById("currentImagePreview");
                    const currentImage = document.getElementById("currentImage");
                    if (cat.imageName) {
                        currentImage.src = `/uploads/${cat.imageName}`;
                        currentPreview.style.display = "block";
                    } else {
                        currentPreview.style.display = "none";
                    }

                    // Clear new image preview
                    document.getElementById("newImagePreview").style.display = "none";
                    document.getElementById("editImage").value = "";

                    // Show modal
                    editModal.style.display = "flex";
                    document.body.style.overflow = "hidden";

                } catch (err) {
                    console.error("Failed to load category ", err);
                    alert("Could not load category for editing. Check console.");
                }
            });
        });

        // ✅ DELETE: Confirm Deletion
        document.querySelectorAll(".btn-delete").forEach(btn => {
            btn.addEventListener("click", function () {
                const title = this.dataset.title || "this category";
                document.getElementById("deleteCategoryName").textContent = title;
                if (confirmDelete) confirmDelete.dataset.id = this.dataset.id;
                deleteModal.style.display = "flex";
                document.body.style.overflow = "hidden";
            });
        });
    }

    // === CLOSE MODALS ===
    const closeModals = () => {
        editModal.style.display = "none";
        deleteModal.style.display = "none";
        document.body.style.overflow = "";
    };

    [closeModal, cancelEdit, cancelDelete].forEach(el => el?.addEventListener("click", closeModals));
    [editModal, deleteModal].forEach(modal => {
        modal?.addEventListener("click", (e) => {
            if (e.target === modal) closeModals();
        });
    });

    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") closeModals();
    });

    // === IMAGE PREVIEW FOR NEW IMAGE ===
    setupImagePreview(
        document.getElementById("editImage"),
        document.getElementById("newImagePreview"),
        document.getElementById("newImage"),
        document.getElementById("removeNewImage")
    );

    // === IMAGE PREVIEW HELPER ===
    function setupImagePreview(input, preview, img, removeBtn) {
        if (!input || !preview || !img) return;

        input.addEventListener("change", () => {
            const file = input.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = () => {
                    img.src = reader.result;
                    preview.style.display = "block";
                    document.getElementById("currentImagePreview").style.display = "none";
                };
                reader.readAsDataURL(file);
            }
        });

        removeBtn?.addEventListener("click", () => {
            input.value = "";
            preview.style.display = "none";
        });
    }

    // === FORM SUBMISSION: Update Category ===
    if (editForm) {
        editForm.addEventListener("submit", async e => {
            e.preventDefault();
            const formData = new FormData(editForm);

            // Ensure ID is sent
            if (!formData.get("id")) {
                alert("Category ID is missing!");
                return;
            }

            try {
                const res = await fetch("/api/update-category", {
                    method: "POST",
                    body: formData
                });

                if (res.ok) {
                    alert("Category updated successfully!");
                    closeModals();
                    location.reload();
                } else {
                    const error = await res.text();
                    alert("Update failed: " + error);
                }
            } catch (err) {
                alert("Network error. Please try again.");
            }
        });
    }

    // === DELETE CONFIRMATION ===
    if (confirmDelete) {
        confirmDelete.addEventListener("click", async () => {
            const id = confirmDelete.dataset.id;
            try {
                const res = await fetch(`/api/delete-category/${id}`, { method: "DELETE" });
                if (res.ok) {
                    alert("Category deleted.");
                    closeModals();
                    location.reload();
                } else {
                    alert("Delete failed.");
                }
            } catch (err) {
                alert("Network error.");
            }
        });
    }
});
