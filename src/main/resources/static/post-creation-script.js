function addPoint() {
    const container = document.getElementById("pointsContainer");

    const group = document.createElement("div");
    group.classList.add("point-group");

    group.innerHTML = `
        <label for="pointTitle">Point title: </label>
        <input type="text" name="pointTitle" required >
        <label for="pointBody">Point Body: </label>
        <textarea name="pointBody" " placeholder="Body" required></textarea>
        <label for="pointImage">Insert Image for Point: </label>
        <input type="file" name="pointImage" accept="image/*" required>`;

        container.appendChild(group);
}

//Populate category drop down
fetch("/api/get-categories")
    .then(res => res.json())
    .then(data => {
        const select = document.getElementById("categorySelect");
        console.log("Fetched data:", data);
        data.categories.forEach(category => {
            const option = document.createElement("option");
            option.value = category.title;
            option.textContent = category.title;
            select.appendChild(option);
        });
    });

    //create form Data Object to be sent to the backend
    document.getElementById("postForm").addEventListener("submit", async function (e) {
        e.preventDefault();// cancel default form submission
        const form = e.target;
        const formData = new FormData();

        // Append form details to formData
        formData.append("title", document.getElementById("title").value);
        formData.append("titleImage", document.getElementById("titleImage").files[0]);
        formData.append("introduction", document.getElementById("introduction").value);
        formData.append("categoryName", document.getElementById("categorySelect").value);

        //append points to formData
        const pointGroups = document.querySelectorAll(".point-group");
        pointGroups.forEach((group, index) => {
            const title = group.querySelector("input[name = 'pointTitle']").value;
            const image = group.querySelector("input[name = 'pointImage']").files[0];
            const body = group.querySelector("textarea[name = 'pointBody']").value;

            console.log(`Point ${index} - Title:`, title);
            console.log(`Point ${index} - Body:`, body);
            console.log(`Point ${index} - Image:`, image);

            formData.append(`points[${index}].pointTitle`,title);
            formData.append(`points[${index}].pointBody`, body);
            if (image) {
               formData.append(`points[${index}].pointImage`, image); 
            }
            
        });
        console.log(formData);
        // post the formData
        try {
           const response = await fetch("/api/create-post", {
            method: "POST",
            body: formData
           })
           if (response.ok) {
            alert("Post Created successfully");
               for (const [key, value] of formData.entries()) {
                   console.log(`${key}:`, value);
               }
               const result = await response.json();
               const post = result["post:"]
               const postId = post.id;
               window.location.href =`/post/view-post?id=${postId}`; /*Ensure you add the api address here */
           }else{
            alert("Failed to Create Post");
           }
        } catch (error) {
            console.error("Error Submitting Form: ", error);
            alert("Post Creation was unsuccessful");
        }

    })

document.getElementById('categoryImage').addEventListener("change", function (){
    const preview = document.getElementById("imagePreview");
    const file = this.files[0];

    if (file) {
        preview.src = URL.createObjectURL(file);
        preview.style.display = 'block';
    }
})