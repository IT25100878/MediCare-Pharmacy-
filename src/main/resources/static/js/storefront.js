document.documentElement.classList.add("js-enabled");

document.addEventListener("DOMContentLoaded", () => {
    const header = document.querySelector(".store-header");
    const menuButton = document.getElementById("storeMenuButton");
    const navigation = document.getElementById("storeNavigation");
    const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    const closeMenu = () => {
        navigation?.classList.remove("is-open");
        menuButton?.classList.remove("is-open");
        menuButton?.setAttribute("aria-expanded", "false");
    };

    menuButton?.addEventListener("click", () => {
        const isOpen = navigation?.classList.toggle("is-open") ?? false;
        menuButton.classList.toggle("is-open", isOpen);
        menuButton.setAttribute("aria-expanded", String(isOpen));
    });

    navigation?.querySelectorAll("a").forEach((link) => link.addEventListener("click", closeMenu));

    const updateHeader = () => header?.classList.toggle("is-scrolled", window.scrollY > 10);
    updateHeader();
    window.addEventListener("scroll", updateHeader, { passive: true });

    document.getElementById("currentYear")?.replaceChildren(String(new Date().getFullYear()));

    const fileInput = document.getElementById("prescriptionFile");
    const fileButton = document.getElementById("prescriptionFileButton");
    const fileName = document.getElementById("fileName");

    fileButton?.addEventListener("click", (event) => {
        event.preventDefault();
        fileInput?.click();
    });

    fileInput?.addEventListener("change", () => {

        const selectedFile = fileInput.files?.[0];

        if (fileName) {

            fileName.textContent = selectedFile
                ? selectedFile.name
                : "Choose a prescription file";

        }

    });

    const revealItems = document.querySelectorAll("[data-reveal]");
    if (reducedMotion || !("IntersectionObserver" in window)) {
        revealItems.forEach((item) => item.classList.add("revealed"));
        return;
    }

    const observer = new IntersectionObserver((entries, currentObserver) => {
        entries.forEach((entry) => {
            if (!entry.isIntersecting) {
                return;
            }
            entry.target.classList.add("revealed");
            currentObserver.unobserve(entry.target);
        });
    }, { threshold: 0.13 });

    revealItems.forEach((item, index) => {
        item.style.transitionDelay = `${Math.min(index % 4, 3) * 65}ms`;
        observer.observe(item);
    });
});
