// login.js
document.getElementById("loginForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
        const response = await fetch("/api/admin/login", {
            method: "POST",
            body: new URLSearchParams({ email, password })
        });

        console.log("Response status:", response.status);
        const text = await response.text();
        console.log("Raw response:", text);

        try {
            const data = JSON.parse(text);
            console.log("Parsed data:", data);

            if (data.token) {
                localStorage.setItem("jwtToken", data.token);
                console.log(data.token)
                localStorage.setItem("currentUser", JSON.stringify(data.user));
                console.log("Token saved, redirecting...");
                window.location.href = "/entry.html"; // Should trigger new request
            } else {
                console.error("No token in response");
                alert("Login failed: Invalid response");
            }
        } catch (jsonError) {
            console.error("Invalid JSON:", text);
            alert("Login failed: Server returned invalid data");
        }
    } catch (err) {
        console.error("Fetch error:", err);
        alert("Network error. Check console.");
    }
});