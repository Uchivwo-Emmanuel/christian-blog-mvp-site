// admin-management.js
document.addEventListener("DOMContentLoaded", () => {
    // === Modal Elements ===
    const editModal = document.getElementById("editModal");
    const deleteModal = document.getElementById("deleteModal");
    const closeModal = document.getElementById("closeModal");
    const cancelEdit = document.getElementById("cancelEdit");
    const cancelDelete = document.getElementById("cancelDelete");
    const confirmDelete = document.getElementById("confirmDelete");
    const editForm = document.getElementById("editAdminForm");
    const adminsGrid = document.getElementById("adminsGrid");

    // === Image Elements ===
    const editImageInput = document.getElementById("editAdminImage");
    const currentImagePreview = document.getElementById("currentImagePreview");
    const currentImage = document.getElementById("currentImage");
    const newImagePreview = document.getElementById("newImagePreview");
    const newImage = document.getElementById("newImage");
    const removeNewImage = document.getElementById("removeNewImage");

    // === Safety Check ===
    if (!editModal || !deleteModal || !adminsGrid || !editForm) {
        console.error("Essential elements not found");
        return;
    }

    // === FETCH & RENDER ADMINS ===
    fetch("/api/admins")
        .then(res => {
            if (!res.ok) throw new Error("Failed to load admins");
            return res.json();
        })
        .then(data => {
            const admins = data.admins || [];
            adminsGrid.innerHTML = "";

            admins.forEach(admin => {
                const firstName = admin.firstName || "Unknown";
                const lastName = admin.lastName || "";
                const fullName = `${firstName} ${lastName}`.trim();
                const email = admin.email || "No email";
                const adminImage = admin.adminImage ? `/uploads/${admin.adminImage}` : "/images/Open-bible.jpg";


                const card = document.createElement("div");
                card.className = "admin-card";

                card.innerHTML = `
                    <div class="admin-card-header">
                        <img src="${adminImage}" alt="Profile" class="admin-avatar" 
                             onerror="this.src='/images/Open_Bible.jpg'; this.classList.add('default');">
                    </div>
                    <div class="admin-info">
                        <h3>${fullName}</h3>
                        <p class="email">${email}</p>
                        <p><small>Created: ${new Date(admin.createdOn).toLocaleDateString()}</small></p>
                    </div>
                    <div class="admin-actions">
                        <button class="btn-edit" 
                                data-id="${admin.id}" 
                                data-firstname="${firstName}"
                                data-lastname="${lastName}"
                                data-email="${email}"
                                data-admin-image="${admin.adminImage || ''}"
                                aria-label="Edit ${fullName}">
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn-delete" 
                                data-id="${admin.id}" 
                                data-name="${fullName}" 
                                aria-label="Delete ${fullName}">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>
                `;

                adminsGrid.appendChild(card);
            });

            attachActionHandlers();
        })
        .catch(err => {
            console.error("Error fetching admins:", err);
            adminsGrid.innerHTML = "<p>Failed to load admins.</p>";
        });

    // === Attach modal handlers after render ===
    function attachActionHandlers() {
        // ✅ EDIT: Fill modal
        document.querySelectorAll(".btn-edit").forEach(btn => {
            btn.addEventListener("click", async function () {
                const id = this.dataset.id;
                const firstName = this.dataset.firstname || "";
                const lastName = this.dataset.lastname || "";
                const email = this.dataset.email || "";
                const adminImage = this.dataset.adminImage || "";

                console.log("Editing admin ID:", id);

                if (!id) {
                    alert("Admin ID is missing!");
                    return;
                }

                // Fill form
                document.getElementById("editAdminId").value = id;
                document.getElementById("editFirstName").value = firstName;
                document.getElementById("editLastName").value = lastName;
                document.getElementById("editEmail").value = email;
                document.getElementById("editPassword").value = "";

                // Current image preview
                if (adminImage) {
                    currentImage.src = `/uploads/${adminImage}`;
                    currentImagePreview.style.display = "block";
                } else {
                    currentImagePreview.style.display = "none";
                }

                // Clear new image preview
                newImagePreview.style.display = "none";
                editImageInput.value = "";

                // Show modal
                editModal.classList.add("show");
                document.body.style.overflow = "hidden";
            });
        });

        // ✅ DELETE: Confirm
        document.querySelectorAll(".btn-delete").forEach(btn => {
            btn.addEventListener("click", function () {
                const name = this.dataset.name || "this admin";
                document.getElementById("deleteAdminName").textContent = name;
                confirmDelete.dataset.id = this.dataset.id;
                deleteModal.classList.add("show");
                document.body.style.overflow = "hidden";
            });
        });
    }

    // === CLOSE MODALS ===
    const closeModals = () => {
        editModal.classList.remove("show");
        deleteModal.classList.remove("show");
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
    if (editImageInput) {
        editImageInput.addEventListener("change", () => {
            const file = editImageInput.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = () => {
                    newImage.src = reader.result;
                    newImagePreview.style.display = "block";
                    currentImagePreview.style.display = "none";
                };
                reader.readAsDataURL(file);
            }
        });
    }

    // === REMOVE NEW IMAGE ===
    if (removeNewImage) {
        removeNewImage.addEventListener("click", () => {
            editImageInput.value = "";
            newImagePreview.style.display = "none";
        });
    }

    // === FORM SUBMISSION: Update Admin ===
    if (editForm) {
        editForm.addEventListener("submit", async e => {
            e.preventDefault();
            const formData = new FormData(editForm);

            // Ensure ID is sent
            if (!formData.get("id")) {
                alert("Admin ID is missing!");
                return;
            }

            try {
                const res = await fetch("/api/admin/update-admin", {
                    method: "POST",
                    body: formData
                });

                if (res.ok) {
                    alert("Admin updated successfully!");
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
                const res = await fetch(`/api/delete-admin/${id}`, { method: "DELETE" });
                if (res.ok) {
                    alert("Admin deleted.");
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