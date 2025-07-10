const forms = document.querySelectorAll('.needs-validation');
console.log(forms);
    Array.from(forms).forEach(function (form){
        form.addEventListener('submit', async function (event){
            event.preventDefault();
            if (!form.checkValidity()) {
                event.stopPropagation();
                console.log("form is invalid"); 
            }else {
                console.log("form is valid");

                const formData = new FormData(form);
                try {
                    const response = await fetch("/post/categories", {
                        method: "POST",
                        body: formData
                    });
                    if (response.ok) {
                        window.location.href = "/post/";
                    } else {
                        alert("Failed to create category");
                    }
                } catch (err) {
                    console.error("Error Submitting Form: " + err);
                    alert("An Error Occurred while submitting the form");
                }
            }
            form.classList.add('was-validated');
        }, false);
    });
    
    document.getElementById('categoryImage').addEventListener("change", function (){
        const preview = document.getElementById("imagePreview");
        const file = this.files[0];

        if (file) {
            preview.src = URL.createObjectURL(file);
            preview.style.display = 'block';
        }
    })
