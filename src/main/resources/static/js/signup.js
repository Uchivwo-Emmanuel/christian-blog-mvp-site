// signup.js
document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("adminSignupForm");
    const successMsg = document.getElementById("successMsg");
    const errorMsg = document.getElementById("errorMsg");
    const adminImageInput = document.getElementById("adminImage"); // ✅ Image input

    if (!form) return;

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        // Reset messages
        successMsg.style.display = "none";
        errorMsg.style.display = "none";

        // Get form values
        const firstName = form.firstName.value.trim();
        const lastName = form.lastName.value.trim();
        const appUserEmail = form.appUserEmail.value.trim().toLowerCase();
        const phoneNumber = form.phoneNumber.value.trim() || null;
        const password = form.password.value;
        const confirmPassword = form.confirmPassword.value;

        // Validate passwords
        if (password !== confirmPassword) {
            errorMsg.textContent = "Passwords do not match.";
            errorMsg.style.display = "block";
            return;
        }

        // ✅ Use FormData for file + text fields
        const formData = new FormData();
        formData.append("firstName", firstName);
        formData.append("lastName", lastName);
        formData.append("appUserEmail", appUserEmail);
        if (phoneNumber) formData.append("phoneNumber", phoneNumber);
        formData.append("password", password);

        // ✅ Append image if selected
        if (adminImageInput.files.length > 0) {
            formData.append("adminImage", adminImageInput.files[0]);
        }

        try {
            const response = await fetch("/admin/signup", {
                method: "POST",
                body: formData // ✅ No manual headers — browser sets content-type
            });

            if (response.ok) {
                successMsg.style.display = "block";
                form.reset();
                // Hide image preview if any
                const imagePreview = document.getElementById("imagePreview");
                if (imagePreview) imagePreview.style.display = "none";

                // Redirect after success
                setTimeout(() => {
                    window.location.href = "../admin/admin-dashboard.html"; // ✅ Fixed path
                }, 1500);
            } else {
                const errorData = await response.text();
                errorMsg.textContent = errorData || "Registration failed.";
                errorMsg.style.display = "block";
            }
        } catch (err) {
            errorMsg.textContent = "Network error. Please try again.";
            errorMsg.style.display = "block";
            console.error("Fetch error:", err);
        }
    });

    // Optional: Image Preview (if you want it)
    setupImagePreview(
        document.getElementById("adminImage"),
        document.getElementById("imagePreview"),
        document.getElementById("previewImg"),
        document.getElementById("removeImage")
    );
});

// ✅ Reusable image preview helper
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