// category.js
document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("categoryForm");
    const imageInput = document.getElementById("image");
    const imagePreview = document.getElementById("imagePreview");
    const previewImg = document.getElementById("previewImg");
    const removeImage = document.getElementById("removeImage");
    const successMsg = document.getElementById("successMsg");
    const errorMsg = document.getElementById("errorMsg");

    // Image Preview
    imageInput.addEventListener("change", () => {
        const file = imageInput.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = () => {
                previewImg.src = reader.result;
                imagePreview.style.display = "block";
            };
            reader.readAsDataURL(file);
        }
    });

    // Remove Image
    removeImage.addEventListener("click", () => {
        imageInput.value = "";
        imagePreview.style.display = "none";
    });

    // Form Submission
    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        // Reset messages
        successMsg.style.display = "none";
        errorMsg.style.display = "none";

        const formData = new FormData();
        formData.append("title", form.title.value.trim());
        formData.append("description", form.description.value.trim() || "");

        const imageFile = imageInput.files[0];
        if (imageFile) {
            formData.append("image", imageFile);
        }

        try {
            const response = await fetch("/api/create-category", {
                method: "POST",
                headers: {
                    "X-Requested-With": "XMLHttpRequest"
                },
                body: formData
            });

            if (response.ok) {
                successMsg.style.display = "block";
                form.reset();
                imagePreview.style.display = "none";
            } else {
                const errorData = await response.json().catch(() => ({}));
                errorMsg.textContent = errorData.message || "Failed to create category.";
                errorMsg.style.display = "block";
            }
        } catch (err) {
            errorMsg.textContent = "Network error. Please try again.";
            errorMsg.style.display = "block";
            console.error("Fetch error:", err);
        }
    });
});