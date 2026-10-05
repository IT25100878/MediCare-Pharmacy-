document.addEventListener("DOMContentLoaded", () => {
    const header = document.querySelector(".site-header");
    const navToggle = document.getElementById("navToggle");
    const siteNav = document.getElementById("siteNav");
    const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

    const closeNavigation = () => {
        siteNav?.classList.remove("open");
        navToggle?.classList.remove("active");
        navToggle?.setAttribute("aria-expanded", "false");
    };

    navToggle?.addEventListener("click", () => {
        const isOpen = siteNav?.classList.toggle("open");
        navToggle.classList.toggle("active", Boolean(isOpen));
        navToggle.setAttribute("aria-expanded", String(Boolean(isOpen)));
    });

    siteNav?.querySelectorAll("a").forEach((link) => link.addEventListener("click", closeNavigation));

    window.addEventListener("scroll", () => {
        header?.classList.toggle("scrolled", window.scrollY > 12);
    }, { passive: true });

    document.getElementById("currentYear")?.replaceChildren(String(new Date().getFullYear()));

    const revealItems = document.querySelectorAll("[data-reveal]");
    if (reducedMotion || !("IntersectionObserver" in window)) {
        revealItems.forEach((item) => item.classList.add("revealed"));
        return;
    }

    const observer = new IntersectionObserver((entries, currentObserver) => {
        entries.forEach((entry) => {
            if (entry.isIntersecting) {
                entry.target.classList.add("revealed");
                currentObserver.unobserve(entry.target);
            }
        });
    }, { threshold: 0.14 });

    revealItems.forEach((item, index) => {
        item.style.transitionDelay = `${Math.min(index % 4, 3) * 70}ms`;
        observer.observe(item);
    });
});
