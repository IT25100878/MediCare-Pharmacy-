document.addEventListener("DOMContentLoaded", () => {
    const roleSelect = document.getElementById("roleCode");
    const roleHint = document.getElementById("roleHint");
    const adminCodeGroup = document.getElementById("adminCodeGroup");
    const adminCodeInput = document.getElementById("adminRegistrationCode");
    const password = document.getElementById("password");
    const confirmPassword = document.getElementById("confirmPassword");
    const passwordNote = document.getElementById("passwordNote");

    const roleDescriptions = {
        ADMIN: "Admin workspace: manage user accounts, roles, and team feedback.",
        INVENTORY_SUPERVISOR: "Inventory workspace: manage medicines, stock, and expiry records.",
        CASHIER: "Cashier workspace: create and manage customer orders.",
        DUTY_PHARMACIST: "Pharmacist workspace: review and manage prescription records.",
        OPERATIONS_MANAGER: "Operations workspace: manage pharmacy branch records.",
        PROCUREMENT_COORDINATOR: "Procurement workspace: manage suppliers and purchase orders.",
        CUSTOMER_FEEDBACK_MANAGER: "Feedback workspace: manage customer feedback cases, owners, and resolutions."
    };

    const updateRoleDetails = () => {
        const isAdmin = roleSelect?.value === "ADMIN";
        roleSelect?.closest(".role-field-group")?.classList.toggle("role-selected", Boolean(roleSelect.value));
        if (adminCodeGroup) {
            adminCodeGroup.hidden = !isAdmin;
        }
        if (adminCodeInput) {
            adminCodeInput.required = isAdmin;
            if (!isAdmin) {
                adminCodeInput.value = "";
            }
        }
        if (roleHint) {
            roleHint.textContent = roleDescriptions[roleSelect?.value]
                || "Select a role to see the workspace you will access.";
        }
    };

    const validatePasswords = () => {
        if (!password || !confirmPassword) {
            return;
        }
        const mismatch = Boolean(confirmPassword.value) && password.value !== confirmPassword.value;
        confirmPassword.setCustomValidity(mismatch ? "Passwords must match." : "");
        confirmPassword.closest(".field-group")?.classList.toggle("has-mismatch", mismatch);

        if (passwordNote) {
            if (password.value.length >= 8 && !mismatch) {
                passwordNote.textContent = "Password looks good. Keep it private and unique.";
                passwordNote.classList.add("valid");
            } else if (mismatch) {
                passwordNote.textContent = "The confirmation password must match your password.";
                passwordNote.classList.remove("valid");
            } else {
                passwordNote.textContent = "Use at least 8 characters. Your password is stored securely.";
                passwordNote.classList.remove("valid");
            }
        }
    };

    document.querySelectorAll("[data-password-toggle]").forEach((button) => {
        button.addEventListener("click", () => {
            const input = document.getElementById(button.dataset.passwordToggle);
            if (!input) {
                return;
            }
            const shouldShow = input.type === "password";
            input.type = shouldShow ? "text" : "password";
            button.textContent = shouldShow ? "Hide" : "Show";
            button.setAttribute("aria-label", `${shouldShow ? "Hide" : "Show"} password`);
        });
    });

    document.querySelectorAll(".auth-form input, .auth-form select").forEach((field) => {
        field.addEventListener("blur", () => {
            field.classList.toggle("is-valid", field.checkValidity() && Boolean(field.value));
        });
    });

    roleSelect?.addEventListener("change", updateRoleDetails);
    password?.addEventListener("input", validatePasswords);
    confirmPassword?.addEventListener("input", validatePasswords);

    updateRoleDetails();
    validatePasswords();
});
