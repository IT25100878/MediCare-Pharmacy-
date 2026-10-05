document.addEventListener("DOMContentLoaded", () => {
    const batchSelect = document.querySelector("#batchId");
    const prescriptionSection = document.querySelector("#prescriptionSection");
    const prescriptionSelect = document.querySelector("#prescriptionId");
    const prescriptionHint = document.querySelector("#prescriptionHint");
    const customerName = document.querySelector("#customerName");
    const customerPhone = document.querySelector("#customerPhone");

    const updatePrescriptionState = () => {
        if (!batchSelect || !prescriptionSection || !prescriptionHint) return;
        const selectedOption = batchSelect.options[batchSelect.selectedIndex];
        const requiresPrescription = selectedOption?.dataset.requiresPrescription === "true";

        prescriptionSection.hidden = !requiresPrescription;
        prescriptionHint.classList.remove("required", "otc");
        if (!selectedOption || !selectedOption.value) {
            prescriptionHint.textContent = "Select a medicine batch to check its prescription requirement.";
            return;
        }
        if (requiresPrescription) {
            prescriptionHint.classList.add("required");
            prescriptionHint.textContent = "Prescription-required medicine: choose an approved prescription before checkout.";
        } else {
            prescriptionHint.classList.add("otc");
            prescriptionHint.textContent = "OTC medicine: an approved prescription is not needed for this checkout.";
            if (prescriptionSelect) prescriptionSelect.value = "";
        }
    };

    const fillCustomerFromPrescription = () => {
        if (!prescriptionSelect) return;
        const selectedOption = prescriptionSelect.options[prescriptionSelect.selectedIndex];
        if (!selectedOption || !selectedOption.value) return;
        if (customerName) customerName.value = selectedOption.dataset.customerName || "";
        if (customerPhone) customerPhone.value = selectedOption.dataset.customerPhone || "";
    };

    batchSelect?.addEventListener("change", updatePrescriptionState);
    prescriptionSelect?.addEventListener("change", fillCustomerFromPrescription);
    updatePrescriptionState();

    const revealItems = document.querySelectorAll("[data-reveal]");
    if (!("IntersectionObserver" in window)) {
        revealItems.forEach((item) => item.classList.add("is-visible"));
        return;
    }
    const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
            if (entry.isIntersecting) {
                entry.target.classList.add("is-visible");
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.08 });
    revealItems.forEach((item) => observer.observe(item));
});
