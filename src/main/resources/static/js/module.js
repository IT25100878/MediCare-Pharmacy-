if (!window.pharmaCareModuleUiInitialized) {
    window.pharmaCareModuleUiInitialized = true;
    document.addEventListener("DOMContentLoaded", () => {
    const menuToggle = document.getElementById("moduleMenuToggle");
    const headerActions = document.getElementById("moduleHeaderActions");
    const motionCards = document.querySelectorAll(".form-card, .info-card, .list-card, .edit-card, .submitted-feedback");
    const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    document.body.classList.add("motion-ready");
    motionCards.forEach((card, index) => {
        card.setAttribute("data-module-reveal", "");
        card.style.setProperty("--module-reveal-delay", `${Math.min(index, 3) * 80}ms`);
    });
    if (reducedMotion || !("IntersectionObserver" in window)) {
        motionCards.forEach((card) => card.classList.add("revealed"));
    } else {
        const motionObserver = new IntersectionObserver((entries, observer) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    entry.target.classList.add("revealed");
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.12 });
        motionCards.forEach((card) => motionObserver.observe(card));
    }

    menuToggle?.addEventListener("click", () => {
        const isOpen = headerActions?.classList.toggle("open");
        menuToggle.classList.toggle("active", Boolean(isOpen));
        menuToggle.setAttribute("aria-expanded", String(Boolean(isOpen)));
    });

    headerActions?.querySelectorAll("a").forEach((link) => link.addEventListener("click", () => {
        headerActions.classList.remove("open");
        menuToggle?.classList.remove("active");
        menuToggle?.setAttribute("aria-expanded", "false");
    }));

    document.querySelectorAll(".notice.success").forEach((notice) => {
        window.setTimeout(() => notice.classList.add("notice-fade"), 4200);
    });

    let pendingForm = null;
    let triggeringControl = null;
    const confirmation = document.createElement("div");
    confirmation.className = "module-dialog";
    confirmation.hidden = true;
    confirmation.innerHTML = `
        <div class="module-dialog-backdrop" data-module-dialog-close></div>
        <section class="module-dialog-card" role="dialog" aria-modal="true" aria-labelledby="moduleDialogTitle" aria-describedby="moduleDialogCopy">
            <span class="module-dialog-icon">!</span>
            <h2 id="moduleDialogTitle">Delete this record?</h2>
            <p id="moduleDialogCopy">This action cannot be undone after confirmation.</p>
            <div class="module-dialog-actions">
                <button class="module-dialog-cancel" type="button" data-module-dialog-close>Keep record</button>
                <button class="module-dialog-delete" id="moduleDialogConfirm" type="button">Delete record</button>
            </div>
        </section>`;
    document.body.append(confirmation);

    const closeConfirmation = () => {
        confirmation.hidden = true;
        pendingForm = null;
        triggeringControl?.focus();
        triggeringControl = null;
    };

    document.querySelectorAll("[data-confirm]").forEach((control) => {
        control.addEventListener("click", (event) => {
            event.preventDefault();
            pendingForm = control.closest("form");
            triggeringControl = control;
            confirmation.querySelector("#moduleDialogTitle").textContent = control.dataset.confirm || "Delete this record?";
            confirmation.hidden = false;
            confirmation.querySelector(".module-dialog-delete")?.focus();
        });
    });

    confirmation.querySelectorAll("[data-module-dialog-close]").forEach((control) => control.addEventListener("click", closeConfirmation));
    confirmation.querySelector("#moduleDialogConfirm")?.addEventListener("click", () => {
        if (pendingForm) {
            HTMLFormElement.prototype.submit.call(pendingForm);
        }
    });

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape" && !confirmation.hidden) {
            closeConfirmation();
        }
    });
    });
}
