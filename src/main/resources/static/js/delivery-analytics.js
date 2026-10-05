document.addEventListener("DOMContentLoaded", () => {
    const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    document.querySelectorAll("[data-reveal]").forEach((element, index) => {
        element.style.transitionDelay = `${Math.min(index % 4, 3) * 55}ms`;
    });

    const revealItems = document.querySelectorAll("[data-reveal]");
    if (reducedMotion || !("IntersectionObserver" in window)) {
        revealItems.forEach((element) => element.classList.add("revealed"));
    } else {
        const observer = new IntersectionObserver((entries, currentObserver) => {
            entries.forEach((entry) => {
                if (!entry.isIntersecting) return;
                entry.target.classList.add("revealed");
                currentObserver.unobserve(entry.target);
            });
        }, { threshold: 0.12 });
        revealItems.forEach((element) => observer.observe(element));
    }

    document.querySelectorAll(".flash-message").forEach((message) => {
        window.setTimeout(() => {
            message.style.opacity = "0";
            message.style.transform = "translateY(-7px)";
            window.setTimeout(() => message.remove(), 380);
        }, 5200);
    });

    document.querySelectorAll("button[data-confirm]").forEach((button) => {
        button.addEventListener("click", (event) => {
            if (!window.confirm(button.dataset.confirm || "Continue with this action?")) {
                event.preventDefault();
            }
        });
    });

    document.querySelectorAll("form").forEach((form) => {
        form.addEventListener("submit", () => {
            const button = form.querySelector("button[type='submit']");
            if (!button || button.dataset.confirm) return;
            button.classList.add("is-submitting");
            button.setAttribute("aria-busy", "true");
        });
    });

    document.querySelectorAll("[data-table-filter]").forEach((input) => {
        const table = document.getElementById(input.dataset.tableFilter);
        if (!table) return;
        input.addEventListener("input", () => {
            const query = input.value.trim().toLowerCase();
            table.querySelectorAll("tbody tr").forEach((row) => {
                if (row.querySelector(".empty-cell")) return;
                row.hidden = !row.textContent.toLowerCase().includes(query);
            });
        });
    });

    document.querySelectorAll(".progress-ring[data-progress]").forEach((ring) => {
        const percent = Math.max(0, Math.min(100, Number(ring.dataset.progress) || 0));
        requestAnimationFrame(() => ring.style.setProperty("--progress", `${percent * 3.6}deg`));
    });

    if (!reducedMotion) {
        document.querySelectorAll(".count-up[data-count]").forEach((element) => {
            const target = Number(element.dataset.count) || 0;
            if (target === 0) return;
            const start = performance.now();
            const duration = Math.min(850, 280 + target * 28);
            const animate = (now) => {
                const progress = Math.min((now - start) / duration, 1);
                element.textContent = String(Math.round(target * (1 - Math.pow(1 - progress, 3))));
                if (progress < 1) requestAnimationFrame(animate);
            };
            element.textContent = "0";
            requestAnimationFrame(animate);
        });
    }
});
