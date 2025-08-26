// src/main/resources/static/admin/js/manage-post.js

document.addEventListener("DOMContentLoaded", () => {
    // === Modal Elements ===
    const editModal = document.getElementById("editModal");
    const deleteModal = document.getElementById("deleteModal");
    const closeModal = document.getElementById("closeModal");
    const cancelEdit = document.getElementById("cancelEdit");
    const cancelDelete = document.getElementById("cancelDelete");
    const confirmDelete = document.getElementById("confirmDelete");
    const editForm = document.getElementById("editPostForm");
    const postsGrid = document.getElementById("postsGrid");
    const categorySelect = document.getElementById("editCategory");

    // === Safety Check ===
    if (!editModal || !deleteModal || !postsGrid) {
        console.error("Essential elements not found");
        return;
    }

    // === Load Categories First ===
    let categories = [];
    fetch("/api/get-categories")
        .then(res => {
            if (!res.ok) throw new Error("Failed to load categories");
            return res.json();
        })
        .then(data => {
            categories = data.categories || [];
            populateCategoryDropdown(categories);
        })
        .catch(err => {
            console.error("Error loading categories:", err);
            // Still allow editing — user can type category name
        });

    // === Populate Category Dropdown ===
    function populateCategoryDropdown(cats) {
        if (!categorySelect) return;
        categorySelect.innerHTML = '<option value="">Select a category</option>';
        cats.forEach(cat => {
            const option = document.createElement("option");
            option.value = cat.title;
            option.textContent = cat.title;
            categorySelect.appendChild(option);
        });
    }

    // === FETCH & RENDER POSTS ===
    fetch("/api/posts")
        .then(res => {
            if (!res.ok) {
                if (res.status === 401) {
                    throw new Error("Unauthorized: Please log in again");
                }
                throw new Error(`Failed to load posts: ${res.status}`);
            }
            return res.json();
        })
        .then(data => {
            const posts = data.posts || [];
            postsGrid.innerHTML = "";

            if (posts.length === 0) {
                postsGrid.innerHTML = "<p>No posts found. <a href='/admin/make-post.html'>Create one</a>.</p>";
                return;
            }

            posts.forEach(post => {
                const points = Array.isArray(post.points) ? post.points : [];

                const postCard = document.createElement("div");
                postCard.className = "post-card";
                postCard.innerHTML = `
                    <div class="post-image">
                        <img src="/uploads/${post.titleImageName}" alt="${escapeHtml(post.title)}">
                    </div>
                    <div class="post-content">
                        <h3>${escapeHtml(post.title)}</h3>
                        <p class="intro">${escapeHtml(post.introduction)}</p>
                        <p class="category"><strong>Category:</strong> ${escapeHtml(post.categoryName)}</p>
                        <p class="date"><strong>Created:</strong> ${new Date(post.createdOn).toLocaleDateString()}</p>

                        <div class="points">
                            ${points.map(p => `
                                <div class="point">
                                    <h4>${escapeHtml(p.pointTitle || '')}</h4>
                                    <p>${escapeHtml(p.pointBody || '')}</p>
                                    ${p.pointImageName ? `<img src="/uploads/${p.pointImageName}" alt="Point Image"/>` : ""}
                                </div>
                            `).join("")}
                        </div>

                        <div class="post-actions">
                            <button class="btn-edit" data-id="${post.id}">
                                <i class="bi bi-pencil-square"></i> Edit
                            </button>
                            <button class="btn-delete" data-id="${post.id}" data-title="${escapeHtml(post.title)}">
                                <i class="bi bi-trash"></i> Delete
                            </button>
                        </div>
                    </div>
                `;
                postsGrid.appendChild(postCard);
            });

            attachActionHandlers();
        })
        .catch(err => {
            console.error("Error fetching posts:", err);
            if (err.message.includes("Unauthorized")) {
                alert("Session expired. Please log in again.");
                logout();
            } else {
                postsGrid.innerHTML = "<p>Failed to load posts. Please try again later.</p>";
            }
        });

    // === Attach modal handlers after posts render ===
    function attachActionHandlers() {
        // ✅ EDIT: Fetch full post data and fill modal
        document.querySelectorAll(".btn-edit").forEach(btn => {
            btn.addEventListener("click", async function () {
                const id = this.dataset.id;
                console.log("Editing post ID:", id);

                if (!id) {
                    alert("Post ID is missing!");
                    return;
                }

                try {
                    const response = await fetch(`/api/posts/${id}`);
                    if (!response.ok) {
                        if (response.status === 401) {
                            throw new Error("Unauthorized: Please log in again");
                        }
                        const errorText = await response.text();
                        throw new Error(`HTTP ${response.status}: ${errorText}`);
                    }

                    const post = await response.json();
                    console.log("Post loaded:", post);

                    const points = Array.isArray(post.points) ? post.points : [];

                    // Fill basic fields
                    document.getElementById("editPostId").value = post.id;
                    document.getElementById("editTitle").value = post.title || "";
                    document.getElementById("editIntro").value = post.introduction || "";

                    // Set category (match by title)
                    const select = document.getElementById("editCategory");
                    if (select) {
                        const matchingOption = Array.from(select.options).find(opt => opt.value === post.categoryName);
                        if (matchingOption) {
                            select.value = post.categoryName;
                        } else {
                            // If category not in list, add it
                            const newOption = document.createElement("option");
                            newOption.value = post.categoryName;
                            newOption.textContent = post.categoryName;
                            select.appendChild(newOption);
                            select.value = post.categoryName;
                        }
                    }

                    // Title Image Preview
                    const currentImg = document.getElementById("currentTitleImage");
                    const currentPreview = document.getElementById("currentTitleImagePreview");
                    if (post.titleImageName) {
                        currentImg.src = `/uploads/${post.titleImageName}`;
                        currentPreview.style.display = "block";
                    } else {
                        currentPreview.style.display = "none";
                    }

                    // Clear new image preview
                    document.getElementById("newTitleImagePreview").style.display = "none";
                    document.getElementById("editTitleImage").value = "";

                    // Rebuild points in modal
                    const pointsContainer = document.getElementById("pointsContainer");
                    pointsContainer.innerHTML = '<h4>Points</h4>';
                    points.forEach((point, index) => addPointToModal(pointsContainer, index, point));

                    // Show modal
                    editModal.style.display = "flex";
                    document.body.style.overflow = "hidden";

                } catch (err) {
                    console.error("Failed to load post ", err);
                    if (err.message.includes("Unauthorized")) {
                        alert("Session expired. Please log in again.");
                        logout();
                    } else {
                        alert("Could not load post for editing. Check console.");
                    }
                }
            });
        });

        // ✅ DELETE: Confirm Deletion
        document.querySelectorAll(".btn-delete").forEach(btn => {
            btn.addEventListener("click", function () {
                const title = this.dataset.title || "this post";
                document.getElementById("deletePostName").textContent = title;
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

    // === IMAGE PREVIEW FOR TITLE IMAGE ===
    setupImagePreview(
        document.getElementById("editTitleImage"),
        document.getElementById("newTitleImagePreview"),
        document.getElementById("newTitleImage"),
        document.getElementById("removeNewTitleImage")
    );

    // === ADD POINT BUTTON ===
    const addPointBtn = document.getElementById("addPointBtn");
    if (addPointBtn) {
        addPointBtn.addEventListener("click", () => {
            const pointsContainer = document.getElementById("pointsContainer");
            const index = pointsContainer.querySelectorAll(".point-group").length;
            addPointToModal(pointsContainer, index);
        });
    }

    // === ADD POINT TO MODAL ===
    function addPointToModal(container, index, pointData = {}) {
        const pointGroup = document.createElement("div");
        pointGroup.className = "point-group";
        pointGroup.dataset.index = index;

        pointGroup.innerHTML = `
            <div class="form-row">
                <div class="form-group">
                    <label>Point Title</label>
                    <input type="text" name="points[${index}].pointTitle" value="${escapeHtml(pointData.pointTitle || '')}" required />
                </div>
                <div class="form-group">
                    <label>Point Image (Optional)</label>
                    <input type="file" name="points[${index}].pointImage" accept="image/*" />
                    <div class="image-preview" style="display: ${pointData.pointImageName ? 'block' : 'none'};">
                        <img src="${pointData.pointImageName ? `/uploads/${pointData.pointImageName}` : ''}" alt="Preview" />
                        <button type="button" class="remove-image">&times;</button>
                    </div>
                </div>
            </div>
            <div class="form-group">
                <label>Point Body</label>
                <textarea name="points[${index}].pointBody" rows="3">${escapeHtml(pointData.pointBody || '')}</textarea>
            </div>
            <button type="button" class="btn-remove-point">Remove</button>
        `;

        container.appendChild(pointGroup);

        const fileInput = pointGroup.querySelector('input[type="file"]');
        const preview = pointGroup.querySelector('.image-preview');
        const img = preview.querySelector('img');
        const removeBtn = preview.querySelector('.remove-image');

        setupImagePreview(fileInput, preview, img, removeBtn);

        pointGroup.querySelector('.btn-remove-point').addEventListener("click", () => {
            pointGroup.remove();
        });
    }

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
                };
                reader.readAsDataURL(file);
            }
        });

        removeBtn?.addEventListener("click", () => {
            input.value = "";
            preview.style.display = "none";
        });
    }

    // === FORM SUBMISSION: Update Post ===
    if (editForm) {
        editForm.addEventListener("submit", async e => {
            e.preventDefault();
            const formData = new FormData(editForm);

            if (!formData.get("id")) {
                alert("Post ID is missing!");
                return;
            }

            try {
                const res = await fetch("/api/update-post", {
                    method: "POST",
                    body: formData
                });

                if (res.ok) {
                    alert("Post updated successfully!");
                    closeModals();
                    location.reload();
                } else if (res.status === 401) {
                    alert("Session expired. Please log in again.");
                    logout();
                } else {
                    const error = await res.text();
                    alert("Update failed: " + (error || "Unknown error"));
                }
            } catch (err) {
                console.error("Network error:", err);
                alert("Network error. Please check connection and try again.");
            }
        });
    }

    // === DELETE CONFIRMATION ===
    if (confirmDelete) {
        confirmDelete.addEventListener("click", async () => {
            const id = confirmDelete.dataset.id;
            if (!id) {
                alert("Invalid post ID.");
                return;
            }

            try {
                const res = await fetch(`/api/delete-post/${id}`, { method: "DELETE" });
                if (res.ok) {
                    alert("Post deleted.");
                    closeModals();
                    location.reload();
                } else if (res.status === 401) {
                    alert("Session expired. Please log in again.");
                    logout();
                } else {
                    alert("Delete failed. Please try again.");
                }
            } catch (err) {
                console.error("Delete error:", err);
                alert("Network error. Could not delete.");
            }
        });
    }

    // === Utility: HTML Escape ===
    function escapeHtml(text) {
        const div = document.createElement("div");
        div.textContent = text;
        return div.innerHTML;
    }
});