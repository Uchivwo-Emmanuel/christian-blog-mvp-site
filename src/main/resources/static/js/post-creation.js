// post.js
document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("postForm");
    const pointsContainer = document.getElementById("pointsContainer");
    const addPointBtn = document.getElementById("addPointBtn");
    let pointIndex = 1; // Start after initial point

    // Image Preview for Title Image
    setupImagePreview("titleImage", "titleImagePreview", "titlePreviewImg", "removeTitleImage");

    // Add New Point
    addPointBtn.addEventListener("click", () => {
        const pointGroup = document.createElement("div");
        pointGroup.className = "point-group";
        pointGroup.setAttribute("data-index", pointIndex);

        pointGroup.innerHTML = `
            <div class="form-row">
                <div class="form-group">
                    <label>Point Title</label>
                    <input type="text" name="points[${pointIndex}].pointTitle" required />
                </div>
                <div class="form-group">
                    <label>Point Image (Optional)</label>
                    <input type="file" name="points[${pointIndex}].pointImage" accept="image/*" />
                    <div class="image-preview" style="display: none;">
                        <img src="" alt="Preview" />
                        <button type="button" class="remove-image">&times;</button>
                    </div>
                </div>
            </div>
            <div class="form-group">
                <label>Point Body</label>
                <textarea name="points[${pointIndex}].pointBody" rows="3"></textarea>
            </div>
            <button type="button" class="btn-remove-point">Remove</button>
        `;

        pointsContainer.appendChild(pointGroup);

        // Setup image preview for new point
        const fileInput = pointGroup.querySelector('input[type="file"]');
        const preview = pointGroup.querySelector('.image-preview');
        const img = preview.querySelector('img');
        const removeBtn = preview.querySelector('.remove-image');

        setupPointImagePreview(fileInput, preview, img, removeBtn);

        // Setup remove button
        pointGroup.querySelector('.btn-remove-point').addEventListener("click", () => {
            pointGroup.remove();
        });

        pointIndex++;
    });

    // Setup image preview for existing points
    document.querySelectorAll('.point-group').forEach((group, index) => {
        const fileInput = group.querySelector('input[type="file"]');
        const preview = group.querySelector('.image-preview');
        const img = preview.querySelector('img');
        const removeBtn = preview.querySelector('.remove-image');

        setupPointImagePreview(fileInput, preview, img, removeBtn);

        const removePointBtn = group.querySelector('.btn-remove-point');
        if (index === 0) {
            removePointBtn.disabled = true;
        } else {
            removePointBtn.addEventListener("click", () => {
                group.remove();
            });
        }
    });

    // Populate Category Dropdown
    async function loadCategories() {
        const select = document.getElementById("categoryName");
        try {
            const response = await fetch("/api/get-categories");
            console.log(response)
            if (!response.ok) throw new Error("Failed to load categories");

            const data = await response.json();
            const categories = data.categories || [];

            // Clear loading option
            select.innerHTML = '';

            if (categories.length === 0) {
                const option = document.createElement("option");
                option.value = "";
                option.textContent = "No categories available";
                option.disabled = true;
                option.selected = true;
                select.appendChild(option);
                return;
            }

            // Add options
            const defaultOption = document.createElement("option");
            defaultOption.value = "";
            defaultOption.textContent = "Select a category";
            defaultOption.selected = true;
            select.appendChild(defaultOption);

            categories.forEach(cat => {
                const option = document.createElement("option");
                option.value = cat.title;  // Matches your backend param: categoryName
                option.textContent = cat.title;
                select.appendChild(option);
            });
        } catch (err) {
            console.error("Error loading categories:", err);
            select.innerHTML = '<option value="" disabled selected>Error loading categories</option>';
        }
    }

// Load on page load
    loadCategories();

    // Submit Form
    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const formData = new FormData(form);
        const successMsg = document.getElementById("successMsg");
        const errorMsg = document.getElementById("errorMsg");

        successMsg.style.display = "none";
        errorMsg.style.display = "none";

        try {
            const response = await fetch("/api/create-post", {
                method: "POST",
                body: formData
            });

            if (response.ok) {
                successMsg.style.display = "block";
                form.reset();
                document.querySelectorAll(".image-preview").forEach(p => p.style.display = "none");
                pointIndex = 1;
            } else {
                const errorData = await response.json().catch(() => ({}));
                errorMsg.textContent = errorData.message || "Failed to publish post.";
                errorMsg.style.display = "block";
            }
        } catch (err) {
            errorMsg.textContent = "Network error. Please try again.";
            errorMsg.style.display = "block";
            console.error("Fetch error:", err);
        }
    });

    // Helper: Setup image preview
    function setupImagePreview(inputId, previewId, imgId, removeId) {
        const input = document.getElementById(inputId);
        const preview = document.getElementById(previewId);
        const img = document.getElementById(imgId);
        const remove = document.getElementById(removeId);

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

        remove?.addEventListener("click", () => {
            input.value = "";
            preview.style.display = "none";
        });
    }

    // Setup preview for point images
    function setupPointImagePreview(fileInput, preview, img, removeBtn) {
        fileInput.addEventListener("change", () => {
            const file = fileInput.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = () => {
                    img.src = reader.result;
                    preview.style.display = "block";
                };
                reader.readAsDataURL(file);
            }
        });

        removeBtn.addEventListener("click", () => {
            fileInput.value = "";
            preview.style.display = "none";
        });
    }
});