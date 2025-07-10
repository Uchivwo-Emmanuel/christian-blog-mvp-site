//Get ID from Url
function getIdFromUrl() {
    const params = new URLSearchParams(window.location.search);
    return  params.get("id");
}

const postId = getIdFromUrl();

// if(!postId){
//     alert("Post ID not provided in URL");
// }else {
//     fetch(`/api/post/${postId}`)
//         .then(response => {
//             if(!response.ok){
//                 throw new Error("No Post Found");
//             }else {
//                 return response.json();
//             }
//         })
//         .then(data => {
//             const post = data.post;
//
//             //set title and Introduction
//             document.getElementById("postTitle").textContent = post.title || "Untitled";
//             document.getElementById("pointIntro").textContent = post.introduction || "";
//
//             //show title image if available
//             const titleImage = document.getElementById("postTitleImage");
//             if (post.titleImageName){
//                 titleImage.src = `/uploads/${post.titleImageName}`;
//                 titleImage.display = "block";
//             }else{
//                 titleImage.style.display = "none";
//             }
//
//             //display points
//             const pointsContainer = document.getElementById("pointsContainer");
//             pointsContainer.innerHTML = "";// clear existing content
//
//             if (post.points && post.points.length > 0){
//                 post.points.forEach(point => {
//                     const pointDiv = document.createElement("div");
//                     pointDiv.classList.add("point");
//
//                     let pointHTML = `<h3>${point.pointHeading || ""}</h3>
//                                             <p>${point.pointBody || ""}</p>`;
//                     if (point.pointImageName){
//                         pointHTML += `<img src="/uploads/${point.pointImageName}" alt="Point Image">`;
//                     }
//                     pointDiv.innerHTML = pointHTML;
//                     pointsContainer.appendChild(pointDiv);
//                 })
//             }
//         })
//         .catch(err => {
//             console.error("Error Loading Post:", err);
//             document.getElementById("postTitle").textContent = "Post Not Found";
//         });
// }


