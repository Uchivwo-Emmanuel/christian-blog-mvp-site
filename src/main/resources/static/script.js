const menuButton = document.getElementById('menuButton');
const menuList = document.getElementById('menuList');

menuButton.addEventListener('click', () => {
    if(getComputedStyle(menuList).display=== 'none'){
        menuList.style.display = 'block';

        requestAnimationFrame(() => {
            menuList.classList.remove('hide');
            menuList.classList.add('show');
        })

        setTimeout(() => {
            menuButton.classList.remove('bi-list');
            menuButton.classList.add('bi-x');
            menuButton.style.transform = 'rotate(180deg)';
        }, 250);
    }else{
        menuList.classList.remove('show');
        menuList.classList.add('hide');
        setTimeout(() => {
            menuButton.classList.remove('bi-x');
            menuButton.classList.add('bi-list');
            menuButton.style.transform = 'rotate(0deg)';
        }, 250);
        setTimeout(() => {
            menuList.style.display = 'none';
        }, 600);
    }
})



    //MODAL FORM


document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('myModal');
    const modalTitle = document.getElementById('modalTitle');
    const modalBody = document.getElementById('modalBody');
    const openModalBtn = document.getElementById('openModalBtn');

    const closeModal = () => {
        modal.classList.remove('show');
    };

    const openModal = (title, bodyHTML) => {
        modalTitle.innerHTML = title;
        modalBody.innerHTML = bodyHTML;
        modal.classList.add('show');

        // Close modal when clicking outside
        window.addEventListener('click', (event) => {
            if (event.target === modal) {
                closeModal();
            }
        });

        // Close modal when pressing escape
        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape' && modal.classList.contains('show')) {
                closeModal();
            }
        });

        // Get form elements AFTER injecting them
        const form = document.getElementById('registrationForm');
        const fullNameInput = document.getElementById('fullName');
        const emailInput = document.getElementById('email');
        const fullNameError = document.getElementById('fullNameError');
        const emailError = document.getElementById('emailError');

        // Utility function to show error
        const displayError = (element, message) => {
            element.textContent = message;
            element.style.display = 'block';
            element.previousElementSibling.classList.add('invalid');
            element.previousElementSibling.classList.remove('valid');
        };

        // Utility function to clear error
        const clearError = (element) => {
            element.textContent = '';
            element.style.display = 'none';
            element.previousElementSibling.classList.remove('invalid');
            element.previousElementSibling.classList.add('valid');
        };

        const validateFullName = () => {
            const fullName = fullNameInput.value.trim();
            if (fullName === '') {
                displayError(fullNameError, 'Full Name is required');
                return false;
            } else if (fullName.length < 3) {
                displayError(fullNameError, 'Full Name must be at least 3 characters');
                return false;
            } else {
                clearError(fullNameError);
                return true;
            }
        };

        const validateEmail = () => {
            const email = emailInput.value.trim();
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (email === '') {
                displayError(emailError, 'Email is required');
                return false;
            } else if (!emailRegex.test(email)) {
                displayError(emailError, 'Please enter a valid email address');
                return false;
            } else {
                clearError(emailError);
                return true;
            }
        };

        // Real-time validation
        fullNameInput.addEventListener('input', validateFullName);
        emailInput.addEventListener('input', validateEmail);

        // Form submission handler
        form.addEventListener('submit', (event) => {
            event.preventDefault();

            const isFullNameValid = validateFullName();
            const isEmailValid = validateEmail();

            if (isFullNameValid && isEmailValid) {
                alert('Form submitted successfully!');
                form.reset();
                closeModal();
                document.querySelectorAll('input').forEach(input => {
                    input.classList.remove('valid', 'invalid');
                });
            } else {
                alert('Please correct the errors in the form.');
            }
        });
    };

    // Open modal on button click
    openModalBtn.addEventListener('click', () => {
        openModal(`Register`,
            `<form id="registrationForm" class="modal-body">
            <div class="form-group">
                <label for="fullName">Full Name:</label>
                <input type="text" id="fullName" name="fullName" placeholder="Enter your full name">
                <div class="error-message" id="fullNameError"></div>
            </div>

            <div class="form-group">
                <label for="email">Email:</label>
                <input type="text" id="email" name="email" placeholder="Enter your email address">
                <div class="error-message" id="emailError"></div>
            </div>

            <button type="submit" class="secondary-button">Sign Up</button>
        </form>`
        );
    });
});
